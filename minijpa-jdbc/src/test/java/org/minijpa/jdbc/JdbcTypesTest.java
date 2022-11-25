package org.minijpa.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;

import org.junit.jupiter.api.Test;

public class JdbcTypesTest {
    @Test
    public void jdbcTypes() {
        assertEquals(Types.BOOLEAN, JdbcTypes.sqlTypeFromClass(Boolean.class));
        assertEquals(Types.CHAR, JdbcTypes.sqlTypeFromClass(Character.class));
        assertEquals(Types.DATE, JdbcTypes.sqlTypeFromClass(Date.class));
        assertEquals(Types.TIME, JdbcTypes.sqlTypeFromClass(Time.class));
        assertEquals(Types.TIMESTAMP, JdbcTypes.sqlTypeFromClass(Timestamp.class));
        assertEquals(Types.DECIMAL, JdbcTypes.sqlTypeFromClass(BigDecimal.class));
        assertEquals(Types.DOUBLE, JdbcTypes.sqlTypeFromClass(Double.class));
        assertEquals(Types.FLOAT, JdbcTypes.sqlTypeFromClass(Float.class));
        assertEquals(Types.INTEGER, JdbcTypes.sqlTypeFromClass(Integer.class));
        assertEquals(Types.BIGINT, JdbcTypes.sqlTypeFromClass(Long.class));
        assertEquals(Types.VARCHAR, JdbcTypes.sqlTypeFromClass(String.class));
        assertEquals(Types.INTEGER, JdbcTypes.sqlTypeFromClass(byte.class));
        assertEquals(Types.INTEGER, JdbcTypes.sqlTypeFromClass(short.class));
        assertEquals(Types.INTEGER, JdbcTypes.sqlTypeFromClass(int.class));
        assertEquals(Types.BIGINT, JdbcTypes.sqlTypeFromClass(long.class));
        assertEquals(Types.FLOAT, JdbcTypes.sqlTypeFromClass(float.class));
        assertEquals(Types.DOUBLE, JdbcTypes.sqlTypeFromClass(double.class));
        assertEquals(Types.BOOLEAN, JdbcTypes.sqlTypeFromClass(boolean.class));
        assertEquals(Types.CHAR, JdbcTypes.sqlTypeFromClass(char.class));
    }
}
