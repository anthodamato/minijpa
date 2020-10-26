package org.tinyjpa.jdbc;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Types;
import java.time.LocalDate;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTypes {
	private static Logger LOG = LoggerFactory.getLogger(JdbcTypes.class);

	public static Integer sqlTypeFromClass(Class<?> c) {
		if (c == BigDecimal.class)
			return Types.DECIMAL;

		if (c == BigInteger.class)
			return Types.BIGINT;

		if (c == Boolean.class)
			return Types.BOOLEAN;

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

		if (c == Character.class)
			return Types.CHAR;

		if (c == Date.class)
			return Types.DATE;

		if (c == LocalDate.class)
			return Types.DATE;

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

		return null;
	}
}
