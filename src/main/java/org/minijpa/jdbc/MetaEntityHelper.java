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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetaEntityHelper {

    private final Logger LOG = LoggerFactory.getLogger(MetaEntityHelper.class);

    private final AttributeValueConverter attributeValueConverter = new EmbeddedIdAttributeValueConverter();

    public List<ColumnNameValue> convertAttributes(List<MetaAttribute> attributes) {
	List<ColumnNameValue> list = new ArrayList<>();
	for (MetaAttribute a : attributes) {
	    ColumnNameValue columnNameValue = ColumnNameValue.build(a);
	    list.add(columnNameValue);
	}

	return list;
    }

    public List<ColumnNameValue> convertJoinColumnAttributes(List<JoinColumnAttribute> attributes) {
	List<ColumnNameValue> list = new ArrayList<>();
	for (JoinColumnAttribute a : attributes) {
	    ColumnNameValue columnNameValue = new ColumnNameValue(a.getColumnName(), null, a.getType(), a.getReadWriteDbType(),
		    a.getSqlType(), a.getForeignKeyAttribute(), null);
	    list.add(columnNameValue);
	}

	return list;
    }

    public List<QueryParameter> convertAbstractAVToQP(AttributeValueArray<AbstractAttribute> attributeValueArray) throws Exception {
	List<QueryParameter> list = new ArrayList<>();
	for (int i = 0; i < attributeValueArray.size(); ++i) {
	    AbstractAttribute a = attributeValueArray.getAttribute(i);
	    QueryParameter queryParameter = new QueryParameter(a.getColumnName(), attributeValueArray.getValue(i),
		    a.getType(), a.getSqlType(), a.jdbcAttributeMapper);
	    list.add(queryParameter);
	}

	return list;
    }

    public List<QueryParameter> convertAVToQP(MetaAttribute a, Object value) throws Exception {
	List<QueryParameter> list = new ArrayList<>();
	if (a.isEmbedded() && a.isId()) {
	    AttributeValueArray<MetaAttribute> attributeValueArray = new AttributeValueArray();
	    attributeValueConverter.convert(a, value, attributeValueArray);
	    list.addAll(convertAVToQP(attributeValueArray));
	    return list;
	}

	if (a.getRelationship() != null) {
	    String joinColumn = a.getRelationship().getJoinColumn();
	    LOG.debug("convertAVToQP: joinColumn=" + joinColumn);
	    MetaEntity attributeType = a.getRelationship().getAttributeType();
	    LOG.debug("convertAVToQP: attributeType=" + attributeType);
	    if (joinColumn != null && attributeType != null) {
		Object idValue = attributeType.getId().getReadMethod().invoke(value);
		QueryParameter queryParameter = new QueryParameter(joinColumn, idValue, attributeType.getId().getType(),
			attributeType.getId().getSqlType(), attributeType.getId().jdbcAttributeMapper);
		list.add(queryParameter);
	    }
	} else {
	    QueryParameter queryParameter = new QueryParameter(a.getColumnName(), value,
		    a.getType(), a.getSqlType(), a.jdbcAttributeMapper);
	    list.add(queryParameter);
	}

	return list;
    }

    public List<QueryParameter> convertAVToQP(AttributeValueArray<MetaAttribute> attributeValueArray) throws Exception {
	List<QueryParameter> list = new ArrayList<>();
	for (int i = 0; i < attributeValueArray.size(); ++i) {
	    list.addAll(convertAVToQP(attributeValueArray.getAttribute(i), attributeValueArray.getValue(i)));
	}

	return list;
    }

    public List<QueryParameter> createJoinColumnAVSToQP(List<JoinColumnAttribute> joinColumnAttributes,
	    MetaAttribute owningId, Object joinTableForeignKey) throws Exception {
	AttributeValueArray<MetaAttribute> attributeValueArray = new AttributeValueArray<>();
	attributeValueConverter.convert(owningId, joinTableForeignKey, attributeValueArray);
	int index = -1;
	List<QueryParameter> columnNameValues = new ArrayList<>();
	for (int i = 0; i < attributeValueArray.size(); ++i) {
	    MetaAttribute attribute = attributeValueArray.getAttribute(i);
	    index = AttributeUtil.indexOfJoinColumnAttribute(joinColumnAttributes, attribute);
	    MetaAttribute metaAttribute = joinColumnAttributes.get(index).getForeignKeyAttribute();
	    QueryParameter qp = new QueryParameter(joinColumnAttributes.get(index).getColumnName(), attributeValueArray.getValue(i),
		    metaAttribute.getType(), metaAttribute.getSqlType(), metaAttribute.jdbcAttributeMapper);
	    columnNameValues.add(qp);
	}

	return columnNameValues;
    }

    public List<ColumnNameValue> convertAllAttributes(MetaEntity entity) {
	List<MetaAttribute> expandedAttributes = entity.expandAllAttributes();
	List<ColumnNameValue> fetchColumnNameValues = convertAttributes(expandedAttributes);
	fetchColumnNameValues.addAll(convertJoinColumnAttributes(entity.getJoinColumnAttributes()));
	return fetchColumnNameValues;
    }

    public List<Value> toValues(MetaEntity entity, FromTable fromTable) {
	List<Value> values = new ArrayList<>();
	List<MetaAttribute> expandedAttributes = entity.expandAllAttributes();
	for (MetaAttribute attribute : expandedAttributes) {
	    TableColumn tableColumn = new TableColumn(fromTable, new Column(attribute.columnName));
	    values.add(tableColumn);
	}

	for (JoinColumnAttribute joinColumnAttribute : entity.getJoinColumnAttributes()) {
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

    public void updateVersionAttributeValue(MetaEntity entity, Object entityInstance)
	    throws Exception {
	if (!hasOptimisticLock(entity, entityInstance))
	    return;

	Object currentVersionValue = entity.getVersionAttribute().get().getReadMethod().invoke(entityInstance);
	Object versionValue = AttributeUtil.increaseVersionValue(entity, currentVersionValue);
	entity.getVersionAttribute().get().getWriteMethod().invoke(entityInstance, versionValue);
    }

    public void createVersionAttributeArrayEntry(MetaEntity entity, Object entityInstance,
	    AttributeValueArray<MetaAttribute> attributeValueArray) throws Exception {
	if (!hasOptimisticLock(entity, entityInstance))
	    return;

	Object currentVersionValue = entity.getVersionAttribute().get().getReadMethod().invoke(entityInstance);
	Object versionValue = AttributeUtil.increaseVersionValue(entity, currentVersionValue);
	attributeValueArray.add(entity.getVersionAttribute().get(), versionValue);
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

}
