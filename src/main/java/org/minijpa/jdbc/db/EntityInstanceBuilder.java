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
package org.minijpa.jdbc.db;

import java.lang.reflect.InvocationTargetException;
import org.minijpa.jdbc.ModelValueArray;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;

public interface EntityInstanceBuilder {

    public Object build(MetaEntity entity, Object idValue)
	    throws Exception;

    public Object writeMetaAttributeValue(Object parentInstance, Class<?> parentClass, MetaAttribute attribute,
	    Object value, MetaEntity entity) throws Exception;

    public Object writeEmbeddableValue(Object parentInstance, Class<?> parentClass, MetaEntity embeddable,
	    Object value, MetaEntity entity) throws Exception;

    public Object writeAttributeValue(MetaEntity entity, Object parentInstance, MetaAttribute attribute,
	    Object value) throws Exception;

    public Object getAttributeValue(Object parentInstance, MetaAttribute attribute) throws Exception;

    public Object getEmbeddableValue(Object parentInstance, MetaEntity metaEntity) throws IllegalAccessException, InvocationTargetException;

    public void removeChanges(MetaEntity entity, Object entityInstance)
	    throws IllegalAccessException, InvocationTargetException;

    public ModelValueArray<MetaAttribute> getModifications(MetaEntity entity, Object entityInstance)
	    throws IllegalAccessException, InvocationTargetException;
}
