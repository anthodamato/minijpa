/*
 * Copyright (C) 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.minijpa.jpa.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.OptimisticLockException;
import org.minijpa.jdbc.AttributeUtil;
import org.minijpa.jdbc.ModelValueArray;
import org.minijpa.jdbc.CollectionUtils;
import org.minijpa.jdbc.ConnectionHolder;
import org.minijpa.jdbc.EntityLoader;
import org.minijpa.jdbc.LockType;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.MetaEntityHelper;
import org.minijpa.jdbc.PkStrategy;
import org.minijpa.jdbc.QueryParameter;
import org.minijpa.jdbc.db.EntityInstanceBuilder;
import org.minijpa.jdbc.model.SqlDelete;
import org.minijpa.jdbc.model.SqlInsert;
import org.minijpa.jdbc.model.SqlStatementGenerator;
import org.minijpa.jdbc.model.SqlUpdate;
import org.minijpa.jdbc.relationship.RelationshipJoinTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class EntityWriterImpl implements EntityWriter {

    private final Logger LOG = LoggerFactory.getLogger(EntityWriterImpl.class);
    private final EntityContainer entityContainer;
    private final SqlStatementFactory sqlStatementFactory;
    private final SqlStatementGenerator sqlStatementGenerator;
    private final EntityLoader entityLoader;
    private final EntityInstanceBuilder entityInstanceBuilder;
    private final ConnectionHolder connectionHolder;
    private final JpaJdbcRunner jdbcRunner;

    private final MetaEntityHelper metaEntityHelper = new MetaEntityHelper();

    public EntityWriterImpl(EntityContainer entityContainer, SqlStatementFactory sqlStatementFactory, SqlStatementGenerator sqlStatementGenerator, EntityLoader entityLoader, EntityInstanceBuilder entityInstanceBuilder, ConnectionHolder connectionHolder, JpaJdbcRunner jdbcRunner) {
	this.entityContainer = entityContainer;
	this.sqlStatementFactory = sqlStatementFactory;
	this.sqlStatementGenerator = sqlStatementGenerator;
	this.entityLoader = entityLoader;
	this.entityInstanceBuilder = entityInstanceBuilder;
	this.connectionHolder = connectionHolder;
	this.jdbcRunner = jdbcRunner;
    }

    @Override
    public void persist(MetaEntity entity, Object entityInstance,
	    ModelValueArray<MetaAttribute> attributeValueArray) throws Exception {
	EntityStatus entityStatus = MetaEntityHelper.getEntityStatus(entity, entityInstance);
	LOG.debug("persist: entityStatus=" + entityStatus);
	if (MetaEntityHelper.isFlushed(entity, entityInstance)) {
	    update(entity, entityInstance, attributeValueArray);
	} else {
	    insert(entity, entityInstance, attributeValueArray);
	}
    }

    private void checkOptimisticLock(MetaEntity entity, Object entityInstance, Object idValue)
	    throws Exception {
	if (!metaEntityHelper.hasOptimisticLock(entity, entityInstance))
	    return;

	Object currentVersionValue = entity.getVersionAttribute().get().getReadMethod().invoke(entityInstance);
	Object dbVersionValue = entityLoader.queryVersionValue(entity, idValue, LockType.NONE);
	LOG.debug("checkOptimisticLock: dbVersionValue=" + dbVersionValue + "; currentVersionValue=" + currentVersionValue);
	if (dbVersionValue == null || !dbVersionValue.equals(currentVersionValue))
	    throw new OptimisticLockException("Entity was written by another transaction, version" + dbVersionValue);
    }

    protected void update(MetaEntity entity, Object entityInstance,
	    ModelValueArray<MetaAttribute> attributeValueArray) throws Exception {
	// It's an update.
	if (attributeValueArray.isEmpty())
	    return;

	Object idValue = AttributeUtil.getIdValue(entity, entityInstance);
	checkOptimisticLock(entity, entityInstance, idValue);
	LOG.debug("update: idValue=" + idValue);
	List<QueryParameter> idParameters = metaEntityHelper.convertAVToQP(entity.getId(), idValue);
	List<String> idColumns = idParameters.stream().map(p -> p.getColumnName()).collect(Collectors.toList());
	if (metaEntityHelper.hasOptimisticLock(entity, entityInstance)) {
	    idColumns.add(entity.getVersionAttribute().get().getColumnName());
	}

	metaEntityHelper.createVersionAttributeArrayEntry(entity, entityInstance, attributeValueArray);
	SqlUpdate sqlUpdate = sqlStatementFactory.generateUpdate(entity, attributeValueArray.getModels(),
		idColumns);
	attributeValueArray.add(entity.getId(), idValue);
	if (metaEntityHelper.hasOptimisticLock(entity, entityInstance)) {
	    Object currentVersionValue = entity.getVersionAttribute().get().getReadMethod().invoke(entityInstance);
	    attributeValueArray.add(entity.getVersionAttribute().get(), currentVersionValue);
	}

	List<QueryParameter> parameters = metaEntityHelper.convertAVToQP(attributeValueArray);
	String sql = sqlStatementGenerator.export(sqlUpdate);
	jdbcRunner.update(connectionHolder.getConnection(), sql, parameters);
	metaEntityHelper.updateVersionAttributeValue(entity, entityInstance);
    }

    protected void insert(MetaEntity entity, Object entityInstance,
	    ModelValueArray<MetaAttribute> attributeValueArray) throws Exception {
	// It's an insert.
	// checks specific relationship attributes ('one to many' with join table) even
	// if there are no notified changes. If they get changed then they'll be made persistent.
	// Collect join table attributes
	Map<MetaAttribute, Object> joinTableAttrs = new HashMap<>();
	for (MetaAttribute a : entity.getAttributes()) {
//	    LOG.info("persist: a.getRelationship()=" + a.getRelationship());
//	    if (a.getRelationship() != null)
//		LOG.info("persist: a.getRelationship().getJoinTable()=" + a.getRelationship().getJoinTable());

	    if (a.getRelationship() != null && a.getRelationship().getJoinTable() != null
		    && a.getRelationship().isOwner()) {
		Object attributeInstance = entityInstanceBuilder.getAttributeValue(entityInstance, a);
//		LOG.info("persist: attributeInstance=" + attributeInstance);
//		LOG.info("persist: attributeInstance.getClass()=" + attributeInstance.getClass());
		if (CollectionUtils.isCollectionClass(attributeInstance.getClass())
			&& !CollectionUtils.isCollectionEmpty(attributeInstance))
		    joinTableAttrs.put(a, attributeInstance);
	    }
	}

	MetaAttribute id = entity.getId();
//	LOG.info("persist: id.getPkGeneration()=" + id.getPkGeneration());
	PkStrategy pkStrategy = id.getPkGeneration().getPkStrategy();
//	LOG.info("Primary Key Generation Strategy: " + pkStrategy);
	if (pkStrategy == PkStrategy.IDENTITY) {
	    List<QueryParameter> parameters = metaEntityHelper.convertAVToQP(attributeValueArray);
	    // version attribute
	    Optional<QueryParameter> optVersion = metaEntityHelper.generateVersionParameter(entity);
	    if (optVersion.isPresent())
		parameters.add(optVersion.get());

	    List<String> columns = parameters.stream().map(p -> {
		return p.getColumnName();
	    }).collect(Collectors.toList());

	    SqlInsert sqlInsert = sqlStatementFactory.generateInsert(entity, columns);
	    String sql = sqlStatementGenerator.export(sqlInsert);
	    Object pk = jdbcRunner.persist(connectionHolder.getConnection(), sql, parameters);
//	    LOG.info("persist: pk=" + pk);
	    entity.getId().getWriteMethod().invoke(entityInstance, pk);
	    if (optVersion.isPresent()) {
		entity.getVersionAttribute().get().getWriteMethod().invoke(entityInstance, optVersion.get().getValue());
	    }
	} else {
	    Object idValue = id.getReadMethod().invoke(entityInstance);
	    List<QueryParameter> idParameters = metaEntityHelper.convertAVToQP(id, idValue);
	    List<QueryParameter> parameters = metaEntityHelper.convertAVToQP(attributeValueArray);
	    parameters.addAll(0, idParameters);
	    // version attribute
	    Optional<QueryParameter> optVersion = metaEntityHelper.generateVersionParameter(entity);
	    if (optVersion.isPresent())
		parameters.add(optVersion.get());

	    List<String> columns = parameters.stream().map(p -> {
		return p.getColumnName();
	    }).collect(Collectors.toList());

	    SqlInsert sqlInsert = sqlStatementFactory.generateInsert(entity, columns);
	    String sql = sqlStatementGenerator.export(sqlInsert);
	    jdbcRunner.persist(connectionHolder.getConnection(), sql, parameters);
//	    LOG.info("persist: pk=" + pk);
	    if (optVersion.isPresent()) {
		entity.getVersionAttribute().get().getWriteMethod().invoke(entityInstance, optVersion.get().getValue());
	    }
	}

	// persist join table attributes
//	LOG.info("persist: joinTableAttrs.size()=" + joinTableAttrs.size());
	for (Map.Entry<MetaAttribute, Object> entry : joinTableAttrs.entrySet()) {
	    MetaAttribute a = entry.getKey();
	    List<Object> ees = CollectionUtils.getCollectionAsList(entry.getValue());
	    if (entityContainer.isManaged(ees))
		persistJoinTableAttributes(ees, a, entityInstance);
	}
    }

    private void persistJoinTableAttributes(List<Object> ees, MetaAttribute a, Object entityInstance) throws Exception {
	// persist every entity instance
	RelationshipJoinTable relationshipJoinTable = a.getRelationship().getJoinTable();
	for (Object instance : ees) {
	    List<QueryParameter> parameters = sqlStatementFactory.createRelationshipJoinTableParameters(relationshipJoinTable, entityInstance, instance);
	    List<String> columnNames = parameters.stream().map(p -> p.getColumnName()).collect(Collectors.toList());
	    SqlInsert sqlInsert = sqlStatementFactory.generateJoinTableInsert(relationshipJoinTable, columnNames);
	    String sql = sqlStatementGenerator.export(sqlInsert);
	    jdbcRunner.persist(connectionHolder.getConnection(), sql, parameters);
	}
    }

    @Override
    public void delete(Object entityInstance, MetaEntity e) throws Exception {
	Object idValue = AttributeUtil.getIdValue(e, entityInstance);
	LOG.debug("delete: idValue=" + idValue);

	List<QueryParameter> idParameters = metaEntityHelper.convertAVToQP(e.getId(), idValue);
	if (metaEntityHelper.hasOptimisticLock(e, entityInstance)) {
	    Object currentVersionValue = e.getVersionAttribute().get().getReadMethod().invoke(entityInstance);
	    idParameters.addAll(metaEntityHelper.convertAVToQP(e.getVersionAttribute().get(), currentVersionValue));
	}

	List<String> idColumns = idParameters.stream().map(p -> p.getColumnName()).collect(Collectors.toList());

	SqlDelete sqlDelete = sqlStatementFactory.generateDeleteById(e, idColumns);
	String sql = sqlStatementGenerator.export(sqlDelete);
	jdbcRunner.delete(sql, connectionHolder.getConnection(), idParameters);
    }

}
