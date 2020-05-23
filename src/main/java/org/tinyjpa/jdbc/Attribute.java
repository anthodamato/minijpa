package org.tinyjpa.jdbc;

import java.lang.reflect.Method;

public class Attribute {
	private String name;
	private String columnName;
	private Class<?> type;
	private Method readMethod;
	private Method writeMethod;
	private boolean id;
	private Integer sqlType;

	public Attribute(String name, String columnName, Class<?> type, Method readMethod, Method writeMethod, boolean id,
			Integer sqlType) {
		super();
		this.name = name;
		this.columnName = columnName;
		this.type = type;
		this.readMethod = readMethod;
		this.writeMethod = writeMethod;
		this.id = id;
		this.sqlType = sqlType;
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

}
