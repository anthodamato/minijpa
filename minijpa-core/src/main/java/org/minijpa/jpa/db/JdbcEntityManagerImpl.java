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

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.persistence.Parameter;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.criteria.CompoundSelection;
import javax.persistence.criteria.CriteriaQuery;

import org.minijpa.jdbc.ConnectionHolder;
import org.minijpa.jdbc.FetchParameter;
import org.minijpa.jdbc.JdbcRecordBuilder;
import org.minijpa.jdbc.JdbcRunner;
import org.minijpa.jdbc.JdbcRunner.JdbcNativeRecordBuilder;
import org.minijpa.jdbc.ModelValueArray;
import org.minijpa.jdbc.PkSequenceGenerator;
import org.minijpa.jdbc.QueryParameter;
import org.minijpa.jdbc.db.SqlSelectData;
import org.minijpa.jdbc.mapper.AttributeMapper;
import org.minijpa.jpa.DeleteQuery;
import org.minijpa.jpa.MetaEntityHelper;
import org.minijpa.jpa.MiniNativeQuery;
import org.minijpa.jpa.MiniTypedQuery;
import org.minijpa.jpa.ParameterUtils;
import org.minijpa.jpa.TupleImpl;
import org.minijpa.jpa.UpdateQuery;
import org.minijpa.jpa.model.MetaAttribute;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.jpa.model.relationship.Cascade;
import org.minijpa.jpa.model.relationship.JoinColumnAttribute;
import org.minijpa.jpa.model.relationship.JoinColumnMapping;
import org.minijpa.metadata.PersistenceUnitContext;
import org.minijpa.sql.model.SqlDelete;
import org.minijpa.sql.model.SqlUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcEntityManagerImpl implements JdbcEntityManager {

  private static final Logger LOG = LoggerFactory.getLogger(JdbcEntityManagerImpl.class);
  protected DbConfiguration dbConfiguration;
  protected PersistenceUnitContext persistenceUnitContext;
  private final EntityContainer entityContainer;
  protected ConnectionHolder connectionHolder;
  private final EntityHandler entityHandler;
  private final JpqlModule jpqlModule;
  private final JdbcFPRecordBuilder jdbcFPRecordBuilder = new JdbcFPRecordBuilder();
  private final JdbcRunner.JdbcRecordBuilderValue jdbcJpqlRecordBuilder = new JdbcRunner.JdbcRecordBuilderValue();
  private final JdbcTupleRecordBuilder jdbcTupleRecordBuilder = new JdbcTupleRecordBuilder();
  private final JdbcNativeRecordBuilder nativeRecordBuilder = new JdbcNativeRecordBuilder();
  private final JdbcQRMRecordBuilder qrmRecordBuilder = new JdbcQRMRecordBuilder();
  private final JdbcFJRecordBuilder jdbcFJRecordBuilder = new JdbcFJRecordBuilder();

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

  public Object findById(Class<?> entityClass, Object primaryKey, LockType lockType)
      throws Exception {
    LOG.debug("findById: primaryKey={}", primaryKey);

    MetaEntity entity = persistenceUnitContext.getEntities().get(entityClass.getName());
    if (entity == null) {
      throw new IllegalArgumentException(
          "Class '" + entityClass.getName() + "' is not an entity");
    }

    LOG.debug("findById: entity={}", entity);
    return entityHandler.findById(entity, primaryKey, lockType);
  }

  public void refresh(Object entityInstance, LockType lockType) throws Exception {
    Class<?> entityClass = entityInstance.getClass();
    MetaEntity entity = persistenceUnitContext.getEntities().get(entityClass.getName());
    if (entity == null) {
      throw new IllegalArgumentException(
          "Class '" + entityClass.getName() + "' is not an entity");
    }

    if (!entityContainer.isManaged(entityInstance)) {
      throw new IllegalArgumentException("Entity '" + entityInstance + "' is not managed");
    }

    Object primaryKey = AttributeUtil.getIdValue(entity, entityInstance);
    entityHandler.refresh(entity, entityInstance, primaryKey, lockType);

    // cascades
    List<MetaAttribute> cascadeAttributes = entity.getCascadeAttributes(Cascade.ALL,
        Cascade.REFRESH);
    for (MetaAttribute attribute : cascadeAttributes) {
      Object attributeInstance = MetaEntityHelper.getAttributeValue(entityInstance, attribute);
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

  public void lock(Object entityInstance, LockType lockType) throws Exception {
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
    Object primaryKey = AttributeUtil.getIdValue(entity, entityInstance);
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
  public void persist(MetaEntity entity, Object entityInstance, MiniFlushMode miniFlushMode)
      throws Exception {
    Object idValue = entity.getId().getReadMethod().invoke(entityInstance);
    if (idValue == null && entity.getId().getPkGeneration().getPkStrategy() == PkStrategy.PLAIN) {
      throw new PersistenceException(
          "Id must be manually assigned for '" + entity.getEntityClass().getName() + "'");
    }

    ModelValueArray<MetaAttribute> modelValueArray = MetaEntityHelper.getModifications(entity,
        entityInstance);
    checkNullableAttributes(entity, entityInstance, modelValueArray);
    if (idValue == null) {
      if (entity.getId().getPkGeneration().getPkStrategy() == PkStrategy.SEQUENCE) {
        PkSequenceGenerator pkSequenceGenerator = entity.getId().getPkGeneration()
            .getPkSequenceGenerator();
        String seqStm = dbConfiguration.getSqlStatementGenerator()
            .sequenceNextValueStatement(Optional.empty(),
                pkSequenceGenerator.getSequenceName());

        idValue = dbConfiguration.getJdbcRunner()
            .generateNextSequenceValue(connectionHolder.getConnection(),
                seqStm);
        entity.getId().getWriteMethod().invoke(entityInstance, idValue);
      } else if (entity.getId().getPkGeneration().getPkStrategy() == PkStrategy.IDENTITY) {
        List<Object> managedEntityList = entityContainer.getManagedEntityList();
        persistEarlyInsertEntityInstance(entity, modelValueArray, managedEntityList);
        entityHandler.persist(entity, entityInstance, modelValueArray);
        MetaEntityHelper.setEntityStatus(entity, entityInstance, EntityStatus.FLUSHED);
        idValue = AttributeUtil.getIdValue(entity, entityInstance);
        addInfoForPostponedUpdateEntities(idValue, entity, modelValueArray);
        MetaEntityHelper.removeChanges(entity, entityInstance);
      }
    }

    EntityStatus entityStatus = MetaEntityHelper.getEntityStatus(entity, entityInstance);
    if (entityStatus == EntityStatus.NEW) {
      MetaEntityHelper.setEntityStatus(entity, entityInstance,
          EntityStatus.PERSIST_NOT_FLUSHED);
    }

    entityContainer.addManaged(entityInstance, idValue);

    // cascades
    List<MetaAttribute> cascadeAttributes = entity.getCascadeAttributes(Cascade.ALL,
        Cascade.PERSIST);
    for (MetaAttribute attribute : cascadeAttributes) {
      Object attributeInstance = MetaEntityHelper.getAttributeValue(entityInstance, attribute);
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

  private void addInfoForPostponedUpdateEntities(Object idValue, MetaEntity entity,
      ModelValueArray<MetaAttribute> modelValueArray)
      throws IllegalAccessException, InvocationTargetException {
    for (JoinColumnMapping joinColumnMapping : entity.getJoinColumnMappings()) {
      int index = modelValueArray.indexOfModel(joinColumnMapping.getAttribute());
      LOG.debug("addInfoForPostponedUpdateEntities: index={}", index);
      LOG.debug("addInfoForPostponedUpdateEntities: joinColumnMapping.getAttribute()={}",
          joinColumnMapping.getAttribute());
      if (index != -1) {
        Object instance = modelValueArray.getValue(index);
        LOG.debug("addInfoForPostponedUpdateEntities: instance={}", instance);
        MetaEntity e = persistenceUnitContext.getEntities().get(instance.getClass().getName());
        LOG.debug("addInfoForPostponedUpdateEntities: e={}", e);
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
  private void checkNullableAttributes(MetaEntity entity, Object entityInstance,
      ModelValueArray<MetaAttribute> attributeValueArray) throws Exception {
    if (entityContainer.isManaged(entityInstance)) {
      EntityStatus entityStatus = MetaEntityHelper.getEntityStatus(entity, entityInstance);
      if (entityStatus == EntityStatus.FLUSHED
          || entityStatus == EntityStatus.FLUSHED_LOADED_FROM_DB) {
        // It's an update.
        // TODO. It should check that no not nullable attrs will be set to null.
        return;
      }
    }

    List<MetaAttribute> notNullableAttributes = entity.notNullableAttributes();
    if (notNullableAttributes.isEmpty()) {
      return;
    }

    if (attributeValueArray.isEmpty()) {
      throw new PersistenceException(
          "Attribute '" + notNullableAttributes.get(0).getName() + "' is null");
    }

    notNullableAttributes.forEach(a -> {
      Optional<MetaAttribute> o = attributeValueArray.getModels().stream().filter(av -> av == a)
          .findFirst();
      if (o.isEmpty()) {
        throw new PersistenceException("Attribute '" + a.getName() + "' is null");
      }
    });
  }

  @Override
  public void flush() throws Exception {
    LOG.debug("Flushing entities...");
    List<Object> managedEntityList = entityContainer.getManagedEntityList();
    // removes join table owning entity records first
    for (Object entityInstance : managedEntityList) {
      MetaEntity me = persistenceUnitContext.getEntities().get(entityInstance.getClass().getName());
      EntityStatus entityStatus = MetaEntityHelper.getEntityStatus(me, entityInstance);
      if (entityStatus == EntityStatus.REMOVED_NOT_FLUSHED) {
        LOG.debug("flush: REMOVED_NOT_FLUSHED Join Table Records entityInstance={}",
            entityInstance);
        entityHandler.removeJoinTableRecords(entityInstance, me);
      }
    }

    for (Object entityInstance : managedEntityList) {
      MetaEntity me = persistenceUnitContext.getEntities().get(entityInstance.getClass().getName());
      EntityStatus entityStatus = MetaEntityHelper.getEntityStatus(me, entityInstance);
      LOG.debug("flush: entityInstance={}; entityStatus={}", entityInstance, entityStatus);
      switch (entityStatus) {
        case FLUSHED:
        case FLUSHED_LOADED_FROM_DB:
          // makes updates
          LOG.debug("flush: FLUSHED_LOADED_FROM_DB entityInstance={}", entityInstance);
          ModelValueArray<MetaAttribute> modelValueArray = MetaEntityHelper.getModifications(me,
              entityInstance);
          LOG.debug("flush: FLUSHED_LOADED_FROM_DB modelValueArray.size()={}",
              modelValueArray.size());
          if (!modelValueArray.isEmpty()) {
            entityHandler.persist(me, entityInstance, modelValueArray);
            MetaEntityHelper.removeChanges(me, entityInstance);
          }
          break;
        case PERSIST_NOT_FLUSHED:
          LOG.debug("flush: PERSIST_NOT_FLUSHED entityInstance={}", entityInstance);
          modelValueArray = MetaEntityHelper.getModifications(me, entityInstance);
          persistEarlyInsertEntityInstance(me, modelValueArray, managedEntityList);
          entityHandler.persist(me, entityInstance, modelValueArray);
          MetaEntityHelper.setEntityStatus(me, entityInstance, EntityStatus.FLUSHED);
          MetaEntityHelper.removeChanges(me, entityInstance);
          EntityStatus es = MetaEntityHelper.getEntityStatus(me, entityInstance);
          LOG.debug("flush: es={}", es);
          break;
        case REMOVED_NOT_FLUSHED:
          LOG.debug("flush: 1 REMOVED_NOT_FLUSHED entityInstance={}", entityInstance);
          persistEarlyDeleteEntityInstance(me, entityInstance, managedEntityList);
          entityHandler.delete(entityInstance, me);
          entityContainer.removeManaged(entityInstance);
          LOG.debug("flush: 2 REMOVED_NOT_FLUSHED entityInstance={}", entityInstance);
          MetaEntityHelper.setEntityStatus(me, entityInstance, EntityStatus.REMOVED);
          break;
        case EARLY_INSERT:
          LOG.debug("flush: EARLY_INSERT entityInstance={}", entityInstance);
          MetaEntityHelper.setEntityStatus(me, entityInstance, EntityStatus.FLUSHED);
          break;
        case EARLY_REMOVE:
          LOG.debug("flush: EARLY_REMOVE entityInstance={}", entityInstance);
          MetaEntityHelper.setEntityStatus(me, entityInstance, EntityStatus.REMOVED);
          break;
      }
    }

    for (Object entityInstance : managedEntityList) {
      LOG.debug("flush: persistJoinTableAttributes entityInstance={}", entityInstance);
      MetaEntity me = persistenceUnitContext.getEntities().get(entityInstance.getClass().getName());
      EntityStatus entityStatus = MetaEntityHelper.getEntityStatus(me, entityInstance);
      LOG.debug("flush: persistJoinTableAttributes entityStatus={}", entityStatus);
      if (entityStatus == EntityStatus.FLUSHED) {
        entityHandler.persistJoinTableAttributes(me, entityInstance);
      }
    }

    LOG.debug("flush: done");
  }

  /**
   * Inserts entities related to join columns not flushed yet.
   *
   * @param me                meta entity
   * @param modelValueArray   values
   * @param managedEntityList entity list
   */
  private void persistEarlyInsertEntityInstance(MetaEntity me,
      ModelValueArray<MetaAttribute> modelValueArray,
      List<Object> managedEntityList) throws Exception {
    List<JoinColumnMapping> joinColumnMappings = me.getJoinColumnMappings();
    LOG.debug("persistEarlyInsertEntityInstance: joinColumnMappings={}", joinColumnMappings);
    if (joinColumnMappings.isEmpty()) {
      return;
    }

    for (JoinColumnMapping joinColumnMapping : joinColumnMappings) {
      int index = modelValueArray.indexOfModel(joinColumnMapping.getAttribute());
      LOG.debug("persistEarlyInsertEntityInstance: index={}", index);
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

        ModelValueArray<MetaAttribute> mva = MetaEntityHelper.getModifications(metaEntity,
            instance);
        entityHandler.persist(metaEntity, instance, mva);
        LOG.debug("persistEarlyInsertEntityInstance: instance={}", instance);
        MetaEntityHelper.setEntityStatus(metaEntity, instance, EntityStatus.EARLY_INSERT);
        MetaEntityHelper.removeChanges(metaEntity, instance);
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
  private void persistEarlyDeleteEntityInstance(MetaEntity me, Object entityInstance,
      List<Object> managedEntityList)
      throws Exception {
    List<MetaAttribute> relationshipAttributes = me.getRelationshipAttributes();
    for (MetaAttribute relationshipAttribute : relationshipAttributes) {
      LOG.debug("persistEarlyDeleteEntityInstance: relationshipAttribute={}",
          relationshipAttribute);
      LOG.debug("persistEarlyDeleteEntityInstance: relationshipAttribute.getRelationship()="
          + relationshipAttribute.getRelationship());
      LOG.debug(
          "persistEarlyDeleteEntityInstance: relationshipAttribute.getRelationship().isOwner()="
              + relationshipAttribute.getRelationship().isOwner());
      if (!relationshipAttribute.getRelationship().isOwner()
          && relationshipAttribute.getRelationship().toOne()) {
        LOG.debug("persistEarlyDeleteEntityInstance: 1");
        Object instance = MetaEntityHelper.getAttributeValue(entityInstance, relationshipAttribute);
        LOG.debug("persistEarlyDeleteEntityInstance: instance={}", instance);
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
    if (entityContainer.isManaged(entity)) {
      LOG.debug("Instance " + entity + " is in the persistence context");
      entityContainer.markForRemoval(entity);
      LOG.debug("remove: entity={}", entity);
      // cascades
      List<MetaAttribute> cascadeAttributes = e.getCascadeAttributes(Cascade.ALL, Cascade.REMOVE);
      for (MetaAttribute attribute : cascadeAttributes) {
        if (!attribute.getRelationship().fromOne()) {
          continue;
        }

        Object attributeInstance = MetaEntityHelper.getAttributeValue(entity, attribute);
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
      LOG.debug("Instance " + entity + " not found in the persistence context");
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
    List<MetaAttribute> cascadeAttributes = e.getCascadeAttributes(Cascade.ALL, Cascade.DETACH);
    for (MetaAttribute attribute : cascadeAttributes) {
      Object attributeInstance = MetaEntityHelper.getAttributeValue(entity, attribute);
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

  @Override
  public List<?> select(Query query) throws Exception {
    CriteriaQuery<?> criteriaQuery = ((MiniTypedQuery<?>) query).getCriteriaQuery();
    StatementParameters statementParameters = dbConfiguration.getSqlStatementFactory().select(query,
        persistenceUnitContext.getAliasGenerator());
    LOG.debug("select: statementParameters={}", statementParameters);
    SqlSelectData sqlSelectData = (SqlSelectData) statementParameters.getSqlStatement();
    String sql = dbConfiguration.getSqlStatementGenerator().export(sqlSelectData);
    sqlSelectData.getFetchParameters().forEach(f -> LOG.debug("select: f={}", f));
    LOG.debug("select: sql={}", sql);
    LOG.debug("select: sqlSelectData.getResult()={}", sqlSelectData.getResult());
    LOG.debug("select: statementParameters.getStatementType()={}",
        statementParameters.getStatementType());
    if (statementParameters.getStatementType() == StatementType.FETCH_JOIN) {
      Collection<Object> collectionResult = (Collection<Object>) CollectionUtils.createInstance(
          null,
          CollectionUtils.findCollectionImplementationClass(List.class));
      LOG.debug("select: collectionResult={}", collectionResult);

      Optional<MetaEntity> optionalEntity = persistenceUnitContext
          .findMetaEntityByTableName(sqlSelectData.getResult().getName());
      MetaEntity entity = optionalEntity.get();
      entityHandler.setLockType(LockType.NONE);
      jdbcFJRecordBuilder.setCollectionResult(collectionResult);
      jdbcFJRecordBuilder.setEntityLoader(entityHandler);
      jdbcFJRecordBuilder.setMetaEntity(entity);
      jdbcFJRecordBuilder.setFetchJoinMetaEntities(statementParameters.getFetchJoinMetaEntities());
      jdbcFJRecordBuilder.setFetchJoinMetaAttributes(
          statementParameters.getFetchJoinMetaAttributes());
      jdbcFJRecordBuilder.setFetchParameters(sqlSelectData.getFetchParameters());
      jdbcFJRecordBuilder.setDistinct(sqlSelectData.isDistinct());

      dbConfiguration.getJdbcRunner().runQuery(connectionHolder.getConnection(), sql,
          statementParameters.getParameters(), jdbcFJRecordBuilder);
      return (List<?>) collectionResult;
    }

    if (sqlSelectData.getResult() != null) {
      Collection<Object> collectionResult = (Collection<Object>) CollectionUtils.createInstance(
          null,
          CollectionUtils.findCollectionImplementationClass(List.class));
      LOG.debug("select: collectionResult={}", collectionResult);

      Optional<MetaEntity> optionalEntity = persistenceUnitContext
          .findMetaEntityByTableName(sqlSelectData.getResult().getName());
      MetaEntity entity = optionalEntity.get();
      entityHandler.setLockType(LockType.NONE);
      jdbcFPRecordBuilder.setCollectionResult(collectionResult);
      jdbcFPRecordBuilder.setEntityLoader(entityHandler);
      jdbcFPRecordBuilder.setMetaEntity(entity);
      jdbcFPRecordBuilder.setFetchParameters(sqlSelectData.getFetchParameters());

      dbConfiguration.getJdbcRunner().runQuery(connectionHolder.getConnection(), sql,
          statementParameters.getParameters(), jdbcFPRecordBuilder);
      return (List<?>) collectionResult;
    }

    if (criteriaQuery.getResultType() == Tuple.class) {
      if (!(criteriaQuery.getSelection() instanceof CompoundSelection<?>)) {
        throw new IllegalArgumentException(
            "Selection '" + criteriaQuery.getSelection() + "' is not a compound selection");
      }

      List<Tuple> collectionResult = new ArrayList<>();
      jdbcTupleRecordBuilder.setSqlSelectData(sqlSelectData);
      jdbcTupleRecordBuilder.setObjects(collectionResult);
      jdbcTupleRecordBuilder.setCompoundSelection(
          (CompoundSelection<?>) criteriaQuery.getSelection());
      dbConfiguration.getJdbcRunner().runQuery(connectionHolder.getConnection(), sql,
          statementParameters.getParameters(), jdbcTupleRecordBuilder);
      return collectionResult;
    }

    // returns an aggregate expression result (max, min, etc)
    List<Object> collectionResult = new ArrayList<>();
    jdbcJpqlRecordBuilder.setFetchParameters(sqlSelectData.getFetchParameters());
    jdbcJpqlRecordBuilder.setCollectionResult(collectionResult);
    dbConfiguration.getJdbcRunner().runQuery(connectionHolder.getConnection(), sql,
        statementParameters.getParameters(), jdbcJpqlRecordBuilder);
    return collectionResult;
  }

  @Override
  public List<?> selectJpql(String jpqlStatement) throws Exception {
    JpqlResult jpqlResult = null;
    try {
      LOG.debug("selectJpql: start parsing");
      jpqlResult = jpqlModule.parse(jpqlStatement);
      LOG.debug("selectJpql: end parsing");
    } catch (Error e) {
      throw new IllegalStateException("Internal Jpql Parser Error: " + e.getMessage());
    }

    SqlSelectData sqlSelectData = (SqlSelectData) jpqlResult.getSqlStatement();
    LOG.debug("selectJpql: sqlSelectData.getResult()={}", sqlSelectData.getResult());
    if (sqlSelectData.getResult() != null) {
      Collection<Object> collectionResult = (Collection<Object>) CollectionUtils.createInstance(
          null,
          CollectionUtils.findCollectionImplementationClass(List.class));
      LOG.debug("selectJpql: collectionResult={}", collectionResult);

      Optional<MetaEntity> optionalEntity = persistenceUnitContext
          .findMetaEntityByTableName(sqlSelectData.getResult().getName());
      MetaEntity entity = optionalEntity.get();
      entityHandler.setLockType(LockType.NONE);
      jdbcFPRecordBuilder.setCollectionResult(collectionResult);
      jdbcFPRecordBuilder.setEntityLoader(entityHandler);
      jdbcFPRecordBuilder.setMetaEntity(entity);
      jdbcFPRecordBuilder.setFetchParameters(sqlSelectData.getFetchParameters());
      dbConfiguration.getJdbcRunner()
          .runQuery(connectionHolder.getConnection(), jpqlResult.getSql(),
              Collections.emptyList(), jdbcFPRecordBuilder);
      return (List<?>) collectionResult;
    }

    List<Object> collectionResult = new ArrayList<>();
    jdbcJpqlRecordBuilder.setFetchParameters(sqlSelectData.getFetchParameters());
    jdbcJpqlRecordBuilder.setCollectionResult(collectionResult);
    dbConfiguration.getJdbcRunner().runQuery(connectionHolder.getConnection(), jpqlResult.getSql(),
        new ArrayList<>(), jdbcJpqlRecordBuilder);
    return collectionResult;
  }

  @Override
  public List<?> selectNative(MiniNativeQuery query) throws Exception {
    Optional<QueryResultMapping> queryResultMapping = Optional.empty();
    if (query.getResultClass().isPresent()) {
      EntityMapping entityMapping = new EntityMapping(
          persistenceUnitContext.getEntities().get(query.getResultClass().get().getName()),
          Collections.emptyList());
      queryResultMapping = Optional.of(new QueryResultMapping("", Arrays.asList(entityMapping),
          Collections.emptyList(), Collections.emptyList()));
    }

    if (query.getResultSetMapping().isPresent()) {
      if (persistenceUnitContext.getQueryResultMappings().isEmpty()) {
        throw new IllegalArgumentException(
            "Result Set Mapping '" + query.getResultSetMapping().get() + "' not found");
      }

      String resultSetMapping = query.getResultSetMapping().get();
      QueryResultMapping qrm = persistenceUnitContext.getQueryResultMappings().get()
          .get(resultSetMapping);
      if (qrm == null) {
        throw new IllegalArgumentException(
            "Result Set Mapping '" + query.getResultSetMapping().get() + "' not found");
      }

      queryResultMapping = Optional.of(qrm);
    }

    String sqlString = query.getSqlString();
    if (queryResultMapping.isEmpty()) {
      List<Object> parameterValues = new ArrayList<>();
      Set<Parameter<?>> parameters = query.getParameters();
      if (parameters.isEmpty()) {
        List<Object> objects = new ArrayList<>();
        nativeRecordBuilder.setCollection(objects);
        dbConfiguration.getJdbcRunner().runNativeQuery(connectionHolder.getConnection(), sqlString,
            parameterValues, nativeRecordBuilder);
        return objects;
      }

      List<ParameterUtils.IndexParameter> indexParameters = ParameterUtils.findIndexParameters(
          query, sqlString);
      String sql = ParameterUtils.replaceParameterPlaceholders(query, sqlString, indexParameters);
      parameterValues = ParameterUtils.sortParameterValues(query, indexParameters);
      List<Object> objects = new ArrayList<>();
      nativeRecordBuilder.setCollection(objects);
      dbConfiguration.getJdbcRunner()
          .runNativeQuery(connectionHolder.getConnection(), sql, parameterValues,
              nativeRecordBuilder);
      return objects;
    }

    entityHandler.setLockType(LockType.NONE);
    List<Object> parameterValues = new ArrayList<>();
    Set<Parameter<?>> parameters = query.getParameters();
    if (parameters.isEmpty()) {
      qrmRecordBuilder.setEntityLoader(entityHandler);
      qrmRecordBuilder.setQueryResultMapping(queryResultMapping.get());
      List<Object> objects = new ArrayList<>();
      qrmRecordBuilder.setCollection(objects);
      dbConfiguration.getJdbcRunner()
          .runNativeQuery(connectionHolder.getConnection(), sqlString, parameterValues,
              qrmRecordBuilder);
      return objects;
    }

    List<ParameterUtils.IndexParameter> indexParameters = ParameterUtils.findIndexParameters(query,
        sqlString);
    String sql = ParameterUtils.replaceParameterPlaceholders(query, sqlString, indexParameters);
    parameterValues = ParameterUtils.sortParameterValues(query, indexParameters);

    qrmRecordBuilder.setEntityLoader(entityHandler);
    qrmRecordBuilder.setQueryResultMapping(queryResultMapping.get());
    List<Object> objects = new ArrayList<>();
    qrmRecordBuilder.setCollection(objects);
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

  private static class JdbcTupleRecordBuilder implements JdbcRecordBuilder {

    private List<Tuple> objects;
    private SqlSelectData sqlSelectData;
    private CompoundSelection<?> compoundSelection;

    public void setObjects(List<Tuple> objects) {
      this.objects = objects;
    }

    public void setSqlSelectData(SqlSelectData sqlSelectData) {
      this.sqlSelectData = sqlSelectData;
    }

    public void setCompoundSelection(CompoundSelection<?> compoundSelection) {
      this.compoundSelection = compoundSelection;
    }

    protected Object[] createRecord(int nc, List<FetchParameter> fetchParameters, ResultSet rs,
        ResultSetMetaData metaData) throws Exception {
      Object[] values = new Object[nc];
      for (int i = 0; i < nc; ++i) {
        int columnType = metaData.getColumnType(i + 1);
        Object v = JdbcRunner.getValue(rs, i + 1, columnType);
        values[i] = v;
      }

      return values;
    }

    @Override
    public void collectRecords(ResultSet rs) throws Exception {
      int nc = sqlSelectData.getValues().size();
      List<FetchParameter> fetchParameters = sqlSelectData.getFetchParameters();
      ResultSetMetaData metaData = rs.getMetaData();
      while (rs.next()) {
        Object[] values = createRecord(nc, fetchParameters, rs, metaData);
        objects.add(new TupleImpl(values, compoundSelection));
      }

    }
  }

  private static class JdbcQRMRecordBuilder implements JdbcRecordBuilder {

    private List<Object> objects;
    private QueryResultMapping queryResultMapping;
    private EntityLoader entityLoader;

    public void setQueryResultMapping(QueryResultMapping queryResultMapping) {
      this.queryResultMapping = queryResultMapping;
    }

    public void setEntityLoader(EntityLoader entityLoader) {
      this.entityLoader = entityLoader;
    }

    public void setCollection(List<Object> objects) {
      this.objects = objects;
    }

    @Override
    public void collectRecords(ResultSet rs) throws Exception {
      ResultSetMetaData metaData = rs.getMetaData();
      while (rs.next()) {
        Object record = buildRecord(metaData, rs, queryResultMapping, entityLoader);
        objects.add(record);
      }
    }

    private Object buildRecord(
        ResultSetMetaData metaData,
        ResultSet rs,
        QueryResultMapping queryResultMapping,
        EntityLoader entityLoader) throws Exception {
      ModelValueArray<FetchParameter> modelValueArray = new ModelValueArray<>();
      int nc = metaData.getColumnCount();
      for (int i = 0; i < nc; ++i) {
        String columnName = metaData.getColumnName(i + 1);
        String columnAlias = metaData.getColumnLabel(i + 1);
        Optional<FetchParameter> optional = findFetchParameter(columnName, columnAlias,
            queryResultMapping);
        if (optional.isPresent()) {
          Optional<AttributeMapper> optionalAM = optional.get().getAttributeMapper();
          Integer sqlType = optional.get().getSqlType();
          if (sqlType == null) {
            sqlType = metaData.getColumnType(i + 1);
          }

          Object v = JdbcRunner.getValueByAttributeMapper(rs, i + 1, sqlType, optionalAM);
          modelValueArray.add(optional.get(), v);
        }
      }

      int k = 0;
      Object[] result = new Object[queryResultMapping.size()];
      for (EntityMapping entityMapping : queryResultMapping.getEntityMappings()) {
        Object entityInstance = entityLoader.buildNoQueries(modelValueArray,
            entityMapping.getMetaEntity());
        result[k] = entityInstance;
        ++k;
      }

      if (result.length == 1) {
        return result[0];
      }

      return result;
    }

    private Optional<FetchParameter> findFetchParameter(String columnName, String columnAlias,
        QueryResultMapping queryResultMapping) {
      for (EntityMapping entityMapping : queryResultMapping.getEntityMappings()) {
        Optional<FetchParameter> optional = buildFetchParameter(columnName, columnAlias,
            entityMapping);
        if (optional.isPresent()) {
          return optional;
        }
      }

      return Optional.empty();
    }

    private Optional<FetchParameter> buildFetchParameter(String columnName, String columnAlias,
        EntityMapping entityMapping) {
      Optional<MetaAttribute> optional = entityMapping.getAttribute(columnAlias);
      if (optional.isPresent()) {
        MetaAttribute metaAttribute = optional.get();
        FetchParameter fetchParameter = new AttributeFetchParameterImpl(
            metaAttribute.getColumnName(),
            metaAttribute.getSqlType(), metaAttribute);
        return Optional.of(fetchParameter);
      }

      Optional<JoinColumnAttribute> optionalJoinColumn = entityMapping.getJoinColumnAttribute(
          columnName);
      if (optionalJoinColumn.isPresent()) {
        JoinColumnAttribute joinColumnAttribute = optionalJoinColumn.get();
        FetchParameter fetchParameter = new AttributeFetchParameterImpl(
            joinColumnAttribute.getColumnName(),
            joinColumnAttribute.getSqlType(), joinColumnAttribute.getAttribute());
        return Optional.of(fetchParameter);
      }

      return Optional.empty();
    }
  }

  public static class JdbcFPRecordBuilder implements JdbcRecordBuilder {

    private List<FetchParameter> fetchParameters;
    private Collection<Object> collectionResult;
    private MetaEntity metaEntity;
    private EntityLoader entityLoader;

    public void setFetchParameters(List<FetchParameter> fetchParameters) {
      this.fetchParameters = fetchParameters;
    }

    public void setCollectionResult(Collection<Object> collectionResult) {
      this.collectionResult = collectionResult;
    }

    public void setMetaEntity(MetaEntity metaEntity) {
      this.metaEntity = metaEntity;
    }

    public void setEntityLoader(EntityLoader entityLoader) {
      this.entityLoader = entityLoader;
    }

    @Override
    public void collectRecords(ResultSet rs) throws Exception {
      ResultSetMetaData metaData = rs.getMetaData();
      while (rs.next()) {
        Optional<ModelValueArray<FetchParameter>> optional = JdbcRunner
            .createModelValueArrayFromResultSetAM(fetchParameters, rs, metaData);
        if (optional.isPresent()) {
          Object instance = entityLoader.build(optional.get(), metaEntity);
          collectionResult.add(instance);
        }
      }
    }
  }

  public static class JdbcFJRecordBuilder implements JdbcRecordBuilder {

    private List<FetchParameter> fetchParameters;
    private Collection<Object> collectionResult;
    private MetaEntity metaEntity;
    private EntityLoader entityLoader;
    private List<MetaEntity> fetchJoinMetaEntities;
    private List<MetaAttribute> fetchJoinMetaAttributes;
    private boolean distinct = false;

    public void setFetchParameters(List<FetchParameter> fetchParameters) {
      this.fetchParameters = fetchParameters;
    }

    public void setCollectionResult(Collection<Object> collectionResult) {
      this.collectionResult = collectionResult;
    }

    public void setMetaEntity(MetaEntity metaEntity) {
      this.metaEntity = metaEntity;
    }

    public void setEntityLoader(EntityLoader entityLoader) {
      this.entityLoader = entityLoader;
    }

    public void setFetchJoinMetaEntities(
        List<MetaEntity> fetchJoinMetaEntities) {
      this.fetchJoinMetaEntities = fetchJoinMetaEntities;
    }

    public void setFetchJoinMetaAttributes(
        List<MetaAttribute> fetchJoinMetaAttributes) {
      this.fetchJoinMetaAttributes = fetchJoinMetaAttributes;
    }

    public void setDistinct(boolean distinct) {
      this.distinct = distinct;
    }

    @Override
    public void collectRecords(ResultSet rs) throws Exception {
      ResultSetMetaData metaData = rs.getMetaData();
      while (rs.next()) {
        Optional<ModelValueArray<FetchParameter>> optional = JdbcRunner
            .createModelValueArrayFromResultSetAM(fetchParameters, rs, metaData);
        LOG.debug("collectRecords: optional={}", optional);
        if (optional.isPresent()) {
          Object instance = entityLoader.buildEntityNoRelationshipAttributeLoading(optional.get(),
              metaEntity);
          if (distinct) {
            if (!collectionResult.contains(instance)) {
              collectionResult.add(instance);
              // set lazy loaded flag for those attributes
              for (int i = 0; i < fetchJoinMetaAttributes.size(); ++i) {
                MetaEntityHelper.lazyAttributeLoaded(metaEntity, fetchJoinMetaAttributes.get(i),
                    instance, true);
              }
            }
          } else {
            collectionResult.add(instance);
            // set lazy loaded flag for those attributes
            for (int i = 0; i < fetchJoinMetaAttributes.size(); ++i) {
              MetaEntityHelper.lazyAttributeLoaded(metaEntity, fetchJoinMetaAttributes.get(i),
                  instance, true);
            }
          }

          // set relationship attribute values
          for (int i = 0; i < fetchJoinMetaEntities.size(); ++i) {
            Object value = entityLoader.buildEntityNoRelationshipAttributeLoading(optional.get(),
                fetchJoinMetaEntities.get(i));
            MetaEntityHelper.addElementToCollectionAttribute(instance, metaEntity,
                fetchJoinMetaAttributes.get(i), value);
          }
        }
      }
    }
  }

}
