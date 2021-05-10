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
package org.minijpa.jdbc.model;

import java.util.List;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.Pk;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class SqlCreateTable implements SqlDDLStatement {

    private final String tableName;
    private final Pk pk;
    private final List<MetaAttribute> attributes;
    private final List<ForeignKeyDeclaration> foreignKeyDeclarations;

    public SqlCreateTable(String tableName,
	    Pk pk,
	    List<MetaAttribute> attributes,
	    List<ForeignKeyDeclaration> foreignKeyDeclarations) {
	this.tableName = tableName;
	this.pk = pk;
	this.attributes = attributes;
	this.foreignKeyDeclarations = foreignKeyDeclarations;
    }

    public String getTableName() {
	return tableName;
    }

    public Pk getPk() {
	return pk;
    }

    public List<MetaAttribute> getAttributes() {
	return attributes;
    }

    public List<ForeignKeyDeclaration> getForeignKeyDeclarations() {
	return foreignKeyDeclarations;
    }

}
