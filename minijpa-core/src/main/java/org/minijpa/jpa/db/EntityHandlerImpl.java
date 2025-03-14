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

import org.minijpa.jdbc.FetchParameter;
import org.minijpa.jdbc.ModelValueArray;
import org.minijpa.jdbc.QueryParameter;
import org.minijpa.jpa.MetaEntityHelper;
import org.minijpa.jpa.model.*;
import org.minijpa.jpa.model.relationship.JoinColumnMapping;
import org.minijpa.jpa.model.relationship.Relationship;
import org.minijpa.jpa.model.relationship.RelationshipJoinTable;
import org.minijpa.jpa.model.relationship.ToManyRelationship;
import org.minijpa.metadata.PersistenceUnitContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityNotFoundException;
import javax.persistence.OptimisticLockException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

        Optional<ModelValueArray<FetchParameter>> optional = jdbcQueryRunner.findById(metaEntity,
                primaryKey, lockType);
        if (optional.isEmpty())
            return null;

        ModelValueArray<FetchParameter> modelValueArray = optional.get();

        entityInstance = metaEntity.buildInstance();
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
        metaEntity.clearModificationAttributes(entityInstance);
        metaEntity.clearLazyAttributeLoaded(entityInstance);
    }

    private void fillCircularRelationships(
            MetaEntity entity,
            Object entityInstance)
            throws Exception {
        log.debug("Building Relationships -> Entity {}", entity);
        log.debug("Building Relationships -> Entity Instance {}", entityInstance);
        for (RelationshipMetaAttribute a : entity.getRelationshipAttributes()) {
            if (!a.isEager()) {
                continue;
            }

            if (a.getRelationship().toOne() && a.getRelationship().isOwner()) {
                log.debug("Building Relationships -> Attribute {}", a);
                Object value = a.getValue(entityInstance);
                log.debug("Building Relationships -> Value {}", value);
                if (value == null) {
                    continue;
                }

                RelationshipMetaAttribute targetAttribute = a.getRelationship().getTargetAttribute();
                log.debug("Building Relationships -> Target Attribute {}", targetAttribute);
                log.debug("Building Relationships -> Attribute Type {}",
                        a.getRelationship().getAttributeType());
                MetaEntity toEntity = a.getRelationship().getAttributeType();
                if (toEntity == null) {
                    continue;
                }

                RelationshipMetaAttribute attribute = toEntity.findAttributeByMappedBy(a.getName());
                log.debug("Building Relationships -> Target Entity Attribute {}", attribute);
                if (attribute == null) {
                    continue;
                }

                // it's bidirectional
                log.debug("Building Relationships -> Is To One {}", attribute.getRelationship().toOne());
                if (attribute.getRelationship().toOne()) {
                    Object v = attribute.getValue(value);
                    log.debug("Building Relationships -> Target Entity Attribute Value {}", v);
                    if (v == null) {
                        toEntity.writeAttributeValue(value, value.getClass(), attribute,
                                entityInstance);
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

        Object entityInstanceNew = entity.buildInstance();
        log.debug("Building Entity Instance -> Entity Instance {}", entityInstanceNew);
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

        // embeddables
        for (MetaEntity embeddable : metaEntity.getEmbeddables()) {
            Object parent = embeddable.getEntityClass().getDeclaredConstructor().newInstance();
            buildAttributeValuesLoadFK(parent, parentInstancePk, embeddable, embeddable.getBasicAttributes(),
                    embeddable.getRelationshipAttributes(), modelValueArray,
                    lockType);
            metaEntity.writeEmbeddableValue(parentInstance, parentInstance.getClass(), embeddable,
                    parent);
        }

        // join columns
        for (JoinColumnMapping joinColumnMapping : metaEntity.getJoinColumnMappings()) {
            log.debug("Building Relationships -> Join Column Mapping Attribute {}", joinColumnMapping.getAttribute());
            log.debug("Building Relationships -> Join Column Mapping Foreign Key {}", joinColumnMapping.getForeignKey());
            Object fk = joinColumnMapping.getForeignKey().buildValue(modelValueArray);
            if (joinColumnMapping.isLazy()) {
                joinColumnMapping.getAttribute().setForeignKeyValue(parentInstance, fk);
                continue;
            }

            Object parent = loadRelationshipByForeignKey(parentInstance, metaEntity,
                    joinColumnMapping.getAttribute(),
                    fk, lockType);
            metaEntity.writeAttributeValue(parentInstance, parentInstance.getClass(),
                    joinColumnMapping.getAttribute(), parent);
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

        metaEntity.writeAttributeValue(
                parentInstance,
                parentInstance.getClass(),
                attribute,
                modelValueArray.getValue(index));
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

        // load embeddables
        for (MetaEntity embeddable : metaEntity.getEmbeddables()) {
            Object parent = embeddable.getEntityClass().getDeclaredConstructor().newInstance();
            buildAttributeValuesNoRelationshipLoading(parent, embeddable, embeddable.getBasicAttributes(),
                    modelValueArray,
                    lockType);
            metaEntity.writeEmbeddableValue(parentInstance, parentInstance.getClass(), embeddable,
                    parent);
        }

        // attributes with join columns
        for (JoinColumnMapping joinColumnMapping : metaEntity.getJoinColumnMappings()) {
            log.debug("Building Relationships -> Join Column Mapping Attribute {}", joinColumnMapping.getAttribute());
            log.debug("Building Relationships -> Join Column Mapping Foreign Key {}", joinColumnMapping.getForeignKey());
            Object fk = joinColumnMapping.getForeignKey().buildValue(modelValueArray);
            if (joinColumnMapping.isLazy()) {
                joinColumnMapping.getAttribute().setForeignKeyValue(parentInstance, fk);
                continue;
            }

            MetaEntity toEntity = joinColumnMapping.getAttribute().getRelationship().getAttributeType();
            Object parent = buildEntityByValuesNoRelationshipAttributeLoading(
                    modelValueArray,
                    toEntity,
                    lockType);
            metaEntity.writeAttributeValue(parentInstance, parentInstance.getClass(),
                    joinColumnMapping.getAttribute(), parent);
        }
    }

    private Object buildEntityByValuesNoRelationshipAttributeLoading(
            ModelValueArray<FetchParameter> modelValueArray,
            MetaEntity entity,
            LockType lockType) throws Exception {
        Object primaryKey = entity.getId().buildValue(modelValueArray);
        log.debug("Build Entity -> PrimaryKey = {}", primaryKey);
        log.debug("Build Entity -> Entity = {}", entity);
        Object entityInstance = entityContainer.find(entity.getEntityClass(), primaryKey);
        log.debug("Build Entity -> Entity Instance = {}", entityInstance);
        if (entityInstance != null)
            return entityInstance;

        entityInstance = entity.buildInstance(primaryKey);
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
            Object result = jdbcQueryRunner.selectByJoinTable(parentInstancePk, entity.getId(),
                    attribute.getRelationship(),
                    attribute, this);
            entity.writeAttributeValue(parentInstance, parentInstance.getClass(), attribute,
                    result);
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
        log.debug("Building relationships -> Foreign Key Attribute = {}", foreignKeyAttribute);
        log.debug("Building relationships -> Foreign Key Value = {}", foreignKeyValue);
        log.debug("Building relationships -> Parent Instance = {}", parentInstance);
        MetaEntity e = persistenceUnitContext.getEntities()
                .get(foreignKeyAttribute.getType().getName());
        log.debug("Building relationships -> Foreign Key Entity = {}", e);
        Object foreignKeyInstance = findById(e, foreignKeyValue, lockType);
        log.debug("Building relationships -> Foreign Key Instance = {}", foreignKeyInstance);
        if (foreignKeyInstance != null) {
            entity.writeAttributeValue(parentInstance, foreignKeyAttribute,
                    foreignKeyInstance);
            RelationshipMetaAttribute a = e.findAttributeByMappedBy(foreignKeyAttribute.getName());
            log.debug("Building relationships -> Relationship Attribute = {}", a);
            if (a != null && a.getRelationship().toOne()) {
                e.writeAttributeValue(foreignKeyInstance, foreignKeyInstance.getClass(),
                        a,
                        parentInstance);
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
        log.debug("Loading Attribute = {}", a);
        log.debug("Loading Attribute -> Current Value = {}", currentValue);
        log.debug("Loading Attribute -> Parent Instance = {}", parentInstance);
        log.debug("Loading Attribute -> Relationship = {}", relationship);

        log.debug("Loading Attribute -> Relationship Target Attribute = {}", relationship.getTargetAttribute());
        if (!relationship.toMany()) {
            MetaEntity entity = persistenceUnitContext.getEntities()
                    .get(parentInstance.getClass().getName());
            Object foreignKey = relationshipMetaAttribute.getForeignKeyValue(parentInstance);
            log.debug("Loading Attribute -> Foreign Key = {}", foreignKey);
            return loadRelationshipByForeignKey(parentInstance, entity, a, foreignKey,
                    LockType.NONE);
        }

        log.debug("Loading Attribute -> To Many Relationship Join Table = {}",
                relationship.getJoinTable());
        if (relationship.getJoinTable() == null) {
            MetaEntity entity = persistenceUnitContext.getEntities()
                    .get(relationship.getTargetEntityClass().getName());
            log.debug("Loading Attribute -> Entity = {}", entity);
            if (entity == null) {
                throw new IllegalArgumentException(
                        "Class '" + relationship.getTargetEntityClass().getName() + "' is not an entity");
            }

            log.debug("Loading Attribute -> Relationship Owning Attribute = {}",
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
        log.debug("Loading Attribute -> Primary Key = {}", pk);
        return jdbcQueryRunner.selectByJoinTable(pk, e.getId(), relationship, relationshipMetaAttribute, this);
    }

    @Override
    public void persist(
            MetaEntity entity,
            Object entityInstance,
            ModelValueArray<AbstractMetaAttribute> modelValueArray)
            throws Exception {
        EntityStatus entityStatus = MetaEntityHelper.getEntityStatus(entity, entityInstance);
        log.debug("Persist -> Entity Status = {}", entityStatus);
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

        log.debug("Update -> Update Count = {}", updateCount);
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

            log.debug("Insert -> Primary Key = {}", pkId);
            if (pkId != null) {
                log.debug("Insert -> Primary key Class = {}", pkId.getClass());
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
            log.debug("Insert -> Id Parameters = {}", idParameters);
            List<QueryParameter> parameters = MetaEntityHelper.convertAVToQP(modelValueArray);
            log.debug("Insert -> Query Parameters = {}", parameters);
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
        log.debug("Updating Postponed Join Columns -> Entity Name = {}", entity.getName());

        List list = entity.getJoinColumnPostponedUpdateAttributeList(entityInstance);
        log.debug("Updating Postponed Join Columns -> Attribute List IsEmpty = {}", list.isEmpty());
        if (list.isEmpty()) {
            return;
        }

        for (Object o : list) {
            PostponedUpdateInfo postponedUpdateInfo = (PostponedUpdateInfo) o;
            log.debug("Updating Postponed Join Columns -> Attribute Type = {}",
                    postponedUpdateInfo.getType());
            Object instance = entityContainer.find(postponedUpdateInfo.getType(),
                    postponedUpdateInfo.getId());
            MetaEntity toEntity = persistenceUnitContext.getEntities()
                    .get(postponedUpdateInfo.getType().getName());
            log.debug("Updating Postponed Join Columns -> To Entity = {}", toEntity);
            log.debug("updatePostponedJoinColumnUpdate: postponedUpdateInfo.getAttributeName()={}",
                    postponedUpdateInfo.getAttributeName());
            Optional<RelationshipMetaAttribute> optional = toEntity
                    .findJoinColumnMappingAttribute(postponedUpdateInfo.getAttributeName());
            log.debug("Updating Postponed Join Columns -> Found Relationship Attribute = {}", optional.isEmpty());
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
        List<QueryParameter> idParameters = entity.getId().queryParameters(idValue);

        for (RelationshipMetaAttribute a : entity.getRelationshipAttributes()) {
            if (a.getRelationship().getJoinTable() != null && a.getRelationship().isOwner()) {
                Object attributeInstance = a.getValue(entityInstance);
                if (attributeInstance != null && CollectionUtils.isCollectionClass(
                        attributeInstance.getClass())
                        && !CollectionUtils.isCollectionEmpty(attributeInstance)) {
                    Collection<?> ees = CollectionUtils.getCollectionFromCollectionOrMap(attributeInstance);
                    log.debug("Persist Join Table Attributes -> Entity Instance List = {}", ees);
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
        log.debug("Delete -> Id Value = {}", idValue);

        List<QueryParameter> idParameters = e.getId().queryParameters(idValue);
        if (MetaEntityHelper.hasOptimisticLock(e, entityInstance)) {
            Object currentVersionValue = e.getVersionMetaAttribute().getReadMethod()
                    .invoke(entityInstance);
            idParameters.add(e.getVersionMetaAttribute().queryParameter(currentVersionValue));
        }

        jdbcQueryRunner.deleteById(e, idParameters);
    }
}
