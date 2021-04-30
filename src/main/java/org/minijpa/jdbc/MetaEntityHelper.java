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
package org.minijpa.jdbc;

import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.minijpa.jdbc.model.Column;
import org.minijpa.jdbc.model.FromTable;
import org.minijpa.jdbc.model.TableColumn;
import org.minijpa.jdbc.model.Value;
import org.minijpa.jpa.db.EntityStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetaEntityHelper {

    private static final Logger LOG = LoggerFactory.getLogger(MetaEntityHelper.class);

    public List<FetchParameter> convertAttributes(List<MetaAttribute> attributes) {
	return attributes.stream().map(a -> FetchParameter.build(a)).collect(Collectors.toList());
    }

    public FetchParameter toFetchParameter(MetaAttribute attribute) {
	return FetchParameter.build(attribute);
    }

    public List<FetchParameter> toFetchParameter(List<JoinColumnAttribute> attributes) {
	List<FetchParameter> list = new ArrayList<>();
	for (JoinColumnAttribute a : attributes) {
	    FetchParameter columnNameValue = new FetchParameter(a.getColumnName(), a.getType(), a.getReadWriteDbType(),
		    a.getSqlType(), a.getForeignKeyAttribute(), null, true);
	    list.add(columnNameValue);
	}

	return list;
    }

    public List<QueryParameter> convertAbstractAVToQP(ModelValueArray<AbstractAttribute> attributeValueArray) throws Exception {
	List<QueryParameter> list = new ArrayList<>();
	for (int i = 0; i < attributeValueArray.size(); ++i) {
	    AbstractAttribute a = attributeValueArray.getModel(i);
	    QueryParameter queryParameter = new QueryParameter(a.getColumnName(), attributeValueArray.getValue(i),
		    a.getType(), a.getSqlType(), a.jdbcAttributeMapper);
	    list.add(queryParameter);
	}

	return list;
    }

    public List<QueryParameter> convertAVToQP(MetaAttribute a, Object value) throws Exception {
	List<QueryParameter> list = new ArrayList<>();
	if (a.getRelationship() != null) {
	    String joinColumn = a.getRelationship().getJoinColumn();
	    LOG.debug("convertAVToQP: joinColumn=" + joinColumn);
	    MetaEntity attributeType = a.getRelationship().getAttributeType();
	    LOG.debug("convertAVToQP: attributeType=" + attributeType);
	    if (joinColumn != null && attributeType != null) {
		Object idValue = attributeType.getId().getReadMethod().invoke(value);
		QueryParameter queryParameter = new QueryParameter(
			joinColumn,
			idValue,
			attributeType.getId().getType(),
			attributeType.getId().getAttribute().getSqlType(),
			attributeType.getId().getAttribute().jdbcAttributeMapper);
		list.add(queryParameter);
	    }
	} else {
	    QueryParameter queryParameter = new QueryParameter(a.getColumnName(), value,
		    a.getType(), a.getSqlType(), a.jdbcAttributeMapper);
	    list.add(queryParameter);
	}

	return list;
    }

    public List<QueryParameter> convertAVToQP(Pk pk, Object value) throws Exception {
	List<QueryParameter> list = new ArrayList<>();
	if (pk.isEmbedded()) {
	    ModelValueArray<MetaAttribute> attributeValueArray = new ModelValueArray();
	    expand(pk, value, attributeValueArray);
	    list.addAll(convertAVToQP(attributeValueArray));
	    return list;
	}

	QueryParameter queryParameter = new QueryParameter(pk.getAttribute().getColumnName(), value,
		pk.getType(), pk.getAttribute().getSqlType(), pk.getAttribute().jdbcAttributeMapper);
	list.add(queryParameter);

	return list;
    }

    public List<QueryParameter> convertAVToQP(ModelValueArray<MetaAttribute> attributeValueArray) throws Exception {
	List<QueryParameter> list = new ArrayList<>();
	for (int i = 0; i < attributeValueArray.size(); ++i) {
	    list.addAll(convertAVToQP(attributeValueArray.getModel(i), attributeValueArray.getValue(i)));
	}

	return list;
    }

    public List<QueryParameter> createJoinColumnAVSToQP(List<JoinColumnAttribute> joinColumnAttributes,
	    Pk owningId, Object joinTableForeignKey) throws Exception {
	ModelValueArray<MetaAttribute> attributeValueArray = new ModelValueArray<>();
	expand(owningId, joinTableForeignKey, attributeValueArray);
	List<QueryParameter> columnNameValues = new ArrayList<>();
	for (int i = 0; i < attributeValueArray.size(); ++i) {
	    MetaAttribute attribute = attributeValueArray.getModel(i);
	    int index = AttributeUtil.indexOfJoinColumnAttribute(joinColumnAttributes, attribute);
	    MetaAttribute metaAttribute = joinColumnAttributes.get(index).getForeignKeyAttribute();
	    QueryParameter qp = new QueryParameter(joinColumnAttributes.get(index).getColumnName(), attributeValueArray.getValue(i),
		    metaAttribute.getType(), metaAttribute.getSqlType(), metaAttribute.jdbcAttributeMapper);
	    columnNameValues.add(qp);
	}

	return columnNameValues;
    }

    public List<FetchParameter> convertAllAttributes(MetaEntity entity) {
	LOG.debug("convertAllAttributes: entity=" + entity);
	List<MetaAttribute> expandedAttributes = entity.expandAllAttributes();
	LOG.debug("convertAllAttributes: expandedAttributes=" + expandedAttributes);
	List<FetchParameter> fetchColumnNameValues = convertAttributes(expandedAttributes);
	fetchColumnNameValues.addAll(toFetchParameter(entity.expandJoinColumnAttributes()));
	return fetchColumnNameValues;
    }

    public Value toValue(MetaAttribute attribute, FromTable fromTable) {
	return new TableColumn(fromTable, new Column(attribute.columnName));
    }

    public List<Value> toValues(MetaEntity entity, FromTable fromTable) {
	List<Value> values = new ArrayList<>();
	List<MetaAttribute> expandedAttributes = entity.expandAllAttributes();
	for (MetaAttribute attribute : expandedAttributes) {
	    TableColumn tableColumn = new TableColumn(fromTable, new Column(attribute.columnName));
	    values.add(tableColumn);
	}

	for (JoinColumnAttribute joinColumnAttribute : entity.expandJoinColumnAttributes()) {
	    TableColumn tableColumn = new TableColumn(fromTable, new Column(joinColumnAttribute.columnName));
	    values.add(tableColumn);
	}

	return values;
    }

    public List<TableColumn> toTableColumns(List<MetaAttribute> attributes, FromTable fromTable) {
	List<TableColumn> tableColumns = new ArrayList<>();
	for (MetaAttribute metaAttribute : attributes) {
	    TableColumn tableColumn = new TableColumn(fromTable, new Column(metaAttribute.getColumnName()));
	    tableColumns.add(tableColumn);
	}

	return tableColumns;
    }

    public List<TableColumn> queryParametersToTableColumns(List<QueryParameter> parameters, FromTable fromTable) {
	return parameters.stream().map(p -> new TableColumn(fromTable, new Column(p.getColumnName())))
		.collect(Collectors.toList());
    }

    public List<TableColumn> attributesToTableColumns(List<AbstractAttribute> attributes, FromTable fromTable) {
	return attributes.stream().map(a -> new TableColumn(fromTable, new Column(a.getColumnName())))
		.collect(Collectors.toList());
    }

    public boolean hasOptimisticLock(MetaEntity entity, Object entityInstance)
	    throws IllegalAccessException, InvocationTargetException {
	LockType lockType = (LockType) entity.getLockTypeAttributeReadMethod().get().invoke(entityInstance);
	if (lockType == LockType.OPTIMISTIC || lockType == LockType.OPTIMISTIC_FORCE_INCREMENT)
	    return true;

	return entity.getVersionAttribute().isPresent();
    }

    public void updateVersionAttributeValue(MetaEntity entity, Object entityInstance) throws Exception {
	if (!hasOptimisticLock(entity, entityInstance))
	    return;

	Object currentVersionValue = entity.getVersionAttribute().get().getReadMethod().invoke(entityInstance);
	Object versionValue = AttributeUtil.increaseVersionValue(entity, currentVersionValue);
	entity.getVersionAttribute().get().getWriteMethod().invoke(entityInstance, versionValue);
    }

    public void createVersionAttributeArrayEntry(MetaEntity entity, Object entityInstance,
	    ModelValueArray<MetaAttribute> attributeValueArray) throws Exception {
	if (!hasOptimisticLock(entity, entityInstance))
	    return;

	Object currentVersionValue = entity.getVersionAttribute().get().getReadMethod().invoke(entityInstance);
	Object versionValue = AttributeUtil.increaseVersionValue(entity, currentVersionValue);
	attributeValueArray.add(entity.getVersionAttribute().get(), versionValue);
    }

    public void expand(MetaAttribute attribute, Object value,
	    ModelValueArray<MetaAttribute> attributeValueArray) throws Exception {
	if (attribute.getRelationship() != null)
	    return;

	attributeValueArray.add(attribute, value);
    }

    public void expand(Pk attribute, Object value,
	    ModelValueArray<MetaAttribute> attributeValueArray) throws Exception {
	if (!attribute.isEmbedded()) {
	    attributeValueArray.add(attribute.getAttribute(), value);
	    return;
	}

	List<MetaAttribute> attributes = attribute.getAttributes();
	for (MetaAttribute a : attributes) {
	    Object v = a.getReadMethod().invoke(value);
	    expand(a, v, attributeValueArray);
	}
    }

    public LockType getLockType(MetaEntity entity, Object entityInstance) throws Exception {
	return (LockType) entity.getLockTypeAttributeReadMethod().get().invoke(entityInstance);
    }

    public void setLockType(MetaEntity entity, Object entityInstance, LockType lockType) throws Exception {
	entity.getLockTypeAttributeWriteMethod().get().invoke(entityInstance, lockType);
    }

    public Optional<QueryParameter> generateVersionParameter(MetaEntity metaEntity) throws Exception {
	if (!metaEntity.hasVersionAttribute())
	    return Optional.empty();

	Object value = null;
	MetaAttribute attribute = metaEntity.getVersionAttribute().get();
	Class<?> type = attribute.getType();
	if (type == Integer.class || (type.isPrimitive() && type.getName().equals("int"))) {
	    value = 0;
	} else if (type == Short.class || (type.isPrimitive() && type.getName().equals("short"))) {
	    value = Short.valueOf("0");
	} else if (type == Long.class || (type.isPrimitive() && type.getName().equals("long"))) {
	    value = 0L;
	} else if (type == Timestamp.class) {
	    value = Timestamp.from(Instant.now());
	}

	List<QueryParameter> parameters = convertAVToQP(metaEntity.getVersionAttribute().get(), value);
	return Optional.of(parameters.get(0));
    }

    public static EntityStatus getEntityStatus(MetaEntity entity, Object entityInstance) throws Exception {
	return (EntityStatus) entity.getEntityStatusAttributeReadMethod().get().invoke(entityInstance);
    }

    public static void setEntityStatus(MetaEntity entity, Object entityInstance, EntityStatus entityStatus) throws Exception {
	entity.getEntityStatusAttributeWriteMethod().get().invoke(entityInstance, entityStatus);
    }

    public static void setForeignKeyValue(MetaAttribute attribute, Object entityInstance, Object value)
	    throws IllegalAccessException, InvocationTargetException {
	LOG.debug("setForeignKeyValue: attribute=" + attribute);
	LOG.debug("setForeignKeyValue: value=" + value);
	LOG.debug("setForeignKeyValue: attribute.getJoinColumnWriteMethod().get()=" + attribute.getJoinColumnWriteMethod().get());
	attribute.getJoinColumnWriteMethod().get().invoke(entityInstance, value);
    }

    public static Object getForeignKeyValue(MetaAttribute attribute, Object entityInstance)
	    throws IllegalAccessException, InvocationTargetException {
	return attribute.getJoinColumnReadMethod().get().invoke(entityInstance);
    }

    public static boolean isFlushed(MetaEntity entity, Object entityInstance) throws Exception {
	EntityStatus entityStatus = getEntityStatus(entity, entityInstance);
	return entityStatus == EntityStatus.FLUSHED || entityStatus == EntityStatus.FLUSHED_LOADED_FROM_DB;
    }

    public static boolean isDetached(MetaEntity entity, Object entityInstance) throws Exception {
	EntityStatus entityStatus = getEntityStatus(entity, entityInstance);
	return entityStatus == EntityStatus.DETACHED;
    }
}
