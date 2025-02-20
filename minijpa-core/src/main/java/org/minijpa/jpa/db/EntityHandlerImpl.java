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
import org.minijpa.jpa.model.AbstractMetaAttribute;
import org.minijpa.jpa.model.MetaAttribute;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.jpa.model.Pk;
import org.minijpa.jpa.model.RelationshipMetaAttribute;
import org.minijpa.jpa.model.relationship.JoinColumnMapping;
import org.minijpa.jpa.model.relationship.Relationship;
import org.minijpa.jpa.model.relationship.RelationshipJoinTable;
import org.minijpa.jpa.model.relationship.ToManyRelationship;
import org.minijpa.metadata.PersistenceUnitContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author adamato
 */
public class EntityHandlerImpl implements EntityHandler {

    private final Logger log = LoggerFactory.getLogger(EntityHandlerImpl.class);
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
        if (entityInstance != null)
            return entityInstance;

//	LOG.debug("findById: metaEntity={}", metaEntity);
//	LOG.debug("findById: primaryKey={}", primaryKey);
        Optional<ModelValueArray<FetchParameter>> optional = jdbcQueryRunner.findById(metaEntity,
                primaryKey, lockType);
        if (optional.isEmpty())
            return null;

        ModelValueArray<FetchParameter> modelValueArray = optional.get();

//	for (int i = 0; i < modelValueArray.size(); ++i) {
//	    LOG.debug("findById: modelValueArray.getModel(i).getAttribute()={}", modelValueArray.getModel(i).getAttribute());
//	    LOG.debug("findById: modelValueArray.getValue(i)={}", modelValueArray.getValue(i));
//	}
//        entityInstance = MetaEntityHelper.build(metaEntity, primaryKey);
        entityInstance = MetaEntityHelper.build(metaEntity);
        buildAttributeValuesLoadFK(entityInstance, primaryKey, metaEntity, metaEntity.getBasicAttributes(),
                metaEntity.getRelationshipAttributes(),
                modelValueArray, lockType);
        metaEntity.getId().writeValue(entityInstance, primaryKey);
        entityContainer.addManaged(entityInstance, primaryKey);
        MetaEntityHelper.setEntityStatus(metaEntity, entityInstance,
                EntityStatus.FLUSHED_LOADED_FROM_DB);
        fillCircularRelationships(metaEntity, entityInstance);
        return entityInstance;
    }

//    @Override
//    public Object queryVersionValue(MetaEntity metaEntity, Object primaryKey, LockType lockType)
//            throws Exception {
//        Optional<ModelValueArray<FetchParameter>> optional = jdbcQueryRunner.runVersionQuery(metaEntity,
//                primaryKey,
//                lockType);
//        if (optional.isEmpty()) {
//            return null;
//        }
//
//        ModelValueArray<FetchParameter> modelValueArray = optional.get();
//        return modelValueArray.getValue(0);
//    }

    @Override
    public void refresh(
            MetaEntity metaEntity,
            Object entityInstance,
            Object primaryKey,
            LockType lockType) throws Exception {
        Optional<ModelValueArray<FetchParameter>> optional = jdbcQueryRunner.findById(metaEntity,
                primaryKey, lockType);
        if (optional.isEmpty()) {
            throw new EntityNotFoundException(
                    "Entity '" + entityInstance + "' not found: pk=" + primaryKey);
        }

        ModelValueArray<FetchParameter> modelValueArray = optional.get();
        buildAttributeValuesLoadFK(entityInstance, primaryKey, metaEntity, metaEntity.getBasicAttributes(),
                metaEntity.getRelationshipAttributes(),
                modelValueArray, lockType);
        MetaEntityHelper.setEntityStatus(metaEntity, entityInstance,
                EntityStatus.FLUSHED_LOADED_FROM_DB);
        fillCircularRelationships(metaEntity, entityInstance);
        MetaEntityHelper.removeChanges(metaEntity, entityInstance);
        MetaEntityHelper.clearLazyAttributeLoaded(metaEntity, entityInstance);
    }

    private void fillCircularRelationships(
            MetaEntity entity,
            Object entityInstance)
            throws Exception {
        log.debug("fillCircularRelationships: entity={}", entity);
        log.debug("fillCircularRelationships: entityInstance={}", entityInstance);
        for (RelationshipMetaAttribute a : entity.getRelationshipAttributes()) {
            if (!a.isEager()) {
                continue;
            }

            if (a.getRelationship().toOne() && a.getRelationship().isOwner()) {
                log.debug("fillCircularRelationships: a={}", a);
                Object value = MetaEntityHelper.getAttributeValue(entityInstance, a);
                log.debug("fillCircularRelationships: value={}", value);
                if (value == null) {
                    continue;
                }

                RelationshipMetaAttribute targetAttribute = a.getRelationship().getTargetAttribute();
                log.debug("fillCircularRelationships: targetAttribute={}", targetAttribute);
                log.debug("fillCircularRelationships: a.getRelationship().getAttributeType()={}",
                        a.getRelationship().getAttributeType());
                MetaEntity toEntity = a.getRelationship().getAttributeType();
                if (toEntity == null) {
                    continue;
                }

                RelationshipMetaAttribute attribute = toEntity.findAttributeByMappedBy(a.getName());
                log.debug("fillCircularRelationships: attribute={}", attribute);
                if (attribute == null) {
                    continue;
                }

                // it's bidirectional
                log.debug("fillCircularRelationships: attribute.getRelationship().toOne()={}", attribute.getRelationship().toOne());
                if (attribute.getRelationship().toOne()) {
                    Object v = MetaEntityHelper.getAttributeValue(value, attribute);
                    log.debug("fillCircularRelationships: v={}", v);
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
            MetaEntity entity) throws Exception {
        Object primaryKey = entity.getId().buildValue(modelValueArray);
        Object entityInstance = entityContainer.find(entity.getEntityClass(), primaryKey);
        if (entityInstance != null)
            return entityInstance;

        Object entityInstanceNew = MetaEntityHelper.build(entity);
        log.debug("build: entityInstanceNew={}", entityInstanceNew);
        buildAttributeValuesLoadFK(entityInstanceNew, primaryKey, entity, entity.getBasicAttributes(),
                entity.getRelationshipAttributes(), modelValueArray,
                lockType);
        entity.getId().writeValue(entityInstanceNew, primaryKey);
        entityContainer.addManaged(entityInstanceNew, primaryKey);
        MetaEntityHelper.setEntityStatus(entity, entityInstanceNew, EntityStatus.FLUSHED_LOADED_FROM_DB);
        fillCircularRelationships(entity, entityInstanceNew);
        return entityInstanceNew;
    }


    private void buildAttributeValuesLoadFK(
            Object parentInstance,
            Object parentInstancePk,
            MetaEntity metaEntity,
            List<MetaAttribute> attributes,
            List<RelationshipMetaAttribute> relationshipMetaAttributes,
            ModelValueArray<FetchParameter> modelValueArray,
            LockType lockType) throws Exception {
        // basic attributes and relationship attributes
        for (MetaAttribute attribute : attributes) {
            buildBasicAttribute(attribute, parentInstance, metaEntity, modelValueArray);
        }

        for (RelationshipMetaAttribute attribute : relationshipMetaAttributes) {
            loadJoinTableRelationships(parentInstance, parentInstancePk, metaEntity, attribute, lockType);
        }

//	LOG.debug("buildAttributeValuesLoadFK: metaEntity.getEmbeddables()={}", metaEntity.getEmbeddables());
        // embeddables
        for (MetaEntity embeddable : metaEntity.getEmbeddables()) {
            Object parent = embeddable.getEntityClass().getDeclaredConstructor().newInstance();
            buildAttributeValuesLoadFK(parent, parentInstancePk, embeddable, embeddable.getBasicAttributes(),
                    embeddable.getRelationshipAttributes(), modelValueArray,
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
//            Object fk = AttributeUtil.buildPK(joinColumnMapping.getForeignKey(), modelValueArray);
            Object fk = joinColumnMapping.getForeignKey().buildValue(modelValueArray);
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


    private void buildAttributeValuesNoRelationshipLoading(
            Object parentInstance,
            MetaEntity metaEntity,
            List<MetaAttribute> attributes,
            ModelValueArray<FetchParameter> modelValueArray,
            LockType lockType) throws Exception {
        // basic attributes and relationship attributes
        for (MetaAttribute attribute : attributes) {
            buildBasicAttribute(attribute, parentInstance, metaEntity, modelValueArray);
        }

//	LOG.debug("buildAttributeValues: metaEntity.getEmbeddables()={}", metaEntity.getEmbeddables());
        // load embeddables
        for (MetaEntity embeddable : metaEntity.getEmbeddables()) {
            Object parent = embeddable.getEntityClass().getDeclaredConstructor().newInstance();
            buildAttributeValuesNoRelationshipLoading(parent, embeddable, embeddable.getBasicAttributes(),
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
            Object fk = joinColumnMapping.getForeignKey().buildValue(modelValueArray);
            if (joinColumnMapping.isLazy()) {
                MetaEntityHelper.setForeignKeyValue(joinColumnMapping.getAttribute(), parentInstance, fk);
                continue;
            }

            MetaEntity toEntity = joinColumnMapping.getAttribute().getRelationship().getAttributeType();
//	    LOG.debug("buildAttributeValues: toEntity={}", toEntity);
            Object parent = buildEntityByValuesNoRelationshipAttributeLoading(
                    modelValueArray,
                    toEntity,
                    lockType);
            MetaEntityHelper.writeMetaAttributeValue(parentInstance, parentInstance.getClass(),
                    joinColumnMapping.getAttribute(), parent, metaEntity);
        }
    }

    private Object buildEntityByValuesNoRelationshipAttributeLoading(
            ModelValueArray<FetchParameter> modelValueArray,
            MetaEntity entity,
            LockType lockType) throws Exception {
        Object primaryKey = entity.getId().buildValue(modelValueArray);
        log.debug("buildEntityByValuesNoRelationshipAttributeLoading: primaryKey={}", primaryKey);
        log.debug("buildEntityByValuesNoRelationshipAttributeLoading: entity={}", entity);
        Object entityInstance = entityContainer.find(entity.getEntityClass(), primaryKey);
        log.debug("buildEntityByValuesNoRelationshipAttributeLoading: entityInstance={}", entityInstance);
        if (entityInstance != null)
            return entityInstance;

        entityInstance = MetaEntityHelper.build(entity, primaryKey);
        buildAttributeValuesNoRelationshipLoading(entityInstance, entity, entity.getBasicAttributes(),
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
            Object parentInstancePk,
            MetaEntity entity,
            RelationshipMetaAttribute attribute,
            LockType lockType) throws Exception {
        if (!attribute.isEager()) {
            return;
        }

        if (attribute.getRelationship().getJoinTable() == null) {
            return;
        }

        if (attribute.getRelationship().isOwner()) {
//            Object pk = entity.getId().readValue(parentInstance);
            Object result = jdbcQueryRunner.selectByJoinTable(parentInstancePk, entity.getId(),
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
            AbstractMetaAttribute foreignKeyAttribute,
            Object foreignKeyValue,
            LockType lockType)
            throws Exception {
        // foreign key on the same table
        log.debug("loadRelationshipByForeignKey: foreignKeyAttribute={}; foreignKeyValue={}",
                foreignKeyAttribute,
                foreignKeyValue);
        log.debug("loadRelationshipByForeignKey: parentInstance={}", parentInstance);
        MetaEntity e = persistenceUnitContext.getEntities()
                .get(foreignKeyAttribute.getType().getName());
        log.debug("loadRelationshipByForeignKey: e={}", e);
        Object foreignKeyInstance = findById(e, foreignKeyValue, lockType);
        log.debug("loadRelationshipByForeignKey: foreignKeyInstance={}", foreignKeyInstance);
        log.debug("loadRelationshipByForeignKey: parentInstance={}", parentInstance);
        log.debug("loadRelationshipByForeignKey: foreignKeyAttribute={}; foreignKeyValue={}",
                foreignKeyAttribute,
                foreignKeyValue);
        if (foreignKeyInstance != null) {
            MetaEntityHelper.writeAttributeValue(entity, parentInstance, foreignKeyAttribute,
                    foreignKeyInstance);
//	    entityInstanceBuilder.writeMetaAttributeValue(
//		    parentInstance, parentInstance.getClass(), foreignKeyAttribute, foreignKeyInstance, entity);

            RelationshipMetaAttribute a = e.findAttributeByMappedBy(foreignKeyAttribute.getName());
            log.debug("loadRelationshipByForeignKey: a={}", a);
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
            AbstractMetaAttribute a,
            Object currentValue)
            throws Exception {
        if (a instanceof MetaAttribute)
            return null;

        RelationshipMetaAttribute relationshipMetaAttribute = (RelationshipMetaAttribute) a;
        Relationship relationship = relationshipMetaAttribute.getRelationship();
        log.debug("loadAttribute: a={}", a);
        log.debug("loadAttribute: currentValue={}", currentValue);
        log.debug("loadAttribute: parentInstance={}", parentInstance);
        log.debug("loadAttribute: relationship={}", relationship);

        log.debug("loadAttribute: relationship.getTargetAttribute()={}", relationship.getTargetAttribute());
        if (!relationship.toMany()) {
            MetaEntity entity = persistenceUnitContext.getEntities()
                    .get(parentInstance.getClass().getName());
            Object foreignKey = MetaEntityHelper.getForeignKeyValue(relationshipMetaAttribute, parentInstance);
            log.debug("loadAttribute: foreignKey={}", foreignKey);
            return loadRelationshipByForeignKey(parentInstance, entity, a, foreignKey,
                    LockType.NONE);
        }

        log.debug("loadAttribute: to Many relationship.getJoinTable()={}",
                relationship.getJoinTable());
        if (relationship.getJoinTable() == null) {
            MetaEntity entity = persistenceUnitContext.getEntities()
                    .get(relationship.getTargetEntityClass().getName());
            log.debug("loadAttribute: entity={}", entity);
            if (entity == null) {
                throw new IllegalArgumentException(
                        "Class '" + relationship.getTargetEntityClass().getName() + "' is not an entity");
            }

            log.debug("loadAttribute: relationship.getOwningAttribute()={}",
                    relationship.getOwningAttribute());
            return jdbcQueryRunner.selectByForeignKey(
                    entity,
                    relationship.getOwningAttribute(),
                    parentInstance,
                    ((ToManyRelationship) relationship).getCollectionClass(),
                    LockType.NONE,
                    this);
        }

        MetaEntity e = persistenceUnitContext.getEntities().get(parentInstance.getClass().getName());
        Object pk = e.getId().readValue(parentInstance);
        log.debug("loadAttribute: pk={}", pk);
        return jdbcQueryRunner.selectByJoinTable(pk, e.getId(), relationship, relationshipMetaAttribute, this);
    }

    @Override
    public void persist(
            MetaEntity entity,
            Object entityInstance,
            ModelValueArray<AbstractMetaAttribute> modelValueArray)
            throws Exception {
        EntityStatus entityStatus = MetaEntityHelper.getEntityStatus(entity, entityInstance);
        log.debug("persist: entityStatus={}", entityStatus);
        if (MetaEntityHelper.isFlushed(entity, entityInstance)) {
            update(entity, entityInstance, modelValueArray);
        } else {
            insert(entity, entityInstance, modelValueArray);
        }
    }

    protected void update(
            MetaEntity entity,
            Object entityInstance,
            ModelValueArray<AbstractMetaAttribute> modelValueArray)
            throws Exception {
        // It's an update.
        if (modelValueArray.isEmpty()) {
            return;
        }

        MetaEntityHelper.createVersionAttributeArrayEntry(entity, entityInstance, modelValueArray);
        List<QueryParameter> parameters = MetaEntityHelper.convertAVToQP(modelValueArray);
        List<String> columns = parameters.stream().map(p -> (String) p.getColumn())
                .collect(Collectors.toList());

        Object idValue = entity.getId().readValue(entityInstance);
        log.debug("update: idValue={}", idValue);
//        List<QueryParameter> idParameters = MetaEntityHelper.convertAVToQP(entity.getId(), idValue);
        List<QueryParameter> idParameters = entity.getId().queryParameters(idValue);
        List<String> idColumns = idParameters.stream().map(p -> (String) p.getColumn())
                .collect(Collectors.toList());
        if (MetaEntityHelper.hasOptimisticLock(entity, entityInstance)) {
            idColumns.add(entity.getVersionMetaAttribute().getColumnName());
        }

        parameters.add(entity.getId().getAttribute().queryParameter(idValue));
        if (MetaEntityHelper.hasOptimisticLock(entity, entityInstance)) {
            Object currentVersionValue = entity.getVersionMetaAttribute().getReadMethod()
                    .invoke(entityInstance);
            parameters.add(
                    entity.getVersionMetaAttribute().queryParameter(currentVersionValue));
        }

        int updateCount = jdbcQueryRunner.update(entity, parameters, columns, idColumns);
        if (updateCount == 0) {
            if (entity.getVersionMetaAttribute() != null) {
                Object currentVersionValue = entity.getVersionMetaAttribute().getReadMethod()
                        .invoke(entityInstance);
                throw new OptimisticLockException(
                        "Entity was written by another transaction, version: " + currentVersionValue);
            }
        }

        log.debug("update: updateCount={}", updateCount);
        MetaEntityHelper.updateVersionAttributeValue(entity, entityInstance);
    }

    protected void insert(
            MetaEntity entity,
            Object entityInstance,
            ModelValueArray<AbstractMetaAttribute> modelValueArray)
            throws Exception {
        Pk pk = entity.getId();
        PkStrategy pkStrategy = pk.getPkGeneration().getPkStrategy();
        if (pkStrategy == PkStrategy.IDENTITY) {
            int pkIndex = modelValueArray.indexOfModel(pk.getAttribute());
            boolean isIdentityColumnNull = pkIndex == -1;
            List<QueryParameter> parameters = MetaEntityHelper.convertAVToQP(modelValueArray);
            // version attribute
            Optional<QueryParameter> optVersion = MetaEntityHelper.generateVersionParameter(entity);
            optVersion.ifPresent(parameters::add);

            Object pkId = jdbcQueryRunner.insertWithIdentityColumn(
                    entity,
                    entityInstance,
                    parameters,
                    isIdentityColumnNull);

            log.debug("insert: pk={}", pkId);
            if (pkId != null) {
                log.debug("insert: pkId.getClass()={}", pkId.getClass());
            }

            Object idv = entity.getId().convertGeneratedKey(pkId);
            entity.getId().writeValue(entityInstance, idv);
            if (optVersion.isPresent()) {
                entity.getVersionMetaAttribute().getWriteMethod()
                        .invoke(entityInstance, optVersion.get().getValue());
            }

            updatePostponedJoinColumnUpdate(entity, entityInstance);
        } else {
            Object idValue = pk.readValue(entityInstance);
            List<QueryParameter> idParameters = pk.queryParameters(idValue);
            log.debug("insert: idParameters={}", idParameters);
            List<QueryParameter> parameters = MetaEntityHelper.convertAVToQP(modelValueArray);
            log.debug("insert: parameters={}", parameters);
            parameters.addAll(0, idParameters);
            // version attribute
            Optional<QueryParameter> optVersion = MetaEntityHelper.generateVersionParameter(entity);
            optVersion.ifPresent(parameters::add);

            jdbcQueryRunner.insert(entity, entityInstance, parameters);
            if (optVersion.isPresent()) {
                entity.getVersionMetaAttribute().getWriteMethod()
                        .invoke(entityInstance, optVersion.get().getValue());
            }
        }
    }

    private void updatePostponedJoinColumnUpdate(MetaEntity entity, Object entityInstance)
            throws Exception {
        log.debug("updatePostponedJoinColumnUpdate: entity.getName()={}", entity.getName());
        log.debug(
                "updatePostponedJoinColumnUpdate: entity.getJoinColumnPostponedUpdateAttributeReadMethod()={}",
                entity.getJoinColumnPostponedUpdateAttributeReadMethod());

        List list = MetaEntityHelper.getJoinColumnPostponedUpdateAttributeList(entity, entityInstance);
        log.debug("updatePostponedJoinColumnUpdate: list.isEmpty()={}", list.isEmpty());
        if (list.isEmpty()) {
            return;
        }

        for (Object o : list) {
            PostponedUpdateInfo postponedUpdateInfo = (PostponedUpdateInfo) o;
            log.debug("updatePostponedJoinColumnUpdate: postponedUpdateInfo.getC()={}",
                    postponedUpdateInfo.getC());
            Object instance = entityContainer.find(postponedUpdateInfo.getC(),
                    postponedUpdateInfo.getId());
            MetaEntity toEntity = persistenceUnitContext.getEntities()
                    .get(postponedUpdateInfo.getC().getName());
            log.debug("updatePostponedJoinColumnUpdate: toEntity={}", toEntity);
            log.debug("updatePostponedJoinColumnUpdate: postponedUpdateInfo.getAttributeName()={}",
                    postponedUpdateInfo.getAttributeName());
            Optional<RelationshipMetaAttribute> optional = toEntity
                    .findJoinColumnMappingAttribute(postponedUpdateInfo.getAttributeName());
            log.debug("updatePostponedJoinColumnUpdate: optional.isEmpty()={}", optional.isEmpty());
            if (optional.isEmpty()) {
                continue;
            }

            ModelValueArray<AbstractMetaAttribute> modelValueArray = new ModelValueArray<>();
            modelValueArray.add(optional.get(), entityInstance);
            update(toEntity, instance, modelValueArray);
        }

        list.clear();
    }

    @Override
    public void persistJoinTableAttributes(MetaEntity entity, Object entityInstance)
            throws Exception {
        Object idValue = entity.getId().readValue(entityInstance);
//        List<QueryParameter> idParameters = MetaEntityHelper.convertAVToQP(entity.getId(), idValue);
        List<QueryParameter> idParameters = entity.getId().queryParameters(idValue);

        for (RelationshipMetaAttribute a : entity.getRelationshipAttributes()) {
            if (a.getRelationship().getJoinTable() != null && a.getRelationship().isOwner()) {
                Object attributeInstance = MetaEntityHelper.getAttributeValue(entityInstance, a);
                if (attributeInstance != null && CollectionUtils.isCollectionClass(
                        attributeInstance.getClass())
                        && !CollectionUtils.isCollectionEmpty(attributeInstance)) {
                    Collection<?> ees = CollectionUtils.getCollectionFromCollectionOrMap(attributeInstance);
                    log.debug("persistJoinTableAttributes: ees={}", ees);
                    if (entityContainer.isManaged(ees) && !ees.isEmpty()) {
                        // removes the join table records first
                        jdbcQueryRunner.removeJoinTableRecords(entity, idValue, idParameters,
                                a.getRelationship().getJoinTable());
                        persistJoinTableAttributes(ees, a, entityInstance);
                    }
                }
            }
        }
    }

    private void persistJoinTableAttributes(
            Collection<?> ees,
            RelationshipMetaAttribute a,
            Object entityInstance)
            throws Exception {
        // persist every entity instance
        RelationshipJoinTable relationshipJoinTable = a.getRelationship().getJoinTable();
        for (Object instance : ees) {
            jdbcQueryRunner.insertJoinTableAttribute(relationshipJoinTable, entityInstance, instance);
        }
    }

    @Override
    public void removeJoinTableRecords(Object entityInstance, MetaEntity e) throws Exception {
        if (e.getRelationshipAttributes().isEmpty())
            return;

        Object idValue = e.getId().readValue(entityInstance);
//        List<QueryParameter> idParameters = MetaEntityHelper.convertAVToQP(e.getId(), idValue);
        List<QueryParameter> idParameters = e.getId().queryParameters(idValue);

        Set<RelationshipJoinTable> relationshipJoinTables = e.getRelationshipAttributes().stream()
                .map(RelationshipMetaAttribute::getRelationship)
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
        Object idValue = e.getId().readValue(entityInstance);
        log.debug("delete: idValue={}", idValue);

//        List<QueryParameter> idParameters = MetaEntityHelper.convertAVToQP(e.getId(), idValue);
        List<QueryParameter> idParameters = e.getId().queryParameters(idValue);
        if (MetaEntityHelper.hasOptimisticLock(e, entityInstance)) {
            Object currentVersionValue = e.getVersionMetaAttribute().getReadMethod()
                    .invoke(entityInstance);
            idParameters.add(e.getVersionMetaAttribute().queryParameter(currentVersionValue));
        }

        jdbcQueryRunner.deleteById(e, idParameters);
    }
}
