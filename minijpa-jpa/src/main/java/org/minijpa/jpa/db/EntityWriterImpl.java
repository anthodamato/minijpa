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
import java.util.stream.Collectors;

import javax.persistence.OptimisticLockException;

import org.minijpa.jdbc.AbstractAttribute;
import org.minijpa.jdbc.AttributeUtil;
import org.minijpa.jdbc.CollectionUtils;
import org.minijpa.jdbc.ConnectionHolder;
import org.minijpa.jdbc.EntityLoader;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.ModelValueArray;
import org.minijpa.jdbc.Pk;
import org.minijpa.jdbc.PkStrategy;
import org.minijpa.jdbc.QueryParameter;
import org.minijpa.jdbc.relationship.RelationshipJoinTable;
import org.minijpa.jpa.MetaEntityHelper;
import org.minijpa.metadata.PersistenceUnitContext;
import org.minijpa.sql.model.FromTable;
import org.minijpa.sql.model.SqlDelete;
import org.minijpa.sql.model.SqlInsert;
import org.minijpa.sql.model.SqlUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class EntityWriterImpl implements EntityWriter {

	private final Logger LOG = LoggerFactory.getLogger(EntityWriterImpl.class);
	private final PersistenceUnitContext persistenceUnitContext;
	private final EntityContainer entityContainer;
	private final SqlStatementFactory sqlStatementFactory;
	private final DbConfiguration dbConfiguration;
	private final EntityLoader entityLoader;
	private final ConnectionHolder connectionHolder;

	public EntityWriterImpl(PersistenceUnitContext persistenceUnitContext,
			EntityContainer entityContainer,
			SqlStatementFactory sqlStatementFactory,
			DbConfiguration dbConfiguration,
			EntityLoader entityLoader,
			ConnectionHolder connectionHolder) {
		this.persistenceUnitContext = persistenceUnitContext;
		this.entityContainer = entityContainer;
		this.sqlStatementFactory = sqlStatementFactory;
		this.dbConfiguration = dbConfiguration;
		this.entityLoader = entityLoader;
		this.connectionHolder = connectionHolder;
	}

	@Override
	public void persist(MetaEntity entity, Object entityInstance,
			ModelValueArray<MetaAttribute> modelValueArray) throws Exception {
		EntityStatus entityStatus = MetaEntityHelper.getEntityStatus(entity, entityInstance);
		LOG.debug("persist: entityStatus={}", entityStatus);
		if (MetaEntityHelper.isFlushed(entity, entityInstance)) {
			update(entity, entityInstance, modelValueArray);
		} else {
			insert(entity, entityInstance, modelValueArray);
		}
	}

//    private void checkOptimisticLock(MetaEntity entity, Object entityInstance, Object idValue)
//	    throws Exception {
//	if (!metaEntityHelper.hasOptimisticLock(entity, entityInstance))
//	    return;
//
//	Object currentVersionValue = entity.getVersionAttribute().get().getReadMethod().invoke(entityInstance);
//	Object dbVersionValue = entityLoader.queryVersionValue(entity, idValue, LockType.NONE);
//
//	Object instance = entityLoader.findById(entity, idValue, LockType.NONE);
//	Object currentVersionValue2 = entity.getVersionAttribute().get().getReadMethod().invoke(instance);
//
//	LOG.debug("checkOptimisticLock: dbVersionValue={}", dbVersionValue + "; currentVersionValue={}", currentVersionValue);
//	if (dbVersionValue == null || !dbVersionValue.equals(currentVersionValue))
//	    throw new OptimisticLockException("Entity was written by another transaction, version" + dbVersionValue);
//    }
	protected void update(MetaEntity entity, Object entityInstance,
			ModelValueArray<MetaAttribute> modelValueArray) throws Exception {
		// It's an update.
		if (modelValueArray.isEmpty())
			return;

		Object idValue = AttributeUtil.getIdValue(entity, entityInstance);
//	checkOptimisticLock(entity, entityInstance, idValue);
		LOG.debug("update: idValue={}", idValue);
		List<QueryParameter> idParameters = MetaEntityHelper.convertAVToQP(entity.getId(), idValue);
		List<String> idColumns = idParameters.stream().map(p -> p.getColumnName()).collect(Collectors.toList());
		if (MetaEntityHelper.hasOptimisticLock(entity, entityInstance)) {
			idColumns.add(entity.getVersionAttribute().get().getColumnName());
		}

		MetaEntityHelper.createVersionAttributeArrayEntry(entity, entityInstance, modelValueArray);
		List<QueryParameter> parameters = MetaEntityHelper.convertAVToQP(modelValueArray);
		List<String> columns = parameters.stream().map(p -> {
			return p.getColumnName();
		}).collect(Collectors.toList());

		SqlUpdate sqlUpdate = sqlStatementFactory.generateUpdate(entity, columns,
				idColumns, persistenceUnitContext.getTableAliasGenerator());

		modelValueArray.add(entity.getId().getAttribute(), idValue);
		if (MetaEntityHelper.hasOptimisticLock(entity, entityInstance)) {
			Object currentVersionValue = entity.getVersionAttribute().get().getReadMethod().invoke(entityInstance);
			modelValueArray.add(entity.getVersionAttribute().get(), currentVersionValue);
		}

		parameters = MetaEntityHelper.convertAVToQP(modelValueArray);

		String sql = dbConfiguration.getSqlStatementGenerator().export(sqlUpdate);
		int updateCount = dbConfiguration.getJdbcRunner().update(connectionHolder.getConnection(), sql, parameters);
		if (updateCount == 0) {
			if (entity.getVersionAttribute().isPresent()) {
				Object currentVersionValue = entity.getVersionAttribute().get().getReadMethod().invoke(entityInstance);
				throw new OptimisticLockException("Entity was written by another transaction, version" + currentVersionValue);
			}
		}

		LOG.debug("update: updateCount={}", updateCount);
		MetaEntityHelper.updateVersionAttributeValue(entity, entityInstance);
	}

	protected void insert(MetaEntity entity, Object entityInstance,
			ModelValueArray<MetaAttribute> modelValueArray) throws Exception {
		Pk pk = entity.getId();
//	LOG.info("persist: id.getPkGeneration()={}", id.getPkGeneration());
		PkStrategy pkStrategy = pk.getPkGeneration().getPkStrategy();
//	LOG.info("Primary Key Generation Strategy: " + pkStrategy);
		if (pkStrategy == PkStrategy.IDENTITY) {
			List<QueryParameter> parameters = MetaEntityHelper.convertAVToQP(modelValueArray);
			// version attribute
			Optional<QueryParameter> optVersion = MetaEntityHelper.generateVersionParameter(entity);
			if (optVersion.isPresent())
				parameters.add(optVersion.get());

			List<String> columns = parameters.stream().map(p -> {
				return p.getColumnName();
			}).collect(Collectors.toList());

			int pkIndex = modelValueArray.indexOfModel(pk.getAttribute());
			SqlInsert sqlInsert = sqlStatementFactory.generateInsert(
					entity, columns, true, pkIndex == -1, Optional.of(entity), persistenceUnitContext.getTableAliasGenerator());
			String sql = dbConfiguration.getSqlStatementGenerator().export(sqlInsert);
			String[] ids = {pk.getAttribute().getColumnName()};
			Object pkId = null;
//	    try {
			pkId = dbConfiguration.getJdbcRunner().insertReturnGeneratedKeys(connectionHolder.getConnection(), sql, parameters, pk);
//	    } catch (Exception e) {
//		LOG.debug(e.getMessage());
//	    }

//	    Object idv = entity.getId().getAttribute().getDbTypeMapper().convertGeneratedKey(pkId, entity.getId().getType());
			Object idv = entity.getId().convertGeneratedKey(pkId);
			LOG.info("persist: pk={}", pkId);
			if (pkId != null)
				LOG.info("persist: pk.getClass()={}", pkId.getClass());

			entity.getId().getWriteMethod().invoke(entityInstance, idv);

			updatePostponedJoinColumnUpdate(entity, entityInstance);
			if (optVersion.isPresent()) {
				entity.getVersionAttribute().get().getWriteMethod().invoke(entityInstance, optVersion.get().getValue());
			}
		} else {
			Object idValue = pk.getReadMethod().invoke(entityInstance);
			List<QueryParameter> idParameters = MetaEntityHelper.convertAVToQP(pk, idValue);
			List<QueryParameter> parameters = MetaEntityHelper.convertAVToQP(modelValueArray);
			parameters.addAll(0, idParameters);
			// version attribute
			Optional<QueryParameter> optVersion = MetaEntityHelper.generateVersionParameter(entity);
			if (optVersion.isPresent())
				parameters.add(optVersion.get());

			List<String> columns = parameters.stream().map(p -> {
				return p.getColumnName();
			}).collect(Collectors.toList());

			SqlInsert sqlInsert = sqlStatementFactory.generateInsert(entity, columns, false, false, Optional.empty(), persistenceUnitContext.getTableAliasGenerator());
			String sql = dbConfiguration.getSqlStatementGenerator().export(sqlInsert);
			dbConfiguration.getJdbcRunner().insert(connectionHolder.getConnection(), sql, parameters);
			if (optVersion.isPresent()) {
				entity.getVersionAttribute().get().getWriteMethod().invoke(entityInstance, optVersion.get().getValue());
			}
		}
	}

	private void updatePostponedJoinColumnUpdate(MetaEntity entity, Object entityInstance)
			throws Exception {
		LOG.debug("updatePostponedJoinColumnUpdate: entity.getName()={}", entity.getName());
		LOG.debug("updatePostponedJoinColumnUpdate: entity.getJoinColumnPostponedUpdateAttributeReadMethod().isEmpty()={}", entity.getJoinColumnPostponedUpdateAttributeReadMethod().isEmpty());
//	if (entity.getJoinColumnPostponedUpdateAttributeReadMethod().isEmpty())
//	    return;

		List list = MetaEntityHelper.getJoinColumnPostponedUpdateAttributeList(entity, entityInstance);
		LOG.debug("updatePostponedJoinColumnUpdate: list.isEmpty()={}", list.isEmpty());
		if (list.isEmpty())
			return;

		for (Object o : list) {
			PostponedUpdateInfo postponedUpdateInfo = (PostponedUpdateInfo) o;
			LOG.debug("updatePostponedJoinColumnUpdate: postponedUpdateInfo.getC()={}", postponedUpdateInfo.getC());
			Object instance = entityContainer.find(postponedUpdateInfo.getC(), postponedUpdateInfo.getId());
			MetaEntity toEntity = persistenceUnitContext.getEntities().get(postponedUpdateInfo.getC().getName());
			LOG.debug("updatePostponedJoinColumnUpdate: toEntity={}", toEntity);
			LOG.debug("updatePostponedJoinColumnUpdate: postponedUpdateInfo.getAttributeName()={}", postponedUpdateInfo.getAttributeName());
			Optional<MetaAttribute> optional = toEntity.findJoinColumnMappingAttribute(postponedUpdateInfo.getAttributeName());
			LOG.debug("updatePostponedJoinColumnUpdate: optional.isEmpty()={}", optional.isEmpty());
			if (optional.isEmpty())
				continue;

			ModelValueArray<MetaAttribute> modelValueArray = new ModelValueArray<>();
			modelValueArray.add(optional.get(), entityInstance);
			update(toEntity, instance, modelValueArray);
		}

		list.clear();
	}

	@Override
	public void persistJoinTableAttributes(MetaEntity entity, Object entityInstance) throws Exception {
		Object idValue = AttributeUtil.getIdValue(entity, entityInstance);
		List<QueryParameter> idParameters = MetaEntityHelper.convertAVToQP(entity.getId(), idValue);

		for (MetaAttribute a : entity.getRelationshipAttributes()) {
			if (a.getRelationship().getJoinTable() != null && a.getRelationship().isOwner()) {
				// removes the join table records first
				removeJoinTableRecords(entity, idValue, idParameters, a.getRelationship().getJoinTable());

				Object attributeInstance = MetaEntityHelper.getAttributeValue(entityInstance, a);
//		LOG.info("persist: attributeInstance={}", attributeInstance);
//		LOG.info("persist: attributeInstance.getClass()={}", attributeInstance.getClass());
				if (CollectionUtils.isCollectionClass(attributeInstance.getClass())
						&& !CollectionUtils.isCollectionEmpty(attributeInstance)) {
					Collection<?> ees = CollectionUtils.getCollectionFromCollectionOrMap(attributeInstance);
					if (entityContainer.isManaged(ees))
						persistJoinTableAttributes(ees, a, entityInstance);
				}
			}
		}
	}

	private void persistJoinTableAttributes(Collection<?> ees, MetaAttribute a, Object entityInstance) throws Exception {
		// persist every entity instance
		RelationshipJoinTable relationshipJoinTable = a.getRelationship().getJoinTable();
		for (Object instance : ees) {
			List<QueryParameter> parameters = sqlStatementFactory.createRelationshipJoinTableParameters(
					relationshipJoinTable, entityInstance, instance);
			List<String> columnNames = parameters.stream().map(p -> p.getColumnName()).collect(Collectors.toList());
			SqlInsert sqlInsert = sqlStatementFactory.generateJoinTableInsert(relationshipJoinTable, columnNames);
			String sql = dbConfiguration.getSqlStatementGenerator().export(sqlInsert);
			dbConfiguration.getJdbcRunner().insert(connectionHolder.getConnection(), sql, parameters);
		}
	}

	private List<RelationshipJoinTable> findRelationshipJoinTable(MetaEntity entity) {
		List<RelationshipJoinTable> joinTables = new ArrayList<>();
		persistenceUnitContext.getEntities().values().forEach(e -> {
			List<MetaAttribute> attributes = e.getRelationshipAttributes();
			List<RelationshipJoinTable> relationshipJoinTables = attributes.stream().
					filter(a -> a.getRelationship().getJoinTable() != null).
					map(a -> a.getRelationship().getJoinTable()).collect(Collectors.toList());
			for (RelationshipJoinTable relationshipJoinTable : relationshipJoinTables) {
				if (relationshipJoinTable.getOwningEntity() == entity || relationshipJoinTable.getTargetEntity() == entity) {
					Optional<RelationshipJoinTable> optional = joinTables.stream().filter(j -> j.getTableName().equals(relationshipJoinTable.getTableName())).findFirst();
					if (optional.isEmpty())
						joinTables.add(relationshipJoinTable);
				}
			}
		});

		return joinTables;
	}

	private void removeJoinTableRecords(
			MetaEntity entity,
			Object primaryKey,
			List<QueryParameter> idParameters) throws Exception {
		List<RelationshipJoinTable> relationshipJoinTables = findRelationshipJoinTable(entity);
		LOG.debug("removeJoinTableRecords: relationshipJoinTables.size()={}", relationshipJoinTables.size());
		for (RelationshipJoinTable relationshipJoinTable : relationshipJoinTables) {
			removeJoinTableRecords(entity, primaryKey, idParameters, relationshipJoinTable);
		}
	}

	private void removeJoinTableRecords(
			MetaEntity entity,
			Object primaryKey,
			List<QueryParameter> idParameters,
			RelationshipJoinTable relationshipJoinTable) throws Exception {
		if (relationshipJoinTable.getOwningEntity() == entity) {
			ModelValueArray<AbstractAttribute> modelValueArray = sqlStatementFactory.expandJoinColumnAttributes(entity.getId(), primaryKey,
					relationshipJoinTable.getOwningJoinColumnMapping().getJoinColumnAttributes());
			List<AbstractAttribute> attributes = modelValueArray.getModels();

			List<String> idColumns = attributes.stream().map(p -> p.getColumnName()).collect(Collectors.toList());
			FromTable fromTable = FromTable.of(relationshipJoinTable.getTableName());
			SqlDelete sqlDelete = sqlStatementFactory.generateDeleteById(fromTable, idColumns);
			String sql = dbConfiguration.getSqlStatementGenerator().export(sqlDelete);
			dbConfiguration.getJdbcRunner().delete(sql, connectionHolder.getConnection(), idParameters);
		} else {
			ModelValueArray<AbstractAttribute> modelValueArray = sqlStatementFactory.expandJoinColumnAttributes(entity.getId(), primaryKey,
					relationshipJoinTable.getTargetJoinColumnMapping().getJoinColumnAttributes());
			List<AbstractAttribute> attributes = modelValueArray.getModels();

			List<String> idColumns = attributes.stream().map(p -> p.getColumnName()).collect(Collectors.toList());
			FromTable fromTable = FromTable.of(relationshipJoinTable.getTableName());
			SqlDelete sqlDelete = sqlStatementFactory.generateDeleteById(fromTable, idColumns);
			String sql = dbConfiguration.getSqlStatementGenerator().export(sqlDelete);
			dbConfiguration.getJdbcRunner().delete(sql, connectionHolder.getConnection(), idParameters);
		}
	}

	@Override
	public void delete(Object entityInstance, MetaEntity e) throws Exception {
		Object idValue = AttributeUtil.getIdValue(e, entityInstance);
		LOG.debug("delete: idValue={}", idValue);

		List<QueryParameter> idParameters = MetaEntityHelper.convertAVToQP(e.getId(), idValue);
		removeJoinTableRecords(e, idValue, idParameters);
		if (MetaEntityHelper.hasOptimisticLock(e, entityInstance)) {
			Object currentVersionValue = e.getVersionAttribute().get().getReadMethod().invoke(entityInstance);
			idParameters.addAll(MetaEntityHelper.convertAVToQP(e.getVersionAttribute().get(), currentVersionValue));
		}

		List<String> idColumns = idParameters.stream().map(p -> p.getColumnName()).collect(Collectors.toList());

		SqlDelete sqlDelete = sqlStatementFactory.generateDeleteById(e, idColumns, persistenceUnitContext.getTableAliasGenerator());
		String sql = dbConfiguration.getSqlStatementGenerator().export(sqlDelete);
		dbConfiguration.getJdbcRunner().delete(sql, connectionHolder.getConnection(), idParameters);
	}

}
