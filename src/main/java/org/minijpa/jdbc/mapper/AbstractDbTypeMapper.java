/*
 * Copyright 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.minijpa.jdbc.mapper;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Calendar;
import org.minijpa.jdbc.DbTypeMapper;

public abstract class AbstractDbTypeMapper implements DbTypeMapper {

    protected JdbcAttributeMapper jdbcBigDecimalMapper = new JdbcBigDecimalMapper();
    protected JdbcAttributeMapper jdbcBooleanMapper = new JdbcBooleanMapper();
    protected JdbcAttributeMapper jdbcDateMapper = new JdbcDateMapper();
    protected JdbcAttributeMapper jdbcDoubleMapper = new JdbcDoubleMapper();
    protected JdbcAttributeMapper jdbcFloatMapper = new JdbcFloatMapper();
    protected JdbcAttributeMapper jdbcIntegerMapper = new JdbcIntegerMapper();
    protected JdbcAttributeMapper jdbcLocalDateMapper = new JdbcLocalDateMapper();
    protected JdbcAttributeMapper jdbcLongMapper = new JdbcLongMapper();
    protected JdbcAttributeMapper jdbcStringMapper = new JdbcStringMapper();
    protected JdbcAttributeMapper jdbcOffsetDateTimeMapper = new JdbcOffsetDateTimeMapper();
    protected JdbcAttributeMapper jdbcStringEnumMapper = new JdbcStringEnumMapper();
    protected JdbcAttributeMapper jdbcOrdinalEnumMapper = new JdbcOrdinalEnumMapper();
    protected JdbcAttributeMapper jdbcCharacterArrayMapper = new JdbcCharacterArrayMapper();
    protected JdbcAttributeMapper jdbcCharacterMapper = new JdbcCharacterMapper();

    @Override
    public Class<?> map(Class<?> attributeType, Integer jdbcType) {
	if (attributeType == LocalDate.class)
	    return java.util.Date.class;

	if (attributeType == OffsetDateTime.class)
	    return Timestamp.class;

	if (attributeType.isEnum() && jdbcType == Types.VARCHAR)
	    return String.class;

	if (attributeType.isEnum() && jdbcType == Types.INTEGER)
	    return Integer.class;

	if (attributeType.isPrimitive()) {
	    String typeName = attributeType.getName();
	    if (typeName.equals("int") || typeName.equals("byte") || typeName.equals("short"))
		return Integer.class;

	    if (typeName.equals("long"))
		return Long.class;

	    if (typeName.equals("float"))
		return Float.class;

	    if (typeName.equals("double"))
		return Double.class;

	    if (typeName.equals("boolean"))
		return Boolean.class;
	}

	return attributeType;
    }

    @Override
    public Object convert(Object value, Class<?> readWriteDbType, Class<?> attributeType) {
	if (value == null)
	    return null;

	Class<?> type = value.getClass();
	if (value instanceof Number) {
	    if (attributeType == Long.class || (attributeType.isPrimitive() && attributeType.getName().equals("long")))
		return ((Number) value).longValue();

	    if (attributeType == Integer.class || (attributeType.isPrimitive() && attributeType.getName().equals("int")))
		return ((Number) value).intValue();

	    if (attributeType == Float.class || (attributeType.isPrimitive() && attributeType.getName().equals("float")))
		return ((Number) value).floatValue();

	    if (attributeType == Double.class || (attributeType.isPrimitive() && attributeType.getName().equals("double")))
		return ((Number) value).doubleValue();
	}

//	if ((attributeType == Long.class || (attributeType.isPrimitive() && attributeType.getName().equals("long")))
//		&& readWriteDbType == BigDecimal.class) {
//	    return ((BigDecimal) value).longValue();
//	}
	if (type == String.class && attributeType.isEnum())
	    return Enum.valueOf((Class<Enum>) attributeType, (String) value);

	if (type == Integer.class && attributeType.isEnum()) {
	    Object[] enums = attributeType.getEnumConstants();
	    for (Object o : enums) {
		if (((Enum) o).ordinal() == (Integer) value)
		    return o;
	    }

	    return null;
	}

	if (attributeType == LocalDate.class) {
	    if (type == java.util.Date.class) {
		java.util.Date date = (java.util.Date) value;
		return new java.sql.Date(date.getTime()).toLocalDate();
	    }

	    if (type == java.sql.Date.class) {
		java.sql.Date date = (java.sql.Date) value;
		return date.toLocalDate();
	    }
	}

	if (type == Timestamp.class) {
	    if (attributeType == OffsetDateTime.class) {
		Timestamp date = (Timestamp) value;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return OffsetDateTime.ofInstant(date.toInstant(), calendar.getTimeZone().toZoneId());
	    }

	    if (attributeType == java.sql.Date.class) {
		Timestamp timestamp = (Timestamp) value;
		return new java.sql.Date(timestamp.getTime());
	    }

	    if (attributeType == java.util.Date.class) {
		Timestamp timestamp = (Timestamp) value;
		return new java.util.Date(timestamp.getTime());
	    }
	}

	if (attributeType == OffsetDateTime.class && type == Timestamp.class) {
	    Timestamp date = (Timestamp) value;
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(date);
	    return OffsetDateTime.ofInstant(date.toInstant(), calendar.getTimeZone().toZoneId());
	}

	return value;
    }

    @Override
    public Object convertGeneratedKey(Object value, Class<?> attributeType) {
	return ((Number) value).longValue();
    }

    @Override
    public JdbcAttributeMapper mapJdbcAttribute(Class<?> attributeType, Integer jdbcType) {
	if (attributeType == BigDecimal.class)
	    return jdbcBigDecimalMapper;

	if (attributeType == Boolean.class)
	    return jdbcBooleanMapper;

	if (attributeType == Double.class)
	    return jdbcDoubleMapper;

	if (attributeType == Float.class)
	    return jdbcFloatMapper;

	if (attributeType == Integer.class)
	    return jdbcIntegerMapper;

	if (attributeType == Long.class)
	    return jdbcLongMapper;

	if (attributeType == String.class)
	    return jdbcStringMapper;

	if (attributeType == Date.class)
	    return jdbcDateMapper;

	if (attributeType == LocalDate.class)
	    return jdbcLocalDateMapper;

	if (attributeType == OffsetDateTime.class)
	    return jdbcOffsetDateTimeMapper;

	if (attributeType.isEnum() && jdbcType == Types.VARCHAR)
	    return jdbcStringEnumMapper;

	if (attributeType.isEnum() && jdbcType == Types.INTEGER)
	    return jdbcOrdinalEnumMapper;

	if (attributeType.isPrimitive()) {
	    if (attributeType.getName().equals("byte"))
		return jdbcIntegerMapper;

	    if (attributeType.getName().equals("short"))
		return jdbcIntegerMapper;

	    if (attributeType.getName().equals("int"))
		return jdbcIntegerMapper;

	    if (attributeType.getName().equals("long"))
		return jdbcIntegerMapper;

	    if (attributeType.getName().equals("float"))
		return jdbcFloatMapper;

	    if (attributeType.getName().equals("double"))
		return jdbcDoubleMapper;

	    if (attributeType.getName().equals("boolean"))
		return jdbcBooleanMapper;

	    if (attributeType.getName().equals("char"))
		return jdbcCharacterMapper;
	}

	if (attributeType.isArray() && attributeType.getComponentType() == Character.class)
	    return jdbcCharacterArrayMapper;

	return null;
    }

}
