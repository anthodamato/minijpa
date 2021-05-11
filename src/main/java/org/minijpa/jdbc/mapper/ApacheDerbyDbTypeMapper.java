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

import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Calendar;
import java.util.Date;

public class ApacheDerbyDbTypeMapper extends DefaultDbTypeMapper {

    @Override
    public Class<?> map(Class<?> attributeType, Integer jdbcType) {
	if (attributeType == LocalDate.class)
	    return Date.class;

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
	if (attributeType == LocalDate.class && readWriteDbType == Date.class && value != null) {
	    Date date = (Date) value;
	    return new java.sql.Date(date.getTime()).toLocalDate();
	}

	if (attributeType == OffsetDateTime.class && readWriteDbType == Timestamp.class && value != null) {
	    Timestamp date = (Timestamp) value;
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(date);
	    return OffsetDateTime.ofInstant(date.toInstant(), calendar.getTimeZone().toZoneId());
	}

	return super.convert(value, readWriteDbType, attributeType);
    }

}
