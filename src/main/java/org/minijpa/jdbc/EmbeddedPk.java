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
import java.util.List;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class EmbeddedPk implements Pk {

    private final MetaEntity entity;
    private final PkGeneration pkGeneration = new PkGeneration();

    public EmbeddedPk(MetaEntity entity) {
	this.entity = entity;
    }

    @Override
    public PkGeneration getPkGeneration() {
	return pkGeneration;
    }

    @Override
    public boolean isEmbedded() {
	return true;
    }

    @Override
    public MetaAttribute getAttribute() {
	return null;
    }

    @Override
    public List<MetaAttribute> getAttributes() {
	return entity.getAttributes();
    }

    @Override
    public Class<?> getType() {
	return entity.getEntityClass();
    }

    @Override
    public String getName() {
	return entity.getName();
    }

    @Override
    public Method getReadMethod() {
	return entity.getReadMethod();
    }

    @Override
    public Method getWriteMethod() {
	return entity.getWriteMethod();
    }

}
