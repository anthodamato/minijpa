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

import org.minijpa.jdbc.*;
import org.minijpa.jdbc.JdbcRunner.JdbcNativeRecordBuilder;
import org.minijpa.jdbc.db.SqlSelectData;
import org.minijpa.jdbc.db.SqlSelectDataBuilder;
import org.minijpa.jpa.*;
import org.minijpa.jpa.criteria.join.CollectionJoinImpl;
import org.minijpa.jpa.db.querymapping.EntityMapping;
import org.minijpa.jpa.db.querymapping.QueryResultMapping;
import org.minijpa.jpa.jpql.SemanticException;
import org.minijpa.jpa.model.AbstractMetaAttribute;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.jpa.model.RelationshipMetaAttribute;
import org.minijpa.jpa.model.relationship.Cascade;
import org.minijpa.jpa.model.relationship.JoinColumnMapping;
import org.minijpa.metadata.PersistenceUnitContext;
import org.minijpa.sql.model.*;
import org.minijpa.sql.model.condition.*;
import org.minijpa.sql.model.join.FromJoin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Parameter;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.criteria.CompoundSelection;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public class JdbcEntityManagerImpl implements JdbcEntityManager {

    private static final Logger log = LoggerFactory.getLogger(JdbcEntityManagerImpl.class);
    protected DbConfiguration dbConfiguration;
    protected PersistenceUnitContext persistenceUnitContext;
    private final EntityContainer entityContainer;
    protected ConnectionHolder connectionHolder;
    private final EntityHandler entityHandler;
    private final JpqlModule jpqlModule;
    private final JdbcFetchParameterRecordBuilder jdbcFetchParameterRecordBuilder = new JdbcFetchParameterRecordBuilder();
    private final JdbcRunner.JdbcRecordBuilderValue jdbcJpqlRecordBuilder = new JdbcRunner.JdbcRecordBuilderValue();
    private final JdbcTupleRecordBuilder jdbcTupleRecordBuilder = new JdbcTupleRecordBuilder();
    private final JdbcNativeRecordBuilder nativeRecordBuilder = new JdbcNativeRecordBuilder();
    private final JdbcQRMRecordBuilder qrmRecordBuilder = new JdbcQRMRecordBuilder();
    private final JdbcFetchJoinRecordBuilder jdbcFetchJoinRecordBuilder = new JdbcFetchJoinRecordBuilder();

    public JdbcEntityManagerImpl(DbConfiguration dbConfiguration,
                                 PersistenceUnitContext persistenceUnitContext,
                                 EntityContainer entityContainer, ConnectionHolder connectionHolder) {
        super();
        this.dbConfiguration = dbConfiguration;
        this.persistenceUnitContext = persistenceUnitContext;
        this.entityContainer = entityContainer;
        this.connectionHolder = connectionHolder;
        this.entityHandler = new EntityHandlerImpl(persistenceUnitContext, entityContainer,
                new JdbcQueryRunner(connectionHolder, dbConfiguration,
                        persistenceUnitContext.getAliasGenerator()));
        this.jpqlModule = new JpqlModule(dbConfiguration, persistenceUnitContext);
    }

    public EntityHandler getEntityLoader() {
        return entityHandler;
    }

    public Object findById(
            Class<?> entityClass,
            Object primaryKey,
            LockType lockType) throws Exception {
        log.debug("Find By Id -> Primary Key {}", primaryKey);

        MetaEntity entity = persistenceUnitContext.getEntities().get(entityClass.getName());
        if (entity == null) {
            throw new IllegalArgumentException(
                    "Class '" + entityClass.getName() + "' is not an entity");
        }

        log.debug("Find By Id -> Entity {}", entity);
        return entityHandler.findById(entity, entity.getId().checkClass(primaryKey), lockType);
    }

    public void refresh(
            Object entityInstance,
            LockType lockType) throws Exception {
        Class<?> entityClass = entityInstance.getClass();
        MetaEntity entity = persistenceUnitContext.getEntities().get(entityClass.getName());
        if (entity == null) {
            throw new IllegalArgumentException(
                    "Class '" + entityClass.getName() + "' is not an entity");
        }

        if (!entityContainer.isManaged(entityInstance)) {
            throw new IllegalArgumentException("Entity '" + entityInstance + "' is not managed");
        }

        Object primaryKey = entity.getId().readValue(entityInstance);
        entityHandler.refresh(entity, entityInstance, primaryKey, lockType);

        // cascades
        List<RelationshipMetaAttribute> cascadeAttributes = entity.getCascadeAttributes(Cascade.ALL,
                Cascade.REFRESH);
        for (RelationshipMetaAttribute attribute : cascadeAttributes) {
            Object attributeInstance = attribute.getValue(entityInstance);
            if (attribute.getRelationship().toMany()) {
                Collection<?> ees = CollectionUtils.getCollectionFromCollectionOrMap(attributeInstance);
                for (Object instance : ees) {
                    refresh(instance, lockType);
                }
            } else {
                refresh(attributeInstance, lockType);
            }
        }
    }

    public void lock(
            Object entityInstance,
            LockType lockType) throws Exception {
        Class<?> entityClass = entityInstance.getClass();
        MetaEntity entity = persistenceUnitContext.getEntities().get(entityClass.getName());
        if (entity == null) {
            throw new IllegalArgumentException(
                    "Class '" + entityClass.getName() + "' is not an entity");
        }

        if (!entityContainer.isManaged(entityInstance)) {
            throw new IllegalArgumentException("Entity '" + entityInstance + "' is not managed");
        }

        MetaEntityHelper.setLockType(entity, entityInstance, lockType);
        Object primaryKey = entity.getId().readValue(entityInstance);
        entityHandler.refresh(entity, entityInstance, primaryKey, lockType);
    }

    public LockType getLockType(Object entityInstance) throws Exception {
        Class<?> entityClass = entityInstance.getClass();
        MetaEntity entity = persistenceUnitContext.getEntities().get(entityClass.getName());
        if (entity == null) {
            throw new IllegalArgumentException(
                    "Class '" + entityClass.getName() + "' is not an entity");
        }

        if (!entityContainer.isManaged(entityInstance)) {
            throw new IllegalArgumentException("Entity '" + entityInstance + "' is not managed");
        }

        return MetaEntityHelper.getLockType(entity, entityInstance);
    }

    @Override
    public void persist(
            MetaEntity entity,
            Object entityInstance,
            MiniFlushMode miniFlushMode)
            throws Exception {
        Object idValue = entity.getId().readValue(entityInstance);
        log.debug("Persist -> Entity {}", entity);
        log.debug("Persist -> Id Value {}", idValue);
        if (idValue == null && entity.getId().getPkGeneration().getPkStrategy() == PkStrategy.PLAIN) {
            throw new PersistenceException(
                    "Id must be manually assigned for '" + entity.getEntityClass().getName() + "'");
        }

        ModelValueArray<AbstractMetaAttribute> modelValueArray = MetaEntityHelper.getModifications(entity,
                entityInstance);
        checkNullableAttributes(entity, entityInstance, modelValueArray);
        if (idValue == null) {
            if (entity.getId().getPkGeneration().getPkStrategy() == PkStrategy.SEQUENCE) {
                PkSequenceGenerator pkSequenceGenerator = entity.getId().getPkGeneration()
                        .getPkSequenceGenerator();
                String seqStm = dbConfiguration.getSqlStatementGenerator()
                        .sequenceNextValueStatement(null,
                                pkSequenceGenerator.getSequenceName());

                idValue = dbConfiguration.getJdbcRunner()
                        .generateNextSequenceValue(connectionHolder.getConnection(),
                                seqStm);
                entity.getId().writeValue(entityInstance, idValue);
            } else if (entity.getId().getPkGeneration().getPkStrategy() == PkStrategy.IDENTITY) {
                List<Object> managedEntityList = entityContainer.getManagedEntityList();
                persistEarlyInsertEntityInstance(entity, modelValueArray, managedEntityList);
                entityHandler.persist(entity, entityInstance, modelValueArray);
                MetaEntityHelper.setEntityStatus(entity, entityInstance, EntityStatus.FLUSHED);
                idValue = entity.getId().readValue(entityInstance);
                addInfoForPostponedUpdateEntities(idValue, entity, modelValueArray);
                entity.clearModificationAttributes(entityInstance);
            }
        }

        EntityStatus entityStatus = MetaEntityHelper.getEntityStatus(entity, entityInstance);
        if (entityStatus == EntityStatus.NEW) {
            MetaEntityHelper.setEntityStatus(entity, entityInstance,
                    EntityStatus.PERSIST_NOT_FLUSHED);
        }

        entityContainer.addManaged(entityInstance, idValue);

        // cascades
        List<RelationshipMetaAttribute> cascadeAttributes = entity.getCascadeAttributes(Cascade.ALL,
                Cascade.PERSIST);
        for (RelationshipMetaAttribute attribute : cascadeAttributes) {
            Object attributeInstance = attribute.getValue(entityInstance);
            if (attribute.getRelationship().toMany()) {
                Collection<?> ees = CollectionUtils.getCollectionFromCollectionOrMap(attributeInstance);
                for (Object instance : ees) {
                    persist(attribute.getRelationship().getAttributeType(), instance, miniFlushMode);
                }
            } else {
                persist(attribute.getRelationship().getAttributeType(), attributeInstance, miniFlushMode);
            }
        }
    }

    private void addInfoForPostponedUpdateEntities(
            Object idValue,
            MetaEntity entity,
            ModelValueArray<AbstractMetaAttribute> modelValueArray)
            throws IllegalAccessException, InvocationTargetException {
        for (JoinColumnMapping joinColumnMapping : entity.getJoinColumnMappings()) {
            int index = modelValueArray.indexOfModel(joinColumnMapping.getAttribute());
            log.debug("Add Info For Postponed Update -> Index {}", index);
            log.debug("Add Info For Postponed Update -> Join Column Mapping Attribute={}",
                    joinColumnMapping.getAttribute());
            if (index != -1) {
                Object instance = modelValueArray.getValue(index);
                log.debug("Add Info For Postponed Update -> Instance {}", instance);
                MetaEntity e = persistenceUnitContext.getEntities().get(instance.getClass().getName());
                log.debug("Add Info For Postponed Update -> Entity {}", e);
                List list = MetaEntityHelper.getJoinColumnPostponedUpdateAttributeList(e, instance);
                list.add(new PostponedUpdateInfo(idValue, entity.getEntityClass(),
                        modelValueArray.getModel(index).getName()));
            }
        }
    }

    /**
     * The modified attributes must include the not nullable attributes.
     *
     * @param entity              the meta entity
     * @param entityInstance      entity instance
     * @param attributeValueArray modified values
     */
    private void checkNullableAttributes(
            MetaEntity entity,
            Object entityInstance,
            ModelValueArray<AbstractMetaAttribute> attributeValueArray) throws Exception {
        if (entityContainer.isManaged(entityInstance)) {
            EntityStatus entityStatus = MetaEntityHelper.getEntityStatus(entity, entityInstance);
            if (entityStatus == EntityStatus.FLUSHED
                    || entityStatus == EntityStatus.FLUSHED_LOADED_FROM_DB) {
                // It's an update.
                // TODO. It should check that no not nullable attrs will be set to null.
                return;
            }
        }

        List<AbstractMetaAttribute> notNullableAttributes = entity.notNullableAttributes();
        if (notNullableAttributes.isEmpty()) {
            return;
        }

        if (attributeValueArray.isEmpty()) {
            throw new PersistenceException(
                    "Attribute '" + notNullableAttributes.get(0).getName() + "' is null");
        }

        notNullableAttributes.forEach(a -> {
            Optional<AbstractMetaAttribute> o = attributeValueArray.getModels().stream().filter(av -> av == a)
                    .findFirst();
            if (o.isEmpty()) {
                throw new PersistenceException("Attribute '" + a.getName() + "' is null");
            }
        });
    }

    @Override
    public void flush() throws Exception {
        log.debug("Flushing entities...");
        List<Object> managedEntityList = entityContainer.getManagedEntityList();
        // removes join table owning entity records first
        for (Object entityInstance : managedEntityList) {
            MetaEntity me = persistenceUnitContext.getEntities().get(entityInstance.getClass().getName());
            EntityStatus entityStatus = MetaEntityHelper.getEntityStatus(me, entityInstance);
            if (entityStatus == EntityStatus.REMOVED_NOT_FLUSHED) {
                log.debug("Flushing -> REMOVED_NOT_FLUSHED Join Table Records Entity Instance {}",
                        entityInstance);
                entityHandler.removeJoinTableRecords(entityInstance, me);
            }
        }

        for (Object entityInstance : managedEntityList) {
            MetaEntity me = persistenceUnitContext.getEntities().get(entityInstance.getClass().getName());
            EntityStatus entityStatus = MetaEntityHelper.getEntityStatus(me, entityInstance);
            log.debug("Flushing -> Entity Instance = {}", entityInstance);
            log.debug("Flushing -> Entity Status = {}", entityStatus);
            switch (entityStatus) {
                case FLUSHED:
                case FLUSHED_LOADED_FROM_DB:
                    // makes updates
                    log.debug("Flushing -> FLUSHED_LOADED_FROM_DB Entity Instance {}", entityInstance);
                    ModelValueArray<AbstractMetaAttribute> modelValueArray = MetaEntityHelper.getModifications(me,
                            entityInstance);
                    log.debug("Flushing -> FLUSHED_LOADED_FROM_DB Modification Count {}",
                            modelValueArray.size());
                    if (!modelValueArray.isEmpty()) {
                        entityHandler.persist(me, entityInstance, modelValueArray);
                        me.clearModificationAttributes(entityInstance);
                    }

                    break;
                case PERSIST_NOT_FLUSHED:
                    log.debug("Flushing -> PERSIST_NOT_FLUSHED Entity Instance {}", entityInstance);
                    modelValueArray = MetaEntityHelper.getModifications(me, entityInstance);
                    log.debug("Flushing -> PERSIST_NOT_FLUSHED Modification Count {}", modelValueArray.size());
                    persistEarlyInsertEntityInstance(me, modelValueArray, managedEntityList);
                    entityHandler.persist(me, entityInstance, modelValueArray);
                    MetaEntityHelper.setEntityStatus(me, entityInstance, EntityStatus.FLUSHED);
                    me.clearModificationAttributes(entityInstance);
                    break;
                case REMOVED_NOT_FLUSHED:
                    log.debug("Flushing -> REMOVED_NOT_FLUSHED Entity Instance {}", entityInstance);
                    persistEarlyDeleteEntityInstance(me, entityInstance, managedEntityList);
                    entityHandler.delete(entityInstance, me);
                    entityContainer.removeManaged(entityInstance);
                    MetaEntityHelper.setEntityStatus(me, entityInstance, EntityStatus.REMOVED);
                    break;
                case EARLY_INSERT:
                    log.debug("Flushing -> EARLY_INSERT Entity Instance {}", entityInstance);
                    MetaEntityHelper.setEntityStatus(me, entityInstance, EntityStatus.FLUSHED);
                    break;
                case EARLY_REMOVE:
                    log.debug("Flushing -> EARLY_REMOVE Entity Instance {}", entityInstance);
                    MetaEntityHelper.setEntityStatus(me, entityInstance, EntityStatus.REMOVED);
                    break;
            }
        }

        log.debug("Flushing -> Persisting Join Table Attributes");
        for (Object entityInstance : managedEntityList) {
            log.debug("Flushing -> Persisting Join Table Attributes Entity Instance {}", entityInstance);
            MetaEntity me = persistenceUnitContext.getEntities().get(entityInstance.getClass().getName());
            EntityStatus entityStatus = MetaEntityHelper.getEntityStatus(me, entityInstance);
            log.debug("Flushing -> Persisting Join Table Attributes Entity Status {}", entityStatus);
            if (entityStatus == EntityStatus.FLUSHED) {
                entityHandler.persistJoinTableAttributes(me, entityInstance);
            }
        }

        log.debug("Flushing -> Done");
    }


    /**
     * Inserts entities related to join columns not flushed yet.
     *
     * @param me                meta entity
     * @param modelValueArray   values
     * @param managedEntityList entity list
     */
    private void persistEarlyInsertEntityInstance(
            MetaEntity me,
            ModelValueArray<AbstractMetaAttribute> modelValueArray,
            List<Object> managedEntityList) throws Exception {
        List<JoinColumnMapping> joinColumnMappings = me.getJoinColumnMappings();
        log.debug("Persist Early Insert -> Join Column Mapping Count {}", joinColumnMappings.size());
        if (joinColumnMappings.isEmpty()) {
            return;
        }

        for (JoinColumnMapping joinColumnMapping : joinColumnMappings) {
            log.debug("Persist Early Insert -> Join Column Mapping Attribute {}", joinColumnMapping.getAttribute());
            log.debug("Persist Early Insert -> Join Column Mapping {}", joinColumnMapping.get());
            int index = modelValueArray.indexOfModel(joinColumnMapping.getAttribute());
            log.debug("Persist Early Insert -> Join Column Mapping Index {}", index);
            if (index != -1) {
                Object instance = modelValueArray.getValue(index);
                MetaEntity metaEntity = persistenceUnitContext.getEntities()
                        .get(instance.getClass().getName());
                EntityStatus entityStatus = MetaEntityHelper.getEntityStatus(metaEntity, instance);
                if (entityStatus != EntityStatus.PERSIST_NOT_FLUSHED) {
                    continue;
                }

                if (!managedEntityList.contains(instance)) {
                    continue;
                }

                ModelValueArray<AbstractMetaAttribute> mva = MetaEntityHelper.getModifications(metaEntity,
                        instance);
                entityHandler.persist(metaEntity, instance, mva);
                log.debug("Persist Early Insert -> Join Column Mapping Instance {}", instance);
                MetaEntityHelper.setEntityStatus(metaEntity, instance, EntityStatus.EARLY_INSERT);
                metaEntity.clearModificationAttributes(instance);
            }
        }
    }

    /**
     * Deletes entities related to join columns not flushed yet.
     *
     * @param me                meta entity
     * @param entityInstance    entity instance
     * @param managedEntityList entity list
     */
    private void persistEarlyDeleteEntityInstance(
            MetaEntity me,
            Object entityInstance,
            List<Object> managedEntityList) throws Exception {
        List<RelationshipMetaAttribute> relationshipAttributes = me.getRelationshipAttributes();
        for (RelationshipMetaAttribute relationshipAttribute : relationshipAttributes) {
            log.debug("Persist Early Delete -> Relationship Attribute {}",
                    relationshipAttribute);
            log.debug("Persist Early Delete -> Relationship {}", relationshipAttribute.getRelationship());
            log.debug(
                    "Persist Early Delete -> Relationship Attribute Is Owner {}", relationshipAttribute.getRelationship().isOwner());
            if (!relationshipAttribute.getRelationship().isOwner()
                    && relationshipAttribute.getRelationship().toOne()) {
                Object instance = relationshipAttribute.getValue(entityInstance);
                log.debug("Persist Early Delete -> Instance {}", instance);
                if (instance == null) {
                    continue;
                }

                MetaEntity metaEntity = persistenceUnitContext.getEntities()
                        .get(instance.getClass().getName());
                EntityStatus entityStatus = MetaEntityHelper.getEntityStatus(metaEntity, instance);
                if (entityStatus != EntityStatus.REMOVED_NOT_FLUSHED) {
                    continue;
                }

                if (!managedEntityList.contains(instance)) {
                    continue;
                }

                entityHandler.delete(instance, metaEntity);
                entityContainer.removeManaged(instance);
                MetaEntityHelper.setEntityStatus(metaEntity, instance, EntityStatus.EARLY_REMOVE);
            }
        }
    }

    @Override
    public void remove(Object entity, MiniFlushMode miniFlushMode) throws Exception {
        MetaEntity e = persistenceUnitContext.getEntities().get(entity.getClass().getName());
        log.debug("Remove -> Entity {}", entity);
        log.debug("Remove -> Is Manage {}", entityContainer.isManaged(entity));
        if (entityContainer.isManaged(entity)) {
            log.debug("Remove -> Instance {} in the persistence context", entity);
            entityContainer.markForRemoval(entity);
            // cascades
            List<RelationshipMetaAttribute> cascadeAttributes = e.getCascadeAttributes(Cascade.ALL,
                    Cascade.REMOVE);
            for (RelationshipMetaAttribute attribute : cascadeAttributes) {
                if (!attribute.getRelationship().fromOne()) {
                    continue;
                }

                Object attributeInstance = attribute.getValue(entity);
                if (attribute.getRelationship().toMany()) {
                    Collection<?> ees = CollectionUtils.getCollectionFromCollectionOrMap(attributeInstance);
                    for (Object instance : ees) {
                        remove(instance, miniFlushMode);
                    }
                } else {
                    remove(attributeInstance, miniFlushMode);
                }
            }

        } else {
            log.debug("Remove -> Instance {} not found in the persistence context", entity);
            EntityStatus entityStatus = MetaEntityHelper.getEntityStatus(e, entity);
            if (entityStatus == EntityStatus.DETACHED) {
                throw new IllegalArgumentException("Entity '" + entity + "' is detached");
            }
        }
    }


    @Override
    public void detach(Object entity) throws Exception {
        entityContainer.detach(entity);

        // cascades
        MetaEntity e = persistenceUnitContext.getEntities().get(entity.getClass().getName());
        List<RelationshipMetaAttribute> cascadeAttributes = e.getCascadeAttributes(Cascade.ALL,
                Cascade.DETACH);
        for (RelationshipMetaAttribute attribute : cascadeAttributes) {
            Object attributeInstance = attribute.getValue(entity);
            if (attribute.getRelationship().toMany()) {
                Collection<?> ees = CollectionUtils.getCollectionFromCollectionOrMap(attributeInstance);
                for (Object instance : ees) {
                    detach(instance);
                }
            } else {
                detach(attributeInstance);
            }
        }
    }


    private static class StatementParametersMetaEntity {
        private final StatementParameters statementParameters;
        private final MetaEntity metaEntity;

        public StatementParametersMetaEntity(StatementParameters statementParameters, MetaEntity metaEntity) {
            this.statementParameters = statementParameters;
            this.metaEntity = metaEntity;
        }
    }

    @Override
    public List<?> selectCriteriaQuery(Query query, CriteriaQuery criteriaQuery) throws Exception {
        if (criteriaQuery.getResultType() == Tuple.class) {
            if (!(criteriaQuery.getSelection() instanceof CompoundSelection<?>)) {
                throw new IllegalArgumentException(
                        "Selection '" + criteriaQuery.getSelection() + "' is not a compound selection");
            }

            Map<Parameter<?>, Object> parameterMap = ((AbstractQuery) query).getParameterMap();
            StatementParameters statementParameters = dbConfiguration.getSqlStatementFactory().select(
                    criteriaQuery, query.getLockMode(), parameterMap,
                    persistenceUnitContext.getAliasGenerator());
            SqlSelectData sqlSelectData = (SqlSelectData) statementParameters.getSqlStatement();
            String sql = dbConfiguration.getSqlStatementGenerator().export(sqlSelectData);

            List<Tuple> collectionResult = new ArrayList<>();
            jdbcTupleRecordBuilder.setSqlSelectData(sqlSelectData);
            jdbcTupleRecordBuilder.setObjects(collectionResult);
            jdbcTupleRecordBuilder.setCompoundSelection(
                    (CompoundSelection<?>) criteriaQuery.getSelection());
            dbConfiguration.getJdbcRunner().runQuery(connectionHolder.getConnection(), sql,
                    statementParameters.getParameters(), jdbcTupleRecordBuilder);
            return collectionResult;
        }

        List<Join<?, ?>> joins = dbConfiguration.getSqlStatementFactory().getJoins(criteriaQuery.getRoots());
        Map<String, Object> hints = query.getHints();
        log.debug("Select Criteria -> Join Count {}", joins.size());
        log.debug("Select Criteria -> SPLIT_MULTIPLE_JOINS {}", hints.get(QueryHints.SPLIT_MULTIPLE_JOINS));
        if (joins.size() == 2 &&
                hints.get(QueryHints.SPLIT_MULTIPLE_JOINS) != null &&
                ((Boolean) hints.get(QueryHints.SPLIT_MULTIPLE_JOINS))) {
            // split multiple joins case
            List<MetaEntity> joinMetaEntityList = new ArrayList<>();
            joins.forEach(join -> {
                if (join instanceof CollectionJoinImpl) {
                    CollectionJoinImpl collectionJoin = (CollectionJoinImpl) join;
                    joinMetaEntityList.add(collectionJoin.getMetaEntity());
                }
            });

            List<StatementParametersMetaEntity> statementParametersList = new ArrayList<>();
            Map<Parameter<?>, Object> parameterMap = ((AbstractQuery) query).getParameterMap();
            joinMetaEntityList.forEach(metaEntity -> {
                CriteriaQuery<?> cq = dbConfiguration.getSqlStatementFactory().filterCriteriaQuery(criteriaQuery, metaEntity);
                StatementParameters statementParameters = dbConfiguration.getSqlStatementFactory().select(
                        cq, query.getLockMode(), parameterMap,
                        persistenceUnitContext.getAliasGenerator());
                List<MetaEntity> joinEntities = new ArrayList<>(joinMetaEntityList);
                joinEntities.remove(metaEntity);
                statementParametersList.add(new StatementParametersMetaEntity(statementParameters, joinEntities.get(0)));
            });

            return runQueryMergeMultipleFetchJoins(statementParametersList);
        }

        Map<Parameter<?>, Object> parameterMap = ((AbstractQuery) query).getParameterMap();
        StatementParameters statementParameters = dbConfiguration.getSqlStatementFactory().select(
                criteriaQuery, query.getLockMode(), parameterMap,
                persistenceUnitContext.getAliasGenerator());
        return runQuery(statementParameters, hints);
    }

    private List<?> runQueryMergeMultipleFetchJoins(
            List<StatementParametersMetaEntity> statementParametersList) throws Exception {
        Collection<Object> finalCollectionResult = (Collection<Object>) CollectionUtils.createInstance(
                null,
                CollectionUtils.findCollectionImplementationClass(List.class));
        for (StatementParametersMetaEntity statementParametersMetaEntity : statementParametersList) {
            StatementParameters statementParameters = statementParametersMetaEntity.statementParameters;
            log.debug("Merge Multiple Fetch Joins -> Statement Type {}", statementParameters.getStatementType());
            // TODO what about normal join
            if (statementParameters.getStatementType() == StatementType.FETCH_JOIN) {
                Collection<Object> collectionResult = (Collection<Object>) CollectionUtils.createInstance(
                        null,
                        CollectionUtils.findCollectionImplementationClass(List.class));
                SqlSelectData sqlSelectData = (SqlSelectData) statementParameters.getSqlStatement();
                Optional<MetaEntity> optionalEntity = persistenceUnitContext
                        .findMetaEntityByTableName(sqlSelectData.getResult().getName());
                MetaEntity entity = optionalEntity.get();

                initializeFetchJoinRecordBuilder(collectionResult, entity, statementParameters);
                String sql = dbConfiguration.getSqlStatementGenerator().export(sqlSelectData);
                dbConfiguration.getJdbcRunner().runQuery(connectionHolder.getConnection(), sql,
                        statementParameters.getParameters(), jdbcFetchJoinRecordBuilder);
                log.debug("Merge Multiple Fetch Joins -> Collection Result Count {}", collectionResult.size());

                // merge the collection with the final one
                for (Object entityInstance : collectionResult) {
                    if (!finalCollectionResult.contains(entityInstance)) {
                        finalCollectionResult.add(entityInstance);
                    }
                }
            }
        }

        return (List<?>) finalCollectionResult;
    }


    private List<StatementParametersMetaEntity> splitMultipleFetchJoinQuery(
            StatementParameters statementParameters) {
        SqlSelect sqlSelect = (SqlSelect) statementParameters.getSqlStatement();
        List<FromJoin> fromJoins = sqlSelect.getFrom().stream()
                .filter(from -> (from instanceof FromJoin))
                .map(from -> (FromJoin) from).collect(Collectors.toList());

        List<StatementParametersMetaEntity> statementParametersList = new ArrayList<>();
        statementParameters.getFetchJoinMetaEntities().forEach(fetchJoinMetaEntity -> {
            SqlSelectDataBuilder selectBuilder = new SqlSelectDataBuilder();
            if (sqlSelect.isDistinct())
                selectBuilder.distinct();

            Optional<FromTable> optionalFromTable = sqlSelect.getFrom().stream().filter(f -> (f instanceof FromTable)).map(f -> (FromTable) f).findFirst();
            optionalFromTable.ifPresent(selectBuilder::withFromTable);

            // Involved joins
            List<FromJoin> relatedFromJoins = extractRelatedFromJoins(fetchJoinMetaEntity, fromJoins);
            relatedFromJoins.forEach(selectBuilder::withFromTable);
            selectBuilder.withResult(sqlSelect.getResult());

            Optional<FromJoin> optionalFromJoin = fromJoins.stream().filter(fromJoin -> fromJoin.getToTable().getName().equals(fetchJoinMetaEntity.getTableName())).findFirst();
            // Values
            List<Value> values = sqlSelect.getValues().stream()
                    .filter(v -> (v instanceof TableColumn)).map(v -> (TableColumn) v)
                    .filter(t -> t.getTable() != null &&
                            (t.getTable().getAlias().equals(optionalFromTable.get().getAlias()) ||
                                    t.getTable().getAlias().equals(optionalFromJoin.get().getToTable().getAlias())))
                    .collect(Collectors.toList());
            selectBuilder.withValues(values);
            List<Integer> valueIndexes = findMatchingIndexes(sqlSelect.getValues(), values);

            // filter conditions
            List<Condition> conditions = new ArrayList<>();
            if (optionalFromTable.isPresent() && optionalFromTable.get().getAlias() != null) {
                filterConditions(sqlSelect, optionalFromTable.get().getAlias(), conditions);
            }

            relatedFromJoins.forEach(j -> {
                if (j.getToTable().getAlias() != null)
                    filterConditions(sqlSelect, j.getToTable().getAlias(), conditions);
            });

            selectBuilder.withConditions(conditions);

            SqlSelectData sqlSelectData = (SqlSelectData) sqlSelect;
            List<FetchParameter> fetchParameters = new ArrayList<>();
            valueIndexes.forEach(i -> fetchParameters.add(sqlSelectData.getFetchParameters().get(i)));
            selectBuilder.withFetchParameters(fetchParameters);

            List<RelationshipMetaAttribute> relationshipMetaAttributes = new ArrayList<>();
            statementParameters.getFetchJoinMetaAttributes().forEach(m -> {
                if (m.getRelationship().getAttributeType() == fetchJoinMetaEntity) {
                    relationshipMetaAttributes.add(m);
                }
            });

            // Query Parameters
            List<QueryParameter> queryParameters = new ArrayList<>();
            if (optionalFromTable.isPresent() && optionalFromTable.get().getAlias() != null) {
                filterQueryParameterByAlias(statementParameters.getParameters(), optionalFromTable.get().getAlias(), queryParameters);
            }

            relatedFromJoins.forEach(j -> {
                if (j.getToTable().getAlias() != null)
                    filterQueryParameterByAlias(statementParameters.getParameters(), j.getToTable().getAlias(), queryParameters);
            });

            StatementParameters sp = new StatementParameters(
                    selectBuilder.build(),
                    queryParameters,
                    StatementType.FETCH_JOIN,
                    List.of(fetchJoinMetaEntity),
                    relationshipMetaAttributes);
            statementParametersList.add(new StatementParametersMetaEntity(sp, fetchJoinMetaEntity));
        });

        return statementParametersList;
    }

    private List<Integer> findMatchingIndexes(List<Value> fullValues, List<Value> values) {
        List<Integer> indexes = new ArrayList<>();
        for (Value value : values) {
            for (int k = 0; k < fullValues.size(); ++k) {
                if (fullValues.get(k) == value) {
                    indexes.add(k);
                }
            }
        }

        return indexes;
    }

    private boolean hasTableColumnTheAlias(TableColumn tableColumn, String alias) {
        if (tableColumn.getTable() == null)
            return false;

        if (tableColumn.getTable().getAlias().isEmpty())
            return false;

        return tableColumn.getTable().getAlias().equals(alias);
    }


    private void filterQueryParameterByAlias(
            List<QueryParameter> queryParameters,
            String alias,
            List<QueryParameter> parameters) {
        queryParameters.forEach(qp -> {
            if (qp.getColumn() != null && qp.getColumn() instanceof TableColumn) {
                if (hasTableColumnTheAlias((TableColumn) qp.getColumn(), alias)) {
                    parameters.add(qp);
                }
            }
        });
    }

    private void filterConditions(
            SqlSelect sqlSelect,
            String alias,
            List<Condition> conditionList) {
        if (sqlSelect.getConditions() == null)
            return;

        List<Condition> conditions = sqlSelect.getConditions();
        conditions.forEach(c -> {
                    filterCondition(c, alias, conditionList);
                }
        );
    }


    private void filterCondition(
            Condition condition,
            String alias,
            List<Condition> conditionList) {
        if (condition instanceof BetweenCondition) {
            BetweenCondition betweenCondition = (BetweenCondition) condition;
            if (betweenCondition.getLeftExpression() instanceof TableColumn &&
                    hasTableColumnTheAlias(((TableColumn) betweenCondition.getLeftExpression()), alias)) {
                conditionList.add(condition);
            } else if (betweenCondition.getRightExpression() instanceof TableColumn &&
                    hasTableColumnTheAlias(((TableColumn) betweenCondition.getRightExpression()), alias)) {
                conditionList.add(condition);
            }
        } else if (condition instanceof BinaryCondition) {
            BinaryCondition binaryCondition = (BinaryCondition) condition;
            if (binaryCondition.getLeft() instanceof TableColumn &&
                    hasTableColumnTheAlias(((TableColumn) binaryCondition.getLeft()), alias)) {
                conditionList.add(condition);
            } else if (binaryCondition.getRight() instanceof TableColumn &&
                    hasTableColumnTheAlias(((TableColumn) binaryCondition.getRight()), alias)) {
                conditionList.add(condition);
            }
        } else if (condition instanceof BinaryLogicCondition) {
            BinaryLogicCondition binaryLogicCondition = (BinaryLogicCondition) condition;
            binaryLogicCondition.getConditions().forEach(bc -> filterCondition(bc, alias, conditionList));
        } else if (condition instanceof InCondition) {
            InCondition inCondition = (InCondition) condition;
            if (hasTableColumnTheAlias(inCondition.getLeftColumn(), alias)) {
                conditionList.add(condition);
            }
        } else if (condition instanceof NestedCondition) {
            NestedCondition nestedCondition = (NestedCondition) condition;
            filterCondition(nestedCondition.getCondition(), alias, conditionList);
        } else if (condition instanceof NotCondition) {
            NotCondition notCondition = (NotCondition) condition;
            filterCondition(notCondition.getCondition(), alias, conditionList);
        } else if (condition instanceof UnaryCondition) {
            UnaryCondition unaryCondition = (UnaryCondition) condition;
            if (unaryCondition.getOperand() instanceof TableColumn &&
                    hasTableColumnTheAlias(((TableColumn) unaryCondition.getOperand()), alias)) {
                conditionList.add(condition);
            }
        } else if (condition instanceof UnaryLogicCondition) {
            UnaryLogicCondition unaryLogicCondition = (UnaryLogicCondition) condition;
            filterCondition(unaryLogicCondition.getCondition(), alias, conditionList);
        }
    }


    /**
     * With multiple joins it has to extract two FromJoin. They are the main table and the join table.
     *
     * @param fetchJoinMetaEntity meta entity
     * @param fromJoins           join list
     * @return the related joins
     */
    private List<FromJoin> extractRelatedFromJoins(
            MetaEntity fetchJoinMetaEntity,
            List<FromJoin> fromJoins) {
        Optional<FromJoin> optional = fromJoins.stream().filter(fromJoin -> fromJoin.getToTable().getName().equals(fetchJoinMetaEntity.getTableName())).findFirst();
        if (optional.isEmpty()) {
            throw new SemanticException("Join table not found for " + fetchJoinMetaEntity.getTableName());
        }

        FromJoin fromJoin = optional.get();
        Optional<FromJoin> optionalJoinTable = fromJoins.stream().filter(fj -> fj.getToTable().getAlias().equals(fromJoin.getFromAlias())).findFirst();
        if (optionalJoinTable.isEmpty()) {
            throw new SemanticException("Join table not found for " + fetchJoinMetaEntity.getTableName());
        }

        return List.of(optionalJoinTable.get(), fromJoin);
    }


    private JdbcRecordBuilder initializeFetchJoinRecordBuilder(
            Collection<Object> collectionResult,
            MetaEntity metaEntity,
            StatementParameters statementParameters) {
        SqlSelectData sqlSelectData = (SqlSelectData) statementParameters.getSqlStatement();
        Optional<MetaEntity> optionalEntity = persistenceUnitContext
                .findMetaEntityByTableName(sqlSelectData.getResult().getName());
        MetaEntity entity = optionalEntity.get();

        entityHandler.setLockType(LockType.NONE);
        jdbcFetchJoinRecordBuilder.setCollectionResult(collectionResult);
        jdbcFetchJoinRecordBuilder.setEntityLoader(entityHandler);
        jdbcFetchJoinRecordBuilder.setMetaEntity(metaEntity);
        jdbcFetchJoinRecordBuilder.setFetchJoinMetaEntities(statementParameters.getFetchJoinMetaEntities());
        jdbcFetchJoinRecordBuilder.setFetchJoinMetaAttributes(
                statementParameters.getFetchJoinMetaAttributes());
        jdbcFetchJoinRecordBuilder.setFetchParameters(sqlSelectData.getFetchParameters());
        jdbcFetchJoinRecordBuilder.setDistinct(sqlSelectData.isDistinct());
        return jdbcFetchJoinRecordBuilder;
    }

    private List<?> runQuery(
            StatementParameters statementParameters,
            Map<String, Object> hints) throws Exception {
        SqlSelectData sqlSelectData = (SqlSelectData) statementParameters.getSqlStatement();
        String sql = dbConfiguration.getSqlStatementGenerator().export(sqlSelectData);
        sqlSelectData.getFetchParameters().forEach(f -> log.debug("select: f={}", f));
        log.debug("runQuery: sql={}", sql);
        log.debug("runQuery: sqlSelectData.getResult()={}", sqlSelectData.getResult());
        log.debug("runQuery: statementParameters.getStatementType()={}",
                statementParameters.getStatementType());

        if (statementParameters.getStatementType() == StatementType.FETCH_JOIN) {
            if (hints != null &&
                    hints.get(QueryHints.SPLIT_MULTIPLE_JOINS) != null &&
                    ((Boolean) hints.get(QueryHints.SPLIT_MULTIPLE_JOINS)) &&
                    statementParameters.getFetchJoinMetaEntities().size() > 1) {
                List<StatementParametersMetaEntity> parametersMetaEntityList = splitMultipleFetchJoinQuery(statementParameters);
                return runQueryMergeMultipleFetchJoins(parametersMetaEntityList);
            }

            Collection<Object> collectionResult = (Collection<Object>) CollectionUtils.createInstance(
                    null,
                    CollectionUtils.findCollectionImplementationClass(List.class));
            log.debug("runQuery: collectionResult={}", collectionResult);
            Optional<MetaEntity> optionalEntity = persistenceUnitContext
                    .findMetaEntityByTableName(sqlSelectData.getResult().getName());
            MetaEntity entity = optionalEntity.get();
            initializeFetchJoinRecordBuilder(collectionResult, entity, statementParameters);
            dbConfiguration.getJdbcRunner().runQuery(connectionHolder.getConnection(), sql,
                    statementParameters.getParameters(), jdbcFetchJoinRecordBuilder);
            return (List<?>) collectionResult;
        }

        if (sqlSelectData.getResult() != null) {
            Collection<Object> collectionResult = (Collection<Object>) CollectionUtils.createInstance(
                    null,
                    CollectionUtils.findCollectionImplementationClass(List.class));
            log.debug("runQuery: collectionResult={}", collectionResult);

            Optional<MetaEntity> optionalEntity = persistenceUnitContext
                    .findMetaEntityByTableName(sqlSelectData.getResult().getName());
            MetaEntity entity = optionalEntity.get();
            entityHandler.setLockType(LockType.NONE);
            jdbcFetchParameterRecordBuilder.setCollectionResult(collectionResult);
            jdbcFetchParameterRecordBuilder.setEntityLoader(entityHandler);
            jdbcFetchParameterRecordBuilder.setMetaEntity(entity);
            jdbcFetchParameterRecordBuilder.setFetchParameters(sqlSelectData.getFetchParameters());

            dbConfiguration.getJdbcRunner().runQuery(connectionHolder.getConnection(), sql,
                    statementParameters.getParameters(), jdbcFetchParameterRecordBuilder);
            return (List<?>) collectionResult;
        }

        // returns an aggregate expression result (max, min, etc)
        List<Object> collectionResult = new ArrayList<>();
        jdbcJpqlRecordBuilder.setFetchParameters(sqlSelectData.getFetchParameters());
        jdbcJpqlRecordBuilder.setCollectionResult(collectionResult);
        dbConfiguration.getJdbcRunner().runQuery(connectionHolder.getConnection(), sql,
                statementParameters.getParameters(), jdbcJpqlRecordBuilder);
        return collectionResult;
    }


    private void assignParameterValues(
            List<QueryParameter> queryParameters,
            Map<Parameter<?>, Object> parameterMap) {
        if (queryParameters == null || queryParameters.isEmpty())
            return;

        queryParameters.forEach(qp -> {
            String inputParameter = qp.getInputParameter();
            Optional<Object> optional = ParameterUtils.findParameterValue(parameterMap, inputParameter);
            if (optional.isEmpty())
                throw new SemanticException("Input parameter '" + inputParameter + "' value not found");

            Object value = optional.get();
            qp.setValue(value);
            qp.setSqlType(JdbcTypes.sqlTypeFromClass(optional.get().getClass()));
        });
    }


    @Override
    public List<?> selectJpql(
            StatementParameters statementParameters,
            Map<Parameter<?>, Object> parameterMap,
            Map<String, Object> hints,
            LockType lockType,
            Class<?> resultClass) {
        assignParameterValues(statementParameters.getParameters(), parameterMap);
        SqlSelectData sqlSelectData = (SqlSelectData) statementParameters.getSqlStatement();
        if (resultClass != null) {
            Optional<MetaEntity> optionalEntity = persistenceUnitContext
                    .findMetaEntityByTableName(sqlSelectData.getResult().getName());
            if (optionalEntity.isEmpty())
                throw new IllegalArgumentException("Expected result type '" + resultClass.getName() + "' but was null");

            MetaEntity entity = optionalEntity.get();
            if (resultClass != entity.getEntityClass())
                throw new IllegalArgumentException("Expected result type '" + resultClass.getName() + "' but was '" + entity.getEntityClass().getName() + "'");
        }

        try {
            return runQuery(statementParameters, hints);
        } catch (Exception e) {
            throw new PersistenceException(e.getMessage());
        }
    }


    @Override
    public List<?> selectJpql(
            String jpqlStatement,
            Map<Parameter<?>, Object> parameterMap,
            Map<String, Object> hints,
            Class<?> resultClass) throws Exception {
        StatementParameters statementParameters;
        try {
            log.debug("Select Jpql -> Start Parsing");
            statementParameters = jpqlModule.parse(jpqlStatement, hints);
            log.debug("Select Jpql -> End Parsing");
        } catch (Error e) {
            throw new PersistenceException("Jpql Parser Error: " + e.getMessage());
        }

        return selectJpql(statementParameters, parameterMap, hints, LockType.NONE, resultClass);
    }


    @Override
    public List<?> selectNative(
            NativeQuery query) throws Exception {
        log.debug("Select Native -> Query Result Class() {}", query.getResultClass());
        if (query.getResultSetMapping() != null) {
            if (persistenceUnitContext.getQueryResultMappings() == null) {
                throw new IllegalArgumentException(
                        "Result Set Mapping '" + query.getResultSetMapping() + "' not found");
            }

            String resultSetMapping = query.getResultSetMapping();
            QueryResultMapping qrm = persistenceUnitContext.getQueryResultMappings()
                    .get(resultSetMapping);
            if (qrm == null) {
                throw new IllegalArgumentException(
                        "Result Set Mapping '" + query.getResultSetMapping() + "' not found");
            }

            return runNativeQuery(query, qrm);
        }

        if (query.getResultClass() != null) {
            EntityMapping entityMapping = new EntityMapping(
                    persistenceUnitContext.getEntities().get(query.getResultClass().getName()),
                    Collections.emptyList());
            QueryResultMapping qrm = new QueryResultMapping("", List.of(entityMapping),
                    Collections.emptyList(), Collections.emptyList());
            return runNativeQuery(query, qrm);
        }

        String sqlString = query.getSql();
        Set<Parameter<?>> parameters = ((Query) query).getParameters();
        if (parameters.isEmpty()) {
            List<Object> objects = new ArrayList<>();
            nativeRecordBuilder.setCollection(objects);
            dbConfiguration.getJdbcRunner().runNativeQuery(connectionHolder.getConnection(), sqlString,
                    null, nativeRecordBuilder);
            return objects;
        }

        List<QueryParameterData> indexParameters = ParameterUtils.findIndexParameters(
                parameters, sqlString);
        String sql = ParameterUtils.replaceParameterPlaceholders(sqlString, indexParameters);
        List<Object> parameterValues = ParameterUtils.sortParameterValues((Query) query, indexParameters);
        List<Object> objects = new ArrayList<>();
        nativeRecordBuilder.setCollection(objects);
        dbConfiguration.getJdbcRunner()
                .runNativeQuery(connectionHolder.getConnection(), sql, parameterValues,
                        nativeRecordBuilder);
        return objects;
    }


    private List<Object> runNativeQuery(
            NativeQuery query,
            QueryResultMapping queryResultMapping) throws Exception {
        String sqlString = query.getSql();
        entityHandler.setLockType(LockType.NONE);
        Set<Parameter<?>> parameters = ((Query) query).getParameters();
        if (parameters.isEmpty()) {
            qrmRecordBuilder.setQueryResultMapping(queryResultMapping);
            List<Object> objects = new ArrayList<>();
            qrmRecordBuilder.setCollection(objects);
            qrmRecordBuilder.setEntityContainer(entityContainer);
            dbConfiguration.getJdbcRunner()
                    .runNativeQuery(connectionHolder.getConnection(), sqlString, null,
                            qrmRecordBuilder);
            return objects;
        }

        List<QueryParameterData> indexParameters = ParameterUtils.findIndexParameters(
                parameters,
                sqlString);
        String sql = ParameterUtils.replaceParameterPlaceholders(sqlString, indexParameters);
        List<Object> parameterValues = ParameterUtils.sortParameterValues((Query) query, indexParameters);

        qrmRecordBuilder.setLockType(LockType.NONE);
        qrmRecordBuilder.setQueryResultMapping(queryResultMapping);
        List<Object> objects = new ArrayList<>();
        qrmRecordBuilder.setCollection(objects);
        qrmRecordBuilder.setEntityContainer(entityContainer);
        dbConfiguration.getJdbcRunner()
                .runNativeQuery(connectionHolder.getConnection(), sql, parameterValues,
                        qrmRecordBuilder);
        return objects;
    }


    @Override
    public int update(String sqlString, Query query) throws Exception {
        return dbConfiguration.getJdbcRunner().update(connectionHolder.getConnection(), sqlString,
                Collections.emptyList());
    }


    @Override
    public int update(UpdateQuery updateQuery) throws Exception {
        if (updateQuery.getCriteriaUpdate().getRoot() == null) {
            throw new IllegalArgumentException("Criteria Update Root not defined");
        }

        List<QueryParameter> parameters = dbConfiguration.getSqlStatementFactory()
                .createUpdateParameters(updateQuery);
        SqlUpdate sqlUpdate = dbConfiguration.getSqlStatementFactory().update(updateQuery, parameters,
                persistenceUnitContext.getAliasGenerator());
        String sql = dbConfiguration.getSqlStatementGenerator().export(sqlUpdate);
        return dbConfiguration.getJdbcRunner()
                .update(connectionHolder.getConnection(), sql, parameters);
    }

    @Override
    public int delete(DeleteQuery deleteQuery) throws Exception {
        if (deleteQuery.getCriteriaDelete().getRoot() == null) {
            throw new IllegalArgumentException("Criteria Delete Root not defined");
        }

        StatementParameters statementParameters = dbConfiguration.getSqlStatementFactory()
                .delete(deleteQuery,
                        persistenceUnitContext.getAliasGenerator());
        SqlDelete sqlDelete = (SqlDelete) statementParameters.getSqlStatement();
        String sql = dbConfiguration.getSqlStatementGenerator().export(sqlDelete);
        return dbConfiguration.getJdbcRunner().delete(sql, connectionHolder.getConnection(),
                statementParameters.getParameters());
    }

}
