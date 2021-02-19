package org.minijpa.jdbc;

import java.util.ArrayList;
import java.util.List;

import org.minijpa.jdbc.model.Column;
import org.minijpa.jdbc.model.FromTable;
import org.minijpa.jdbc.model.TableColumn;
import org.minijpa.jdbc.model.Value;

public class MetaEntityHelper {

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

    public List<ColumnNameValue> convertAttributeValue(AttributeValue av) throws Exception {
	List<ColumnNameValue> list = new ArrayList<>();
	MetaAttribute a = av.getAttribute();
	if (a.isEmbedded() && a.isId()) {
	    List<AttributeValue> idav = attributeValueConverter.convert(av);
	    list.addAll(convertAttributeValues(idav));
	    return list;
	}

	if (a.getRelationship() != null) {
	    String joinColumn = a.getRelationship().getJoinColumn();
	    MetaEntity attributeType = a.getRelationship().getAttributeType();
	    if (joinColumn != null && attributeType != null) {
		Object idValue = attributeType.getId().getReadMethod().invoke(av.getValue());
		ColumnNameValue columnNameValue = new ColumnNameValue(joinColumn, idValue,
			attributeType.getId().getType(), attributeType.getId().getReadWriteDbType(),
			attributeType.getId().getSqlType(), a, null);
		list.add(columnNameValue);
	    }
	} else {
	    ColumnNameValue columnNameValue = ColumnNameValue.build(av);
	    list.add(columnNameValue);
	}

	return list;
    }

    public List<ColumnNameValue> convertAttributeValues(List<AttributeValue> attributeValues) throws Exception {
	List<ColumnNameValue> list = new ArrayList<>();
	for (AttributeValue av : attributeValues) {
	    list.addAll(convertAttributeValue(av));
	}

	return list;
    }

    public List<QueryParameter> convertAVToQP(AbstractAttributeValue av) throws Exception {
	List<QueryParameter> list = new ArrayList<>();
	AbstractAttribute a = av.getAttribute();
	QueryParameter queryParameter = new QueryParameter(a.getColumnName(), av.getValue(),
		a.getType(), a.getSqlType(), a.jdbcAttributeMapper);
	list.add(queryParameter);
	return list;
    }

    public List<QueryParameter> convertAbstractAVToQP(List<AbstractAttributeValue> attributeValues) throws Exception {
	List<QueryParameter> list = new ArrayList<>();
	for (AbstractAttributeValue av : attributeValues) {
	    list.addAll(convertAVToQP(av));
	}

	return list;
    }

    public List<QueryParameter> convertAVToQP(AttributeValue av) throws Exception {
	List<QueryParameter> list = new ArrayList<>();
	MetaAttribute a = av.getAttribute();
	if (a.isEmbedded() && a.isId()) {
	    List<AttributeValue> idav = attributeValueConverter.convert(av);
	    list.addAll(convertAVToQP(idav));
	    return list;
	}

	if (a.getRelationship() != null) {
	    String joinColumn = a.getRelationship().getJoinColumn();
	    MetaEntity attributeType = a.getRelationship().getAttributeType();
	    if (joinColumn != null && attributeType != null) {
		Object idValue = attributeType.getId().getReadMethod().invoke(av.getValue());
		QueryParameter queryParameter = new QueryParameter(joinColumn, idValue, attributeType.getId().getType(),
			attributeType.getId().getSqlType(), attributeType.getId().jdbcAttributeMapper);
		list.add(queryParameter);
	    }
	} else {
	    QueryParameter queryParameter = new QueryParameter(a.getColumnName(), av.getValue(),
		    a.getType(), a.getSqlType(), a.jdbcAttributeMapper);
	    list.add(queryParameter);
	}

	return list;
    }

    public List<QueryParameter> convertAVToQP(List<AttributeValue> attributeValues) throws Exception {
	List<QueryParameter> list = new ArrayList<>();
	for (AttributeValue av : attributeValues) {
	    list.addAll(convertAVToQP(av));
	}

	return list;
    }

    public List<ColumnNameValue> createJoinColumnAVS(List<JoinColumnAttribute> joinColumnAttributes,
	    MetaAttribute owningId, Object joinTableForeignKey) throws Exception {
	List<AttributeValue> idAttributeValues = attributeValueConverter
		.convert(new AttributeValue(owningId, joinTableForeignKey));

	int index = -1;
	List<ColumnNameValue> columnNameValues = new ArrayList<>();
	for (AttributeValue av : idAttributeValues) {
	    index = AttributeUtil.indexOfJoinColumnAttribute(joinColumnAttributes, av.getAttribute());
	    MetaAttribute metaAttribute = joinColumnAttributes.get(index).getForeignKeyAttribute();

	    ColumnNameValue cnv = new ColumnNameValue(joinColumnAttributes.get(index).getColumnName(), av.getValue(),
		    metaAttribute.getType(), metaAttribute.getReadWriteDbType(), metaAttribute.getSqlType(), null,
		    metaAttribute);
	    columnNameValues.add(cnv);
	}

	return columnNameValues;
    }

    public List<QueryParameter> createJoinColumnAVSToQP(List<JoinColumnAttribute> joinColumnAttributes,
	    MetaAttribute owningId, Object joinTableForeignKey) throws Exception {
	List<AttributeValue> idAttributeValues = attributeValueConverter
		.convert(new AttributeValue(owningId, joinTableForeignKey));

	int index = -1;
	List<QueryParameter> columnNameValues = new ArrayList<>();
	for (AttributeValue av : idAttributeValues) {
	    index = AttributeUtil.indexOfJoinColumnAttribute(joinColumnAttributes, av.getAttribute());
	    MetaAttribute metaAttribute = joinColumnAttributes.get(index).getForeignKeyAttribute();
	    QueryParameter qp = new QueryParameter(joinColumnAttributes.get(index).getColumnName(), av.getValue(),
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
	List<TableColumn> tableColumns = new ArrayList<>();
	for (QueryParameter parameter : parameters) {
	    TableColumn tableColumn = new TableColumn(fromTable, new Column(parameter.getColumnName()));
	    tableColumns.add(tableColumn);
	}

	return tableColumns;
    }

}
