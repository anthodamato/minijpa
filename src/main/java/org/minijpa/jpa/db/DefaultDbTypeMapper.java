package org.minijpa.jpa.db;

import org.minijpa.jdbc.DbTypeMapper;

public class DefaultDbTypeMapper implements DbTypeMapper {

	@Override
	public Class<?> map(Class<?> attributeType, Integer jdbcType) {
		return attributeType;
	}

	@Override
	public Object convert(Object value, Class<?> readWriteDbType, Class<?> attributeType) {
		return value;
	}

}
