package org.tinyjpa.jpa.db;

import java.time.LocalDate;
import java.util.Date;

import org.tinyjpa.jdbc.DbTypeMapper;

public class ApacheDerbyDbTypeMapper implements DbTypeMapper {
	@Override
	public Class<?> map(Class<?> attributeType, Integer jdbcType) {
		if (attributeType == LocalDate.class)
			return Date.class;

		return attributeType;
	}

	@Override
	public Object convert(Object value, Class<?> readWriteDbType, Class<?> attributeType) {
		if (attributeType == LocalDate.class && readWriteDbType == Date.class && value != null) {
			Date date = (Date) value;
			return new java.sql.Date(date.getTime()).toLocalDate();
		}

		return value;
	}

}
