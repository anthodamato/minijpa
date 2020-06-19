package org.tinyjpa.jdbc;

public class ColumnNameValue {
	private String columnName;
	private Object value;
	private Class<?> type;
	private Integer sqlType;
	private Attribute foreignKeyAttribute;
	private Attribute attribute;

	public ColumnNameValue(String columnName, Object value, Class<?> type, Integer sqlType,
			Attribute foreignKeyAttribute, Attribute attribute) {
		super();
		this.columnName = columnName;
		this.value = value;
		this.type = type;
		this.sqlType = sqlType;
		this.foreignKeyAttribute = foreignKeyAttribute;
		this.attribute = attribute;
	}

	public static ColumnNameValue build(AttributeValue av) {
		ColumnNameValue cnv = new ColumnNameValue(av.getAttribute().getColumnName(), av.getValue(),
				av.getAttribute().getType(), av.getAttribute().getSqlType(), null, av.getAttribute());
		return cnv;
	}

	public static ColumnNameValue build(Attribute av) {
		ColumnNameValue cnv = new ColumnNameValue(av.getColumnName(), null, av.getType(), av.getSqlType(), null, av);
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

	public Integer getSqlType() {
		return sqlType;
	}

	public Attribute getForeignKeyAttribute() {
		return foreignKeyAttribute;
	}

	public Attribute getAttribute() {
		return attribute;
	}

}
