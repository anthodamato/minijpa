package org.tinyjpa.jdbc;

import java.util.ArrayList;
import java.util.List;

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

	public List<ColumnNameValue> convertAttributeValues(List<AttributeValue> attributeValues) throws Exception {
		List<ColumnNameValue> list = new ArrayList<>();
		for (AttributeValue av : attributeValues) {
			MetaAttribute a = av.getAttribute();
			if (a.isEmbedded() && a.isId()) {
				List<AttributeValue> idav = attributeValueConverter.convert(av);
				list.addAll(convertAttributeValues(idav));
				continue;
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
}
