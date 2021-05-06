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

public class JoinColumnAttribute extends AbstractAttribute {

    private MetaAttribute attribute;
    private MetaAttribute foreignKeyAttribute;

    public MetaAttribute getAttribute() {
	return attribute;
    }

    public MetaAttribute getForeignKeyAttribute() {
	return foreignKeyAttribute;
    }

    public static class Builder {

	private String columnName;
	private Class<?> type;
	private Class<?> readWriteDbType;
	private DbTypeMapper dbTypeMapper;
	private Integer sqlType;
	private MetaAttribute attribute;
	private MetaAttribute foreignKeyAttribute;
	protected JdbcAttributeMapper jdbcAttributeMapper;

	public Builder withColumnName(String columnName) {
	    this.columnName = columnName;
	    return this;
	}

	public Builder withType(Class<?> type) {
	    this.type = type;
	    return this;
	}

	public Builder withReadWriteDbType(Class<?> readWriteDbType) {
	    this.readWriteDbType = readWriteDbType;
	    return this;
	}

	public Builder withDbTypeMapper(DbTypeMapper dbTypeMapper) {
	    this.dbTypeMapper = dbTypeMapper;
	    return this;
	}

	public Builder withSqlType(Integer sqlType) {
	    this.sqlType = sqlType;
	    return this;
	}

	public Builder withAttribute(MetaAttribute attribute) {
	    this.attribute = attribute;
	    return this;
	}

	public Builder withForeignKeyAttribute(MetaAttribute foreignKeyAttribute) {
	    this.foreignKeyAttribute = foreignKeyAttribute;
	    return this;
	}

	public Builder withJdbcAttributeMapper(JdbcAttributeMapper jdbcAttributeMapper) {
	    this.jdbcAttributeMapper = jdbcAttributeMapper;
	    return this;
	}

	public JoinColumnAttribute build() {
	    JoinColumnAttribute joinColumnAttribute = new JoinColumnAttribute();
	    joinColumnAttribute.columnName = columnName;
	    joinColumnAttribute.type = type;
	    joinColumnAttribute.readWriteDbType = readWriteDbType;
	    joinColumnAttribute.dbTypeMapper = dbTypeMapper;
	    joinColumnAttribute.sqlType = sqlType;
	    joinColumnAttribute.attribute = attribute;
	    joinColumnAttribute.foreignKeyAttribute = foreignKeyAttribute;
	    joinColumnAttribute.jdbcAttributeMapper = jdbcAttributeMapper;
	    return joinColumnAttribute;
	}
    }
}
