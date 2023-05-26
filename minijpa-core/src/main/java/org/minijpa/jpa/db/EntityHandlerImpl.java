/*
 * Copyright 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.minijpa.jpa.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;
import javax.persistence.OptimisticLockException;

import org.minijpa.jdbc.FetchParameter;
import org.minijpa.jdbc.ModelValueArray;
import org.minijpa.jdbc.QueryParameter;
import org.minijpa.jpa.MetaEntityHelper;
import org.minijpa.jpa.model.MetaAttribute;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.jpa.model.Pk;
import org.minijpa.jpa.model.relationship.JoinColumnMapping;
import org.minijpa.jpa.model.relationship.Relationship;
import org.minijpa.jpa.model.relationship.RelationshipJoinTable;
import org.minijpa.metadata.PersistenceUnitContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author adamato
 */
public class EntityHandlerImpl implements EntityHandler {

  private final Logger LOG = LoggerFactory.getLogger(EntityHandlerImpl.class);
  private final PersistenceUnitContext persistenceUnitContext;
  private final EntityContainer entityContainer;
  private final JdbcQueryRunner jdbcQueryRunner;
  private LockType lockType = LockType.NONE;

  public EntityHandlerImpl(PersistenceUnitContext persistenceUnitContext,
      EntityContainer entityContainer,
      JdbcQueryRunner jdbcQueryRunner) {
    this.persistenceUnitContext = persistenceUnitContext;
    this.entityContainer = entityContainer;
    this.jdbcQueryRunner = jdbcQueryRunner;
  }

  @Override
  public LockType getLockType() {
    return lockType;
  }

  @Override
  public void setLockType(LockType lockType) {
    this.lockType = lockType;
  }

  @Override
  public Object findById(MetaEntity metaEntity, Object primaryKey, LockType lockType)
      throws Exception {
    Object entityInstance = entityContainer.find(metaEntity.getEntityClass(), primaryKey);
    if (entityInstance != null) {
      return entityInstance;
    }

//	LOG.debug("findById: metaEntity={}", metaEntity);
//	LOG.debug("findById: primaryKey={}", primaryKey);
    Optional<ModelValueArray<FetchParameter>> optional = jdbcQueryRunner.findById(metaEntity,
        primaryKey, lockType);
    if (optional.isEmpty()) {
      return null;
    }

    ModelValueArray<FetchParameter> modelValueArray = optional.get();

//	for (int i = 0; i < modelValueArray.size(); ++i) {
//	    LOG.debug("findById: modelValueArray.getModel(i).getAttribute()={}", modelValueArray.getModel(i).getAttribute());
//	    LOG.debug("findById: modelValueArray.getValue(i)={}", modelValueArray.getValue(i));
//	}
    entityInstance = MetaEntityHelper.build(metaEntity, primaryKey);
    buildAttributeValuesLoadFK(entityInstance, metaEntity, metaEntity.getAttributes(),
        modelValueArray, lockType);
    entityContainer.addManaged(entityInstance, primaryKey);
    MetaEntityHelper.setEntityStatus(metaEntity, entityInstance,
        EntityStatus.FLUSHED_LOADED_FROM_DB);
    fillCircularRelationships(metaEntity, entityInstance);
    return entityInstance;
  }

  @Override
  public Object queryVersionValue(MetaEntity metaEntity, Object primaryKey, LockType lockType)
      throws Exception {
    Optional<ModelValueArray<FetchParameter>> optional = jdbcQueryRunner.runVersionQuery(metaEntity,
        primaryKey,
        lockType);
    if (optional.isEmpty()) {
      return null;
    }

    ModelValueArray<FetchParameter> modelValueArray = optional.get();
    return modelValueArray.getValue(0);
  }

  @Override
  public void refresh(MetaEntity metaEntity, Object entityInstance, Object primaryKey,
      LockType lockType)
      throws Exception {
    Optional<ModelValueArray<FetchParameter>> optional = jdbcQueryRunner.findById(metaEntity,
        primaryKey, lockType);
    if (optional.isEmpty()) {
      throw new EntityNotFoundException(
          "Entity '" + entityInstance + "' not found: pk=" + primaryKey);
    }

    ModelValueArray<FetchParameter> modelValueArray = optional.get();
    buildAttributeValuesLoadFK(entityInstance, metaEntity, metaEntity.getAttributes(),
        modelValueArray, lockType);
    MetaEntityHelper.setEntityStatus(metaEntity, entityInstance,
        EntityStatus.FLUSHED_LOADED_FROM_DB);
    fillCircularRelationships(metaEntity, entityInstance);
    MetaEntityHelper.removeChanges(metaEntity, entityInstance);
    MetaEntityHelper.clearLazyAttributeLoaded(metaEntity, entityInstance);
  }

  private void fillCircularRelationships(MetaEntity entity, Object entityInstance)
      throws Exception {
    LOG.debug("fillCircularRelationships: entity={}", entity);
    LOG.debug("fillCircularRelationships: entityInstance={}", entityInstance);
    for (MetaAttribute a : entity.getRelationshipAttributes()) {
      if (!a.isEager()) {
        continue;
      }

      if (a.getRelationship().toOne() && a.getRelationship().isOwner()) {
        LOG.debug("fillCircularRelationships: a={}", a);
        Object value = MetaEntityHelper.getAttributeValue(entityInstance, a);
        LOG.debug("fillCircularRelationships: value={}", value);
        if (value == null) {
          continue;
        }

        MetaAttribute targetAttribute = a.getRelationship().getTargetAttribute();
        LOG.debug("fillCircularRelationships: targetAttribute={}", targetAttribute);
        LOG.debug("fillCircularRelationships: a.getRelationship().getAttributeType()={}",
            a.getRelationship().getAttributeType());
        MetaEntity toEntity = a.getRelationship().getAttributeType();
        if (toEntity == null) {
          continue;
        }

        MetaAttribute attribute = toEntity.findAttributeByMappedBy(a.getName());
        LOG.debug("fillCircularRelationships: attribute={}", attribute);
        if (attribute == null) {
          continue;
        }

        // it's bidirectional
        if (attribute.getRelationship().toOne()) {
          Object v = MetaEntityHelper.getAttributeValue(value, attribute);
          LOG.debug("fillCircularRelationships: v={}", v);
          if (v == null) {
            MetaEntityHelper.writeMetaAttributeValue(value, value.getClass(), attribute,
                entityInstance,
                toEntity);
          }
        }
      }
    }
  }

  @Override
  public Object build(
      ModelValueArray<FetchParameter> modelValueArray,
      MetaEntity entity)
      throws Exception {
    LOG.debug("build: entity={}", entity);

    Object primaryKey = AttributeUtil.buildPK(entity.getId(), modelValueArray);
    LOG.debug("build: primaryKey={}", primaryKey);
    Object entityInstance = entityContainer.find(entity.getEntityClass(), primaryKey);
    if (entityInstance != null) {
      return entityInstance;
    }

    entityInstance = MetaEntityHelper.build(entity, primaryKey);
    LOG.debug("build: entityInstance={}", entityInstance);
    buildAttributeValuesLoadFK(entityInstance, entity, entity.getAttributes(), modelValueArray,
        lockType);
    entityContainer.addManaged(entityInstance, primaryKey);
    MetaEntityHelper.setEntityStatus(entity, entityInstance, EntityStatus.FLUSHED_LOADED_FROM_DB);
    fillCircularRelationships(entity, entityInstance);
    return entityInstance;
  }

  private void buildAttributeValuesLoadFK(
      Object parentInstance,
      MetaEntity metaEntity,
      List<MetaAttribute> attributes,
      ModelValueArray<FetchParameter> modelValueArray,
      LockType lockType)
      throws Exception {
    // basic attributes and relationship attributes
    for (MetaAttribute attribute : attributes) {
//			LOG.info("buildAttributeValuesLoadFK: attribute={}", attribute);
      if (attribute.getRelationship() != null) {
        loadJoinTableRelationships(parentInstance, metaEntity, attribute, lockType);
      } else {
        buildBasicAttribute(attribute, parentInstance, metaEntity, modelValueArray);
      }
    }

//	LOG.debug("buildAttributeValuesLoadFK: metaEntity.getEmbeddables()={}", metaEntity.getEmbeddables());
    // embeddables
    for (MetaEntity embeddable : metaEntity.getEmbeddables()) {
      Object parent = embeddable.getEntityClass().getDeclaredConstructor().newInstance();
      buildAttributeValuesLoadFK(parent, embeddable, embeddable.getAttributes(), modelValueArray,
          lockType);
      MetaEntityHelper.writeEmbeddableValue(parentInstance, parentInstance.getClass(), embeddable,
          parent,
          metaEntity);
    }

//	LOG.debug("buildAttributeValuesLoadFK: metaEntity.getJoinColumnMappings()={}", metaEntity.getJoinColumnMappings());
    // join columns
    for (JoinColumnMapping joinColumnMapping : metaEntity.getJoinColumnMappings()) {
//			LOG.info("buildAttributeValuesLoadFK: joinColumnMapping.getAttribute()={}", joinColumnMapping.getAttribute());
//	    LOG.debug("buildAttributeValuesLoadFK: joinColumnMapping.getForeignKey()={}", joinColumnMapping.getForeignKey());
      Object fk = AttributeUtil.buildPK(joinColumnMapping.getForeignKey(), modelValueArray);
      if (joinColumnMapping.isLazy()) {
        MetaEntityHelper.setForeignKeyValue(joinColumnMapping.getAttribute(), parentInstance, fk);
        continue;
      }

      Object parent = loadRelationshipByForeignKey(parentInstance, metaEntity,
          joinColumnMapping.getAttribute(),
          fk, lockType);
      MetaEntityHelper.writeMetaAttributeValue(parentInstance, parentInstance.getClass(),
          joinColumnMapping.getAttribute(), parent, metaEntity);
    }
  }

  private void buildBasicAttribute(
      MetaAttribute attribute,
      Object parentInstance,
      MetaEntity metaEntity,
      ModelValueArray<FetchParameter> modelValueArray) throws Exception {
    int index = AttributeUtil.indexOfAttribute(modelValueArray, attribute);
    if (index == -1) {
      throw new IllegalArgumentException(
          "Column '" + attribute.getColumnName() + "' not found");
    }

    MetaEntityHelper.writeMetaAttributeValue(
        parentInstance,
        parentInstance.getClass(),
        attribute,
        modelValueArray.getValue(index),
        metaEntity);
  }

  private void buildAttributeValues(
      Object parentInstance,
      MetaEntity metaEntity,
      List<MetaAttribute> attributes,
      ModelValueArray<FetchParameter> modelValueArray,
      LockType lockType) throws Exception {
    // basic attributes and relationship attributes
    for (MetaAttribute attribute : attributes) {
      if (attribute.getRelationship() != null) {
        loadJoinTableRelationships(parentInstance, metaEntity, attribute, lockType);
      } else {
        buildBasicAttribute(attribute, parentInstance, metaEntity, modelValueArray);
      }
    }

//	LOG.debug("buildAttributeValues: metaEntity.getEmbeddables()={}", metaEntity.getEmbeddables());
    // load embeddables
    for (MetaEntity embeddable : metaEntity.getEmbeddables()) {
      Object parent = embeddable.getEntityClass().getDeclaredConstructor().newInstance();
      buildAttributeValues(parent, embeddable, embeddable.getAttributes(), modelValueArray,
          lockType);
      MetaEntityHelper.writeEmbeddableValue(parentInstance, parentInstance.getClass(), embeddable,
          parent,
          metaEntity);
    }

//	LOG.debug("buildAttributeValues: metaEntity.getJoinColumnMappings()={}", metaEntity.getJoinColumnMappings());
    // attributes with join columns
    for (JoinColumnMapping joinColumnMapping : metaEntity.getJoinColumnMappings()) {
//	    LOG.debug("buildAttributeValues: joinColumnMapping.getAttribute()={}", joinColumnMapping.getAttribute());
//	    LOG.debug("buildAttributeValues: joinColumnMapping.getForeignKey()={}", joinColumnMapping.getForeignKey());
      Object fk = AttributeUtil.buildPK(joinColumnMapping.getForeignKey(), modelValueArray);
      if (joinColumnMapping.isLazy()) {
        MetaEntityHelper.setForeignKeyValue(joinColumnMapping.getAttribute(), parentInstance, fk);
        continue;
      }

      MetaEntity toEntity = joinColumnMapping.getAttribute().getRelationship().getAttributeType();
//	    LOG.debug("buildAttributeValues: toEntity={}", toEntity);
      Object parent = buildEntityByValues(modelValueArray, toEntity, lockType);
      MetaEntityHelper.writeMetaAttributeValue(parentInstance, parentInstance.getClass(),
          joinColumnMapping.getAttribute(), parent, metaEntity);
    }
  }

  private Object buildEntityByValues(
      ModelValueArray<FetchParameter> modelValueArray,
      MetaEntity entity,
      LockType lockType) throws Exception {
    Object primaryKey = AttributeUtil.buildPK(entity.getId(), modelValueArray);
    LOG.debug("buildEntityByValues: primaryKey={}", primaryKey);
    LOG.debug("buildEntityByValues: entity={}", entity);
    Object entityInstance = entityContainer.find(entity.getEntityClass(), primaryKey);
    if (entityInstance != null) {
      return entityInstance;
    }

    entityInstance = MetaEntityHelper.build(entity, primaryKey);
    buildAttributeValues(entityInstance, entity, entity.getAttributes(), modelValueArray, lockType);
    entityContainer.addManaged(entityInstance, primaryKey);
    MetaEntityHelper.setEntityStatus(entity, entityInstance, EntityStatus.FLUSHED_LOADED_FROM_DB);
    fillCircularRelationships(entity, entityInstance);
    return entityInstance;
  }

  @Override
  public Object buildNoQueries(
      ModelValueArray<FetchParameter> modelValueArray,
      MetaEntity entity)
      throws Exception {
    return buildEntityByValues(modelValueArray, entity, lockType);
  }

  private void buildAttributeValuesNoRelationshipLoading(
      Object parentInstance,
      MetaEntity metaEntity,
      List<MetaAttribute> attributes,
      ModelValueArray<FetchParameter> modelValueArray,
      LockType lockType) throws Exception {
    // basic attributes and relationship attributes
    for (MetaAttribute attribute : attributes) {
      if (attribute.getRelationship() == null) {
        buildBasicAttribute(attribute, parentInstance, metaEntity, modelValueArray);
      }
    }

//	LOG.debug("buildAttributeValues: metaEntity.getEmbeddables()={}", metaEntity.getEmbeddables());
    // load embeddables
    for (MetaEntity embeddable : metaEntity.getEmbeddables()) {
      Object parent = embeddable.getEntityClass().getDeclaredConstructor().newInstance();
      buildAttributeValuesNoRelationshipLoading(parent, embeddable, embeddable.getAttributes(),
          modelValueArray,
          lockType);
      MetaEntityHelper.writeEmbeddableValue(parentInstance, parentInstance.getClass(), embeddable,
          parent,
          metaEntity);
    }

//	LOG.debug("buildAttributeValues: metaEntity.getJoinColumnMappings()={}", metaEntity.getJoinColumnMappings());
    // attributes with join columns
    for (JoinColumnMapping joinColumnMapping : metaEntity.getJoinColumnMappings()) {
//	    LOG.debug("buildAttributeValues: joinColumnMapping.getAttribute()={}", joinColumnMapping.getAttribute());
//	    LOG.debug("buildAttributeValues: joinColumnMapping.getForeignKey()={}", joinColumnMapping.getForeignKey());
      Object fk = AttributeUtil.buildPK(joinColumnMapping.getForeignKey(), modelValueArray);
      if (joinColumnMapping.isLazy()) {
        MetaEntityHelper.setForeignKeyValue(joinColumnMapping.getAttribute(), parentInstance, fk);
        continue;
      }

      MetaEntity toEntity = joinColumnMapping.getAttribute().getRelationship().getAttributeType();
//	    LOG.debug("buildAttributeValues: toEntity={}", toEntity);
      Object parent = buildEntityByValuesNoRelationshipAttributeLoading(modelValueArray, toEntity,
          lockType);
      MetaEntityHelper.writeMetaAttributeValue(parentInstance, parentInstance.getClass(),
          joinColumnMapping.getAttribute(), parent, metaEntity);
    }
  }

  private Object buildEntityByValuesNoRelationshipAttributeLoading(
      ModelValueArray<FetchParameter> modelValueArray,
      MetaEntity entity,
      LockType lockType) throws Exception {
    Object primaryKey = AttributeUtil.buildPK(entity.getId(), modelValueArray);
    LOG.debug("buildEntityByValues: primaryKey={}", primaryKey);
    LOG.debug("buildEntityByValues: entity={}", entity);
    Object entityInstance = entityContainer.find(entity.getEntityClass(), primaryKey);
    LOG.debug("buildEntityByValues: entityInstance={}", entityInstance);
    if (entityInstance != null) {
      return entityInstance;
    }

    entityInstance = MetaEntityHelper.build(entity, primaryKey);
    buildAttributeValuesNoRelationshipLoading(entityInstance, entity, entity.getAttributes(),
        modelValueArray, lockType);
    entityContainer.addManaged(entityInstance, primaryKey);
    MetaEntityHelper.setEntityStatus(entity, entityInstance, EntityStatus.FLUSHED_LOADED_FROM_DB);
    fillCircularRelationships(entity, entityInstance);
    return entityInstance;
  }

  @Override
  public Object buildEntityNoRelationshipAttributeLoading(
      ModelValueArray<FetchParameter> modelValueArray,
      MetaEntity entity)
      throws Exception {
    return buildEntityByValuesNoRelationshipAttributeLoading(modelValueArray, entity, lockType);
  }

  private void loadJoinTableRelationships(
      Object parentInstance,
      MetaEntity entity,
      MetaAttribute attribute,
      LockType lockType) throws Exception {
    if (!attribute.isEager()) {
      return;
    }

    if (attribute.getRelationship().getJoinTable() == null) {
      return;
    }

    if (attribute.getRelationship().isOwner()) {
      Object pk = AttributeUtil.getIdValue(entity, parentInstance);
      Object result = jdbcQueryRunner.selectByJoinTable(pk, entity.getId(),
          attribute.getRelationship(),
          attribute, this);
      MetaEntityHelper.writeMetaAttributeValue(parentInstance, parentInstance.getClass(), attribute,
          result,
          entity);
    }
  }

  private Object loadRelationshipByForeignKey(
      Object parentInstance,
      MetaEntity entity,
      MetaAttribute foreignKeyAttribute,
      Object foreignKeyValue,
      LockType lockType)
      throws Exception {
    // foreign key on the same table
    LOG.debug("loadRelationshipByForeignKey: foreignKeyAttribute={}; foreignKeyValue={}",
        foreignKeyAttribute,
        foreignKeyValue);
    LOG.debug("loadRelationshipByForeignKey: parentInstance={}", parentInstance);
    MetaEntity e = persistenceUnitContext.getEntities()
        .get(foreignKeyAttribute.getType().getName());
    LOG.debug("loadRelationshipByForeignKey: e={}", e);
    Object foreignKeyInstance = findById(e, foreignKeyValue, lockType);
    LOG.debug("loadRelationshipByForeignKey: foreignKeyInstance={}", foreignKeyInstance);
    LOG.debug("loadRelationshipByForeignKey: parentInstance={}", parentInstance);
    LOG.debug("loadRelationshipByForeignKey: foreignKeyAttribute={}; foreignKeyValue={}",
        foreignKeyAttribute,
        foreignKeyValue);
    if (foreignKeyInstance != null) {
      MetaEntityHelper.writeAttributeValue(entity, parentInstance, foreignKeyAttribute,
          foreignKeyInstance);
//	    entityInstanceBuilder.writeMetaAttributeValue(
//		    parentInstance, parentInstance.getClass(), foreignKeyAttribute, foreignKeyInstance, entity);

      MetaAttribute a = e.findAttributeByMappedBy(foreignKeyAttribute.getName());
      LOG.debug("loadRelationshipByForeignKey: a={}", a);
      if (a != null && a.getRelationship().toOne()) {
        MetaEntityHelper.writeMetaAttributeValue(foreignKeyInstance, foreignKeyInstance.getClass(),
            a,
            parentInstance, e);
      }
    }

    return foreignKeyInstance;
  }

  @Override
  public Object loadAttribute(
      Object parentInstance,
      MetaAttribute a,
      Object value)
      throws Exception {
    MetaAttribute targetAttribute = null;
    Relationship relationship = a.getRelationship();
    LOG.debug("loadAttribute: a={}", a);
    LOG.debug("loadAttribute: value={}", value);
    LOG.debug("loadAttribute: parentInstance={}", parentInstance);
    LOG.debug("loadAttribute: relationship={}", relationship);
    if (relationship != null) {
      targetAttribute = relationship.getTargetAttribute();
    }

    LOG.debug("loadAttribute: targetAttribute={}", targetAttribute);
    if (relationship == null || !relationship.toMany()) {
      MetaEntity entity = persistenceUnitContext.getEntities()
          .get(parentInstance.getClass().getName());
      Object foreignKey = MetaEntityHelper.getForeignKeyValue(a, parentInstance);
      LOG.debug("loadAttribute: foreignKey={}", foreignKey);
      Object result = loadRelationshipByForeignKey(parentInstance, entity, a, foreignKey,
          LockType.NONE);
      return result;
    }

    LOG.debug("loadAttribute: to Many targetAttribute={}; relationship.getJoinTable()={}",
        targetAttribute,
        relationship.getJoinTable());
    if (relationship.getJoinTable() == null) {
      MetaEntity entity = persistenceUnitContext.getEntities()
          .get(relationship.getTargetEntityClass().getName());
      LOG.debug("loadAttribute: entity={}", entity);
      if (entity == null) {
        throw new IllegalArgumentException(
            "Class '" + relationship.getTargetEntityClass().getName() + "' is not an entity");
      }

      LOG.debug("loadAttribute: relationship.getOwningAttribute()={}",
          relationship.getOwningAttribute());
      return jdbcQueryRunner.selectByForeignKey(entity, relationship.getOwningAttribute(),
          parentInstance,
          LockType.NONE, this);
    }

    MetaEntity e = persistenceUnitContext.getEntities().get(parentInstance.getClass().getName());
    Object pk = AttributeUtil.getIdValue(e, parentInstance);
    LOG.debug("loadAttribute: pk={}", pk);
    return jdbcQueryRunner.selectByJoinTable(pk, e.getId(), relationship, a, this);
  }

  @Override
  public void persist(
      MetaEntity entity,
      Object entityInstance,
      ModelValueArray<MetaAttribute> modelValueArray)
      throws Exception {
    EntityStatus entityStatus = MetaEntityHelper.getEntityStatus(entity, entityInstance);
    LOG.debug("persist: entityStatus={}", entityStatus);
    if (MetaEntityHelper.isFlushed(entity, entityInstance)) {
      update(entity, entityInstance, modelValueArray);
    } else {
      insert(entity, entityInstance, modelValueArray);
    }
  }

  protected void update(
      MetaEntity entity,
      Object entityInstance,
      ModelValueArray<MetaAttribute> modelValueArray)
      throws Exception {
    // It's an update.
    if (modelValueArray.isEmpty()) {
      return;
    }

    MetaEntityHelper.createVersionAttributeArrayEntry(entity, entityInstance, modelValueArray);
    List<QueryParameter> parameters = MetaEntityHelper.convertAVToQP(modelValueArray);
    List<String> columns = parameters.stream().map(QueryParameter::getColumnName)
        .collect(Collectors.toList());

    Object idValue = AttributeUtil.getIdValue(entity, entityInstance);
    LOG.debug("update: idValue={}", idValue);
    List<QueryParameter> idParameters = MetaEntityHelper.convertAVToQP(entity.getId(), idValue);
    List<String> idColumns = idParameters.stream().map(p -> p.getColumnName())
        .collect(Collectors.toList());
    if (MetaEntityHelper.hasOptimisticLock(entity, entityInstance)) {
      idColumns.add(entity.getVersionAttribute().get().getColumnName());
    }

    parameters.addAll(MetaEntityHelper.convertAVToQP(entity.getId().getAttribute(), idValue));
    if (MetaEntityHelper.hasOptimisticLock(entity, entityInstance)) {
      Object currentVersionValue = entity.getVersionAttribute().get().getReadMethod()
          .invoke(entityInstance);
      parameters.addAll(
          MetaEntityHelper.convertAVToQP(entity.getVersionAttribute().get(), currentVersionValue));
    }

    int updateCount = jdbcQueryRunner.update(entity, parameters, columns, idColumns);
    if (updateCount == 0) {
      if (entity.getVersionAttribute().isPresent()) {
        Object currentVersionValue = entity.getVersionAttribute().get().getReadMethod()
            .invoke(entityInstance);
        throw new OptimisticLockException(
            "Entity was written by another transaction, version: " + currentVersionValue);
      }
    }

    LOG.debug("update: updateCount={}", updateCount);
    MetaEntityHelper.updateVersionAttributeValue(entity, entityInstance);
  }

  protected void insert(
      MetaEntity entity,
      Object entityInstance,
      ModelValueArray<MetaAttribute> modelValueArray)
      throws Exception {
    Pk pk = entity.getId();
//  LOG.info("persist: id.getPkGeneration()={}", id.getPkGeneration());
    PkStrategy pkStrategy = pk.getPkGeneration().getPkStrategy();
//  LOG.info("Primary Key Generation Strategy: " + pkStrategy);
    if (pkStrategy == PkStrategy.IDENTITY) {
      int pkIndex = modelValueArray.indexOfModel(pk.getAttribute());
      boolean isIdentityColumnNull = pkIndex == -1;
      List<QueryParameter> parameters = MetaEntityHelper.convertAVToQP(modelValueArray);
      // version attribute
      Optional<QueryParameter> optVersion = MetaEntityHelper.generateVersionParameter(entity);
      if (optVersion.isPresent()) {
        parameters.add(optVersion.get());
      }

      Object pkId = jdbcQueryRunner.insertWithIdentityColumn(entity, entityInstance, parameters,
          isIdentityColumnNull);

      LOG.info("persist: pk={}", pkId);
      if (pkId != null) {
        LOG.info("persist: pkId.getClass()={}", pkId.getClass());
      }

      Object idv = entity.getId().convertGeneratedKey(pkId);
      entity.getId().getWriteMethod().invoke(entityInstance, idv);
      if (optVersion.isPresent()) {
        entity.getVersionAttribute().get().getWriteMethod()
            .invoke(entityInstance, optVersion.get().getValue());
      }

      updatePostponedJoinColumnUpdate(entity, entityInstance);
    } else {
      Object idValue = pk.getReadMethod().invoke(entityInstance);
      List<QueryParameter> idParameters = MetaEntityHelper.convertAVToQP(pk, idValue);
      List<QueryParameter> parameters = MetaEntityHelper.convertAVToQP(modelValueArray);
      parameters.addAll(0, idParameters);
      // version attribute
      Optional<QueryParameter> optVersion = MetaEntityHelper.generateVersionParameter(entity);
      if (optVersion.isPresent()) {
        parameters.add(optVersion.get());
      }

      jdbcQueryRunner.insert(entity, entityInstance, parameters);
      if (optVersion.isPresent()) {
        entity.getVersionAttribute().get().getWriteMethod()
            .invoke(entityInstance, optVersion.get().getValue());
      }
    }
  }

  private void updatePostponedJoinColumnUpdate(MetaEntity entity, Object entityInstance)
      throws Exception {
    LOG.debug("updatePostponedJoinColumnUpdate: entity.getName()={}", entity.getName());
    LOG.debug(
        "updatePostponedJoinColumnUpdate: entity.getJoinColumnPostponedUpdateAttributeReadMethod().isEmpty()={}",
        entity.getJoinColumnPostponedUpdateAttributeReadMethod().isEmpty());

    List list = MetaEntityHelper.getJoinColumnPostponedUpdateAttributeList(entity, entityInstance);
    LOG.debug("updatePostponedJoinColumnUpdate: list.isEmpty()={}", list.isEmpty());
    if (list.isEmpty()) {
      return;
    }

    for (Object o : list) {
      PostponedUpdateInfo postponedUpdateInfo = (PostponedUpdateInfo) o;
      LOG.debug("updatePostponedJoinColumnUpdate: postponedUpdateInfo.getC()={}",
          postponedUpdateInfo.getC());
      Object instance = entityContainer.find(postponedUpdateInfo.getC(),
          postponedUpdateInfo.getId());
      MetaEntity toEntity = persistenceUnitContext.getEntities()
          .get(postponedUpdateInfo.getC().getName());
      LOG.debug("updatePostponedJoinColumnUpdate: toEntity={}", toEntity);
      LOG.debug("updatePostponedJoinColumnUpdate: postponedUpdateInfo.getAttributeName()={}",
          postponedUpdateInfo.getAttributeName());
      Optional<MetaAttribute> optional = toEntity
          .findJoinColumnMappingAttribute(postponedUpdateInfo.getAttributeName());
      LOG.debug("updatePostponedJoinColumnUpdate: optional.isEmpty()={}", optional.isEmpty());
      if (optional.isEmpty()) {
        continue;
      }

      ModelValueArray<MetaAttribute> modelValueArray = new ModelValueArray<>();
      modelValueArray.add(optional.get(), entityInstance);
      update(toEntity, instance, modelValueArray);
    }

    list.clear();
  }

  @Override
  public void persistJoinTableAttributes(MetaEntity entity, Object entityInstance)
      throws Exception {
    Object idValue = AttributeUtil.getIdValue(entity, entityInstance);
    List<QueryParameter> idParameters = MetaEntityHelper.convertAVToQP(entity.getId(), idValue);

    for (MetaAttribute a : entity.getRelationshipAttributes()) {
      if (a.getRelationship().getJoinTable() != null && a.getRelationship().isOwner()) {
        // removes the join table records first
        jdbcQueryRunner.removeJoinTableRecords(entity, idValue, idParameters,
            a.getRelationship().getJoinTable());

        Object attributeInstance = MetaEntityHelper.getAttributeValue(entityInstance, a);
        if (attributeInstance != null && CollectionUtils.isCollectionClass(
            attributeInstance.getClass())
            && !CollectionUtils.isCollectionEmpty(attributeInstance)) {
          Collection<?> ees = CollectionUtils.getCollectionFromCollectionOrMap(attributeInstance);
          LOG.debug("persistJoinTableAttributes: ees={}", ees);
          if (entityContainer.isManaged(ees)) {
            persistJoinTableAttributes(ees, a, entityInstance);
          }
        }
      }
    }
  }

  private void persistJoinTableAttributes(Collection<?> ees, MetaAttribute a, Object entityInstance)
      throws Exception {
    // persist every entity instance
    RelationshipJoinTable relationshipJoinTable = a.getRelationship().getJoinTable();
    for (Object instance : ees) {
      jdbcQueryRunner.insertJoinTableAttribute(relationshipJoinTable, entityInstance, instance);
    }
  }

  @Override
  public void removeJoinTableRecords(Object entityInstance, MetaEntity e) throws Exception {
    Object idValue = AttributeUtil.getIdValue(e, entityInstance);
    List<QueryParameter> idParameters = MetaEntityHelper.convertAVToQP(e.getId(), idValue);
    Set<RelationshipJoinTable> relationshipJoinTables = e.getRelationshipAttributes().stream()
        .map(MetaAttribute::getRelationship)
        .filter(r -> r.getJoinTable() != null && r.getJoinTable().getOwningEntity() == e)
        .map(Relationship::getJoinTable)
        .collect(Collectors.toSet());
    for (RelationshipJoinTable relationshipJoinTable : relationshipJoinTables) {
      jdbcQueryRunner.removeJoinTableRecords(e, idValue, idParameters,
          relationshipJoinTable);
    }
  }

  @Override
  public void delete(Object entityInstance, MetaEntity e) throws Exception {
    Object idValue = AttributeUtil.getIdValue(e, entityInstance);
    LOG.debug("delete: idValue={}", idValue);

    List<QueryParameter> idParameters = MetaEntityHelper.convertAVToQP(e.getId(), idValue);
    if (MetaEntityHelper.hasOptimisticLock(e, entityInstance)) {
      Object currentVersionValue = e.getVersionAttribute().get().getReadMethod()
          .invoke(entityInstance);
      idParameters.addAll(
          MetaEntityHelper.convertAVToQP(e.getVersionAttribute().get(), currentVersionValue));
    }

    jdbcQueryRunner.deleteById(e, idParameters);
  }
}
