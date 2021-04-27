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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class BasicAttributePk implements Pk {

    private final MetaAttribute attribute;
    private final PkGeneration pkGeneration;

    public BasicAttributePk(MetaAttribute attribute, PkGeneration pkGeneration) {
	this.attribute = attribute;
	this.pkGeneration = pkGeneration;
    }

    @Override
    public PkGeneration getPkGeneration() {
	return pkGeneration;
    }

    @Override
    public boolean isEmbedded() {
	return false;
    }

    @Override
    public MetaAttribute getAttribute() {
	return attribute;
    }

    @Override
    public List<MetaAttribute> getAttributes() {
	return Arrays.asList(attribute);
    }

    @Override
    public Class<?> getType() {
	return attribute.getType();
    }

    @Override
    public String getName() {
	return attribute.getName();
    }

    @Override
    public Method getReadMethod() {
	return attribute.getReadMethod();
    }

    @Override
    public Method getWriteMethod() {
	return attribute.getWriteMethod();
    }

}
