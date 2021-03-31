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
import java.util.List;
import org.minijpa.jdbc.AttributeValueArray;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;

public interface EntityInstanceBuilder {

    public Object build(MetaEntity entity, Object idValue)
	    throws Exception;

    public Object setAttributeValue(Object parentInstance, Class<?> parentClass, MetaAttribute attribute,
	    Object value, MetaEntity entity) throws Exception;

    public void setAttributeValues(MetaEntity entity, Object entityInstance, List<MetaAttribute> attributes,
	    List<Object> values) throws Exception;

    public Object getAttributeValue(Object parentInstance, MetaAttribute attribute) throws Exception;

    public void removeChanges(MetaEntity entity, Object entityInstance)
	    throws IllegalAccessException, InvocationTargetException;

    public AttributeValueArray getModifications(MetaEntity entity, Object entityInstance)
	    throws IllegalAccessException, InvocationTargetException;
}
