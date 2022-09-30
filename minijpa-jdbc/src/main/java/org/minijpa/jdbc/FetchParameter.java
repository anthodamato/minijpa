package org.minijpa.jdbc;

import java.util.Optional;

import org.minijpa.jdbc.mapper.AttributeMapper;

public interface FetchParameter {
	public String getColumnName();

	public Integer getSqlType();

	@SuppressWarnings("rawtypes")
	public Optional<AttributeMapper> getAttributeMapper();

}
