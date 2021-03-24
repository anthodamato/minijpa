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

/**
 *
 * @author adamato
 */
public class PkSequenceGenerator {

    private String name;
    private String sequenceName;
    private String schema;
    private int allocationSize;
    private int initialValue;
    private String catalog;

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getSequenceName() {
	return sequenceName;
    }

    public void setSequenceName(String sequenceName) {
	this.sequenceName = sequenceName;
    }

    public String getSchema() {
	return schema;
    }

    public void setSchema(String schema) {
	this.schema = schema;
    }

    public int getAllocationSize() {
	return allocationSize;
    }

    public void setAllocationSize(int allocationSize) {
	this.allocationSize = allocationSize;
    }

    public int getInitialValue() {
	return initialValue;
    }

    public void setInitialValue(int initialValue) {
	this.initialValue = initialValue;
    }

    public String getCatalog() {
	return catalog;
    }

    public void setCatalog(String catalog) {
	this.catalog = catalog;
    }

}
