package org.tinyjpa.jdbc;

import java.lang.reflect.Method;

import org.tinyjpa.metadata.GeneratedValue;

public class Attribute {
	private String name;
	private String columnName;
	private Class<?> type;
	private Method readMethod;
	private Method writeMethod;
	private boolean id;
	private Integer sqlType;
	private GeneratedValue generatedValue;

	public Attribute(String name, String columnName, Class<?> type, Method readMethod, Method writeMethod, boolean id,
			Integer sqlType, GeneratedValue generatedValue) {
		super();
		this.name = name;
		this.columnName = columnName;
		this.type = type;
		this.readMethod = readMethod;
		this.writeMethod = writeMethod;
		this.id = id;
		this.sqlType = sqlType;
		this.generatedValue = generatedValue;
	}

	public String getName() {
		return name;
	}

	public String getColumnName() {
		return columnName;
	}

	public Method getReadMethod() {
		return readMethod;
	}

	public Method getWriteMethod() {
		return writeMethod;
	}

	public Class<?> getType() {
		return type;
	}

	public boolean isId() {
		return id;
	}

	public Integer getSqlType() {
		return sqlType;
	}

	public GeneratedValue getGeneratedValue() {
		return generatedValue;
	}

}
