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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTypes {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(JdbcTypes.class);
    private static int typeOrder[] = {Types.TINYINT, Types.SMALLINT, Types.INTEGER, Types.BIGINT, Types.FLOAT, Types.REAL, Types.DOUBLE, Types.DECIMAL, Types.NUMERIC};

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
                return Types.BIGINT;

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

    public static int compareNumericTypes(int sqlType1, int sqlType2) {
        Integer index1 = -1;
        for (int i = 0; i < typeOrder.length; ++i) {
            if (typeOrder[i] == sqlType1) {
                index1 = i;
                break;
            }
        }

        Integer index2 = -1;
        for (int i = 0; i < typeOrder.length; ++i) {
            if (typeOrder[i] == sqlType2) {
                index2 = i;
                break;
            }
        }

        return index1.compareTo(index2);
    }
}
