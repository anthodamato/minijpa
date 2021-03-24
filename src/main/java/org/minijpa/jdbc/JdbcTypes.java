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
package org.minijpa.jdbc;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Types;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTypes {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(JdbcTypes.class);

    public static Integer sqlTypeFromClass(Class<?> c) {
	if (c == BigInteger.class)
	    return Types.BIGINT;

	if (c == Boolean.class)
	    return Types.BOOLEAN;

	if (c == Character.class)
	    return Types.CHAR;

	if (c == Date.class)
	    return Types.DATE;

	if (c == LocalDate.class)
	    return Types.DATE;

	if (c == BigDecimal.class)
	    return Types.DECIMAL;

	if (c == Double.class)
	    return Types.DOUBLE;

	if (c == Float.class)
	    return Types.FLOAT;

	if (c == Integer.class)
	    return Types.INTEGER;

	if (c == Long.class)
	    return Types.INTEGER;

	if (c == String.class)
	    return Types.VARCHAR;

	if (c == OffsetDateTime.class)
	    return Types.TIMESTAMP_WITH_TIMEZONE;

	if (c.isPrimitive()) {
	    if (c.getName().equals("byte"))
		return Types.INTEGER;

	    if (c.getName().equals("short"))
		return Types.INTEGER;

	    if (c.getName().equals("int"))
		return Types.INTEGER;

	    if (c.getName().equals("long"))
		return Types.INTEGER;

	    if (c.getName().equals("float"))
		return Types.FLOAT;

	    if (c.getName().equals("double"))
		return Types.DOUBLE;

	    if (c.getName().equals("boolean"))
		return Types.BOOLEAN;

	    if (c.getName().equals("char"))
		return Types.CHAR;
	}

	return Types.NULL;
    }

    public static Class classFromSqlType(int type) {
	switch (type) {
	    case Types.BIGINT:
		return BigInteger.class;
	    case Types.BOOLEAN:
		return Boolean.class;
	    case Types.CHAR:
		return Character.class;
	    case Types.DATE:
		return Date.class;
	    case Types.DECIMAL:
		return BigDecimal.class;
	    case Types.DOUBLE:
		return Double.class;
	    case Types.FLOAT:
		return Float.class;
	    case Types.INTEGER:
		return Integer.class;
	    case Types.VARCHAR:
		return String.class;
	    case Types.TIMESTAMP_WITH_TIMEZONE:
		return OffsetDateTime.class;
	    default:
		return null;
	}
    }
}
