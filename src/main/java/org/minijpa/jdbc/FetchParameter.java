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

public class FetchParameter {

    private final String columnName;
    private final Class<?> type;
    private final Class<?> readWriteDbType;
    private final Integer sqlType;
    private final MetaAttribute attribute;
    private final boolean joinColumn;

    public FetchParameter(String columnName, Class<?> type, Class<?> readWriteDbType, Integer sqlType,
	    MetaAttribute attribute, boolean joinColumn) {
	super();
	this.columnName = columnName;
	this.type = type;
	this.readWriteDbType = readWriteDbType;
	this.sqlType = sqlType;
	this.attribute = attribute;
	this.joinColumn = joinColumn;
    }

    public static FetchParameter build(MetaAttribute attribute) {
	return new FetchParameter(attribute.getColumnName(), attribute.getType(),
		attribute.getReadWriteDbType(), attribute.getSqlType(), attribute, false);
    }

    public String getColumnName() {
	return columnName;
    }

    public Class<?> getType() {
	return type;
    }

    public Class<?> getReadWriteDbType() {
	return readWriteDbType;
    }

    public Integer getSqlType() {
	return sqlType;
    }

    public MetaAttribute getAttribute() {
	return attribute;
    }

    public boolean isJoinColumn() {
	return joinColumn;
    }

}
