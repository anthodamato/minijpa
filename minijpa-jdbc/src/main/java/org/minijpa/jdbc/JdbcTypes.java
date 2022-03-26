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
import java.sql.Types;
import java.time.OffsetDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTypes {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(JdbcTypes.class);

	public static Integer sqlTypeFromClass(Class<?> c) {
		if (c == Boolean.class)
			return Types.BOOLEAN;

		if (c == Character.class)
			return Types.CHAR;

		if (c == java.sql.Date.class)
			return Types.DATE;

		if (c == java.sql.Time.class)
			return Types.TIME;

		if (c == java.sql.Timestamp.class)
			return Types.TIMESTAMP;

		if (c == BigDecimal.class)
			return Types.DECIMAL;

		if (c == Double.class)
			return Types.DOUBLE;

		if (c == Float.class)
			return Types.FLOAT;

		if (c == Integer.class)
			return Types.INTEGER;

		if (c == Long.class)
			return Types.BIGINT;

		if (c == String.class)
			return Types.VARCHAR;

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

	public static Class classFromSqlType(int type, int precision, int scale) {
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
			case Types.TIME:
				return java.sql.Time.class;
			case Types.TIMESTAMP:
				return java.sql.Timestamp.class;
			case Types.NUMERIC:
				if (precision == 10 && scale == 0)
					return Integer.class;

				if (precision == 19 && scale == 0)
					return Long.class;

				if (precision == 19 && scale == 4)
					return Float.class;

				return BigDecimal.class;
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
			default:
				return null;
		}
	}

	public static Class<?> getWrapperClass(Class<?> c) {
		if (!c.isPrimitive())
			return c;

		if (c.getName().equals("byte"))
			return Byte.class;

		if (c.getName().equals("short"))
			return Short.class;

		if (c.getName().equals("int"))
			return Integer.class;

		if (c.getName().equals("long"))
			return Long.class;

		if (c.getName().equals("float"))
			return Float.class;

		if (c.getName().equals("double"))
			return Double.class;

		if (c.getName().equals("boolean"))
			return Boolean.class;

		if (c.getName().equals("char"))
			return Character.class;

		return c;
	}
}
