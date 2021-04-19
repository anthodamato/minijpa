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

import java.util.List;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class QueryResultMapping {

    private String name;
    private final List<EntityMapping> entityMappings;
    private final List<ConstructorMapping> constructorMappings;
    private final List<SingleColumnMapping> singleColumnMappings;

    public QueryResultMapping(String name, List<EntityMapping> entityMappings,
	    List<ConstructorMapping> constructorMappings,
	    List<SingleColumnMapping> singleColumnMappings) {
	this.entityMappings = entityMappings;
	this.constructorMappings = constructorMappings;
	this.singleColumnMappings = singleColumnMappings;
    }

    public String getName() {
	return name;
    }

    public List<EntityMapping> getEntityMappings() {
	return entityMappings;
    }

    public List<ConstructorMapping> getConstructorMappings() {
	return constructorMappings;
    }

    public List<SingleColumnMapping> getSingleColumnMappings() {
	return singleColumnMappings;
    }

    public int size() {
	return entityMappings.size() + constructorMappings.size() + singleColumnMappings.size();
    }
}
