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

import java.util.Optional;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class DDLData {

    private final Optional<String> columnDefinition;
    private final Optional<Integer> length;
    private final Optional<Integer> precision;
    private final Optional<Integer> scale;
    private final Optional<Boolean> nullable;

    public DDLData(Optional<String> columnDefinition,
	    Optional<Integer> length,
	    Optional<Integer> precision,
	    Optional<Integer> scale,
	    Optional<Boolean> nullable) {
	this.columnDefinition = columnDefinition;
	this.length = length;
	this.precision = precision;
	this.scale = scale;
	this.nullable = nullable;
    }

    public Optional<String> getColumnDefinition() {
	return columnDefinition;
    }

    public Optional<Integer> getLength() {
	return length;
    }

    public Optional<Integer> getPrecision() {
	return precision;
    }

    public Optional<Integer> getScale() {
	return scale;
    }

    public Optional<Boolean> getNullable() {
	return nullable;
    }

}
