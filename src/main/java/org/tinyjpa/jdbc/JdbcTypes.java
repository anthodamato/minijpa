package org.tinyjpa.jdbc;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Types;

public class JdbcTypes {
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

		return null;
	}
}
