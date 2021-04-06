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

public class ColumnNameValue {

    private String columnName;
    private Object value;
    private Class<?> type;
    private Class<?> readWriteDbType;
    private Integer sqlType;
    private MetaAttribute foreignKeyAttribute;
    private MetaAttribute attribute;

    public ColumnNameValue(String columnName, Object value, Class<?> type, Class<?> readWriteDbType, Integer sqlType,
	    MetaAttribute foreignKeyAttribute, MetaAttribute attribute) {
	super();
	this.columnName = columnName;
	this.value = value;
	this.type = type;
	this.readWriteDbType = readWriteDbType;
	this.sqlType = sqlType;
	this.foreignKeyAttribute = foreignKeyAttribute;
	this.attribute = attribute;
    }

    public static ColumnNameValue build(MetaAttribute av) {
	ColumnNameValue cnv = new ColumnNameValue(av.getColumnName(), null, av.getType(), av.getReadWriteDbType(),
		av.getSqlType(), null, av);
	return cnv;
    }

    public String getColumnName() {
	return columnName;
    }

    public Object getValue() {
	return value;
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

    public MetaAttribute getForeignKeyAttribute() {
	return foreignKeyAttribute;
    }

    public MetaAttribute getAttribute() {
	return attribute;
    }

}
