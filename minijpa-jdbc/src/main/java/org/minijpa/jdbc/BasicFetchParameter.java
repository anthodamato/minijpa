package org.minijpa.jdbc;

import java.util.Optional;

import org.minijpa.jdbc.mapper.AttributeMapper;

public class BasicFetchParameter implements FetchParameter {
	private final String columnName;
	private final Integer sqlType;
	private final Optional<AttributeMapper> attributeMapper;

	public BasicFetchParameter(String columnName, Integer sqlType, Optional<AttributeMapper> attributeMapper) {
		super();
		this.columnName = columnName;
		this.sqlType = sqlType;
		this.attributeMapper = attributeMapper;
	}

	@Override
	public String getColumnName() {
		return columnName;
	}

	@Override
	public Integer getSqlType() {
		return sqlType;
	}

	@Override
	public Optional<AttributeMapper> getAttributeMapper() {
		return attributeMapper;
	}

}
