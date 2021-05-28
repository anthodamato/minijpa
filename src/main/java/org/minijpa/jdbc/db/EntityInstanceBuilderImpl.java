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
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import org.minijpa.jdbc.ModelValueArray;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityInstanceBuilderImpl implements EntityInstanceBuilder {

    private final Logger LOG = LoggerFactory.getLogger(EntityInstanceBuilderImpl.class);

    @Override
    public Object build(MetaEntity entity, Object idValue) throws Exception {
	Object entityInstance = entity.getEntityClass().getDeclaredConstructor().newInstance();
	entity.getId().getWriteMethod().invoke(entityInstance, idValue);
	return entityInstance;
    }

    @Override
    public Object writeAttributeValue(MetaEntity entity, Object parentInstance, MetaAttribute attribute,
	    Object value) throws Exception {
	LOG.debug("writeAttributeValue: parentInstance=" + parentInstance);
	LOG.debug("writeAttributeValue: attribute.getName()=" + attribute.getName() + "; value=" + value);
	return findAndSetAttributeValue(entity.getEntityClass(), parentInstance, attribute,
		value, entity);
    }

    @Override
    public Object writeMetaAttributeValue(Object parentInstance, Class<?> parentClass, MetaAttribute attribute,
	    Object value, MetaEntity entity) throws Exception {
	Object parent = parentInstance;
	if (parent == null)
	    parent = parentClass.getDeclaredConstructor().newInstance();

	LOG.debug("writeMetaAttributeValue: parent=" + parent + "; a.getWriteMethod()=" + attribute.getWriteMethod());
	LOG.debug("writeMetaAttributeValue: value=" + value);

	attribute.getWriteMethod().invoke(parent, value);
	Method m = entity.getModificationAttributeReadMethod();
	List list = (List) m.invoke(parent);
	list.remove(attribute.getName());
	return parent;
    }

    @Override
    public Object writeEmbeddableValue(Object parentInstance, Class<?> parentClass, MetaEntity embeddable,
	    Object value, MetaEntity entity) throws Exception {
	Object parent = parentInstance;
	if (parent == null) {
	    parent = parentClass.getDeclaredConstructor().newInstance();
	}

	LOG.debug("writeEmbeddableValue: parent=" + parent + "; a.getWriteMethod()=" + embeddable.getWriteMethod());
	LOG.debug("writeEmbeddableValue: value=" + value);

	embeddable.getWriteMethod().invoke(parent, value);
	Method m = entity.getModificationAttributeReadMethod();
	List list = (List) m.invoke(parent);
	list.remove(embeddable.getName());
	return parent;
    }

    @Override
    public Object getAttributeValue(Object parentInstance, MetaAttribute attribute) throws Exception {
	LOG.debug(
		"getAttributeValue: parent=" + parentInstance + "; a.getReadMethod()=" + attribute.getReadMethod());
	return attribute.getReadMethod().invoke(parentInstance);
    }

    @Override
    public Object getEmbeddableValue(Object parentInstance, MetaEntity embeddable) throws IllegalAccessException, InvocationTargetException {
	LOG.debug(
		"getEmbeddableValue: parent=" + parentInstance + "; embeddable.getReadMethod()=" + embeddable.getReadMethod());
	return embeddable.getReadMethod().invoke(parentInstance);
    }

    private Object findAndSetAttributeValue(Class<?> parentClass, Object parentInstance,
	    MetaAttribute attribute, Object value, MetaEntity entity)
	    throws Exception {
	LOG.debug("findAndSetAttributeValue: value=" + value + "; attribute=" + attribute);
	LOG.debug("findAndSetAttributeValue: parentInstance=" + parentInstance + "; parentClass=" + parentClass);
	LOG.debug("findAndSetAttributeValue: entity=" + entity);

	// search over all attributes
	for (MetaAttribute a : entity.getAttributes()) {
	    if (a == attribute) {
		return writeMetaAttributeValue(parentInstance, parentClass, a, value, entity);
	    }
	}

	for (MetaEntity embeddable : entity.getEmbeddables()) {
	    Object parent = getEmbeddableValue(parentInstance, embeddable);
	    Object aInstance = findAndSetAttributeValue(embeddable.getEntityClass(), parent,
		    attribute, value, embeddable);
	    if (aInstance != null) {
		return writeEmbeddableValue(parentInstance, parentClass, embeddable, aInstance, entity);
	    }
	}

	return null;
    }

    @Override
    public void removeChanges(MetaEntity entity, Object entityInstance) throws IllegalAccessException, InvocationTargetException {
	Method m = entity.getModificationAttributeReadMethod();
	List list = (List) m.invoke(entityInstance);
	list.clear();
    }

    @Override
    public ModelValueArray<MetaAttribute> getModifications(MetaEntity entity, Object entityInstance)
	    throws IllegalAccessException, InvocationTargetException {
	ModelValueArray<MetaAttribute> modelValueArray = new ModelValueArray();
	Method m = entity.getModificationAttributeReadMethod();
	List list = (List) m.invoke(entityInstance);
	if (list.isEmpty())
	    return modelValueArray;

	for (Object p : list) {
	    String v = (String) p;
	    MetaAttribute attribute = entity.getAttribute(v);
	    if (attribute == null) {
		Optional<MetaEntity> embeddable = entity.getEmbeddable(v);
		if (embeddable.isPresent()) {
		    Object embeddedInstance = getEmbeddableValue(entityInstance, embeddable.get());
		    ModelValueArray<MetaAttribute> mva = getModifications(embeddable.get(), embeddedInstance);
		    modelValueArray.add(mva);
		}
	    } else {
		Object value = attribute.getReadMethod().invoke(entityInstance);
		modelValueArray.add(attribute, value);
	    }
	}

	return modelValueArray;
    }

}
