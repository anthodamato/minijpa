package org.tinyjpa.jdbc;

import java.util.ArrayList;
import java.util.List;

import org.tinyjpa.jdbc.model.Column;
import org.tinyjpa.jdbc.model.FromTable;
import org.tinyjpa.jdbc.model.QueryParameter;
import org.tinyjpa.jdbc.model.TableColumn;
import org.tinyjpa.jdbc.model.Value;

public class MetaEntityHelper {
	private AttributeValueConverter attributeValueConverter = new EmbeddedIdAttributeValueConverter();

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
			ColumnNameValue columnNameValue = null;
			columnNameValue = new ColumnNameValue(a.getColumnName(), null, a.getType(), a.getReadWriteDbType(),
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
				QueryParameter queryParameter = new QueryParameter(idValue, attributeType.getId().getType(),
						attributeType.getId().getSqlType());
				list.add(queryParameter);
			}
		} else {
			QueryParameter queryParameter = new QueryParameter(av.getValue(), av.getAttribute().getType(),
					av.getAttribute().getSqlType());
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
			AttributeValue avn = new AttributeValue(joinColumnAttributes.get(index).getForeignKeyAttribute(),
					av.getValue());

			ColumnNameValue cnv = new ColumnNameValue(joinColumnAttributes.get(index).getColumnName(), avn.getValue(),
					avn.getAttribute().getType(), avn.getAttribute().getReadWriteDbType(),
					avn.getAttribute().getSqlType(), null, avn.getAttribute());
			columnNameValues.add(cnv);
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
		List<Value> values = new ArrayList<Value>();
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

//	public List<TableColumn> toTableColumns(List<AbstractAttribute> abstractAttributes, FromTable fromTable) {
//		List<TableColumn> tableColumns = new ArrayList<>();
//		for (AbstractAttribute attribute : abstractAttributes) {
//			TableColumn tableColumn = new TableColumn(fromTable, new Column(attribute.columnName));
//			tableColumns.add(tableColumn);
//		}
//
//		return tableColumns;
//	}

	public List<TableColumn> toTableColumns(List<ColumnNameValue> columnNameValues, FromTable fromTable) {
		List<TableColumn> tableColumns = new ArrayList<>();
		for (ColumnNameValue columnNameValue : columnNameValues) {
			TableColumn tableColumn = new TableColumn(fromTable, new Column(columnNameValue.getColumnName()));
			tableColumns.add(tableColumn);
		}

		return tableColumns;
	}
}
