package org.minijpa.jdbc;

public class ColumnNameValue {
	private String columnName;
	private Object value;
	private Class<?> type;
	private Class<?> readWriteDbType;
	private Integer sqlType;
	private MetaAttribute foreignKeyAttribute;
	private MetaAttribute attribute;

	public ColumnNameValue(String columnName, Object value, Class<?> type, Class<?> readWriteDbType, Integer sqlType,
			MetaAttribute foreignKeyAttribute, MetaAttribute attribute) {
		super();
		this.columnName = columnName;
		this.value = value;
		this.type = type;
		this.readWriteDbType = readWriteDbType;
		this.sqlType = sqlType;
		this.foreignKeyAttribute = foreignKeyAttribute;
		this.attribute = attribute;
	}

	public static ColumnNameValue build(AttributeValue av) {
		ColumnNameValue cnv = new ColumnNameValue(av.getAttribute().getColumnName(), av.getValue(),
				av.getAttribute().getType(), av.getAttribute().getReadWriteDbType(), av.getAttribute().getSqlType(),
				null, av.getAttribute());
		return cnv;
	}

	public static ColumnNameValue build(MetaAttribute av) {
		ColumnNameValue cnv = new ColumnNameValue(av.getColumnName(), null, av.getType(), av.getReadWriteDbType(),
				av.getSqlType(), null, av);
		return cnv;
	}

	public String getColumnName() {
		return columnName;
	}

	public Object getValue() {
		return value;
	}

	public Class<?> getType() {
		return type;
	}

	public Class<?> getReadWriteDbType() {
		return readWriteDbType;
	}

	public Integer getSqlType() {
		return sqlType;
	}

	public MetaAttribute getForeignKeyAttribute() {
		return foreignKeyAttribute;
	}

	public MetaAttribute getAttribute() {
		return attribute;
	}

}
