package org.minijpa.jdbc;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Types;
import java.time.LocalDate;
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
	    default:
		return null;
	}
    }
}
