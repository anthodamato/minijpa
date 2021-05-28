/*
 * Copyright 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
