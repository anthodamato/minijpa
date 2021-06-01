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
		return Long.class;
	    case Types.BOOLEAN:
	    case Types.BIT:
		return Boolean.class;
	    case Types.CHAR:
		return Character.class;
	    case Types.DATE:
		return java.sql.Date.class;
	    case Types.DECIMAL:
		return BigDecimal.class;
	    case Types.DOUBLE:
		return Double.class;
	    case Types.FLOAT:
	    case Types.REAL:
		return Float.class;
	    case Types.INTEGER:
		return Integer.class;
	    case Types.VARCHAR:
		return String.class;
	    case Types.TIMESTAMP_WITH_TIMEZONE:
		return OffsetDateTime.class;
	    case Types.NUMERIC:
		return BigDecimal.class;
	    default:
		return null;
	}
    }
}
