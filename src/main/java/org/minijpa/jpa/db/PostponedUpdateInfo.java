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
package org.minijpa.jpa.db;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class PostponedUpdateInfo {

    private Object id;
    private Class<?> c;
    private String attributeName;

    public PostponedUpdateInfo(Object id, Class<?> c, String attributeName) {
	this.id = id;
	this.c = c;
	this.attributeName = attributeName;
    }

    public Object getId() {
	return id;
    }

    public Class<?> getC() {
	return c;
    }

    public String getAttributeName() {
	return attributeName;
    }

}
