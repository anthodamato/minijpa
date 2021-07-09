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

import org.minijpa.jdbc.mapper.AttributeMapper;
import org.minijpa.jdbc.mapper.JdbcAttributeMapper;

public interface DbTypeMapper {

    /**
     * Finds a mapper that converts an attribute value of type <code>attributeType</code> to a value of type
     * <code>databaseType</code> and vice-versa.
     *
     *
     * @param attributeType
     * @param databaseType
     * @return
     */
    public AttributeMapper attributeMapper(Class<?> attributeType, Class<?> databaseType);

    /**
     * Maps the attribute type to the db type. For example, on Apache Derby if a column has the 'DATE' data type a
     * LocalDate attribute type is mapped as Date.
     *
     * @param attributeType
     * @param jdbcType
     * @return
     */
    public Class<?> map(Class<?> attributeType, Integer jdbcType);

    /**
     * Converts the 'value' read from a resultSet to an object with class 'attributeType'.
     *
     * @param value
     * @param attributeType
     * @return
     */
    public Object convertToAttributeType(Object value, Class<?> attributeType);

    /**
     * Converts the 'value' read from a resultSet with type 'readWriteDbType' to an object with class 'attributeType'.
     * This method is called only to convert the generated key of an identity column.
     *
     * @param value
     * @param attributeType
     * @return
     */
    public Object convertGeneratedKey(Object value, Class<?> attributeType);

    /**
     * Returns the attribute converter.
     *
     * @param attributeType
     * @param jdbcType
     * @return
     */
    public JdbcAttributeMapper mapJdbcAttribute(Class<?> attributeType, Integer jdbcType);
}
