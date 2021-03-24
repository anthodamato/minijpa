/*
 * Copyright (C) 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.minijpa.jdbc.mapper;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Types;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.minijpa.jdbc.DbTypeMapper;

public class DefaultDbTypeMapper implements DbTypeMapper {

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
	return attributeType;
    }

    @Override
    public Object convert(Object value, Class<?> readWriteDbType, Class<?> attributeType) {
	if (readWriteDbType == String.class && attributeType.isEnum())
	    return Enum.valueOf((Class<Enum>) attributeType, (String) value);

	if (readWriteDbType == Integer.class && attributeType.isEnum()) {
	    Object[] enums = attributeType.getEnumConstants();
	    for (Object o : enums) {
		if (((Enum) o).ordinal() == (Integer) value)
		    return o;
	    }

	    return null;
	}

	return value;
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
