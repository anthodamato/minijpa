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

import org.minijpa.jdbc.mapper.JdbcAttributeMapper;

public interface DbTypeMapper {

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
     * Converts the 'value' read from a resultSet with type 'readWriteDbType' to an object with class 'attributeType'.
     *
     * @param value
     * @param readWriteDbType
     * @param attributeType
     * @return
     */
    public Object convert(Object value, Class<?> readWriteDbType, Class<?> attributeType);

    /**
     * Returns the attribute converter.
     *
     * @param attributeType
     * @param jdbcType
     * @return
     */
    public JdbcAttributeMapper mapJdbcAttribute(Class<?> attributeType, Integer jdbcType);
}
