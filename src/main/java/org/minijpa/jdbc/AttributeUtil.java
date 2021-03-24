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
import org.minijpa.jdbc.db.EntityInstanceBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttributeUtil {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(AttributeUtil.class);

    public static Object createPK(MetaEntity entity, QueryResultValues attributeValues) throws Exception {
	MetaAttribute id = entity.getId();
	if (id.isEmbedded()) {
	    Object pkObject = id.getType().getConstructor().newInstance();
	    createPK(entity, attributeValues, id.getEmbeddableMetaEntity().getAttributes(), id, pkObject);
	    return pkObject;
	}

	int index = indexOf(attributeValues.attributes, id.getName());
	return attributeValues.values.get(index);
    }

    public static void createPK(MetaEntity entity, QueryResultValues attributeValues, List<MetaAttribute> attributes,
	    MetaAttribute id, Object pkObject) throws Exception {
	for (MetaAttribute a : attributes) {
	    if (a.isEmbedded())
		createPK(entity, attributeValues, a.getEmbeddableMetaEntity().getAttributes(), id, pkObject);
	    else {
		int index = indexOf(attributeValues.attributes, a.getName());
		Object value = attributeValues.values.get(index);
		a.getWriteMethod().invoke(pkObject, value);
	    }
	}
    }

    public static int indexOf(List<MetaAttribute> attributes, String name) {
	for (int i = 0; i < attributes.size(); ++i) {
	    MetaAttribute a = attributes.get(i);
	    if (a.getName().equals(name))
		return i;
	}

	return -1;
    }

    public static int indexOfJoinColumnAttribute(List<JoinColumnAttribute> joinColumnAttributes, MetaAttribute a) {
	for (int i = 0; i < joinColumnAttributes.size(); ++i) {
	    if (joinColumnAttributes.get(i).getForeignKeyAttribute() == a)
		return i;
	}

	return -1;
    }

    public static Object getIdValue(MetaEntity entity, Object entityInstance) throws Exception {
	return entity.getId().getReadMethod().invoke(entityInstance);
    }

    public static Object getIdValue(MetaAttribute id, Object entityInstance) throws Exception {
	return id.getReadMethod().invoke(entityInstance);
    }

    /**
     * Finds the parent instance the 'attribute' belongs to. If the 'attribute' is one of the 'entity' attributes it
     * will return the 'parentInstance'.If the 'attribute' is inside an embeddable it will return the embeddable
     * instance.
     *
     * @param parentInstance
     * @param attributes
     * @param attribute
     * @param entityInstanceBuilder
     * @return
     * @throws java.lang.Exception
     */
    public static Object findParentInstance(Object parentInstance, List<MetaAttribute> attributes,
	    MetaAttribute attribute, EntityInstanceBuilder entityInstanceBuilder) throws Exception {
	for (MetaAttribute a : attributes) {
	    if (a == attribute)
		return parentInstance;
	}

	for (MetaAttribute a : attributes) {
	    if (!a.isEmbedded())
		continue;

	    MetaAttribute emb = a.getEmbeddableMetaEntity().getAttribute(attribute.getName());
	    if (emb != null)
		return entityInstanceBuilder.getAttributeValue(parentInstance, a);

	    Object p = entityInstanceBuilder.getAttributeValue(parentInstance, a);
	    Object parent = findParentInstance(p, a.getEmbeddableMetaEntity().getAttributes(),
		    attribute, entityInstanceBuilder);
	    if (parent != null)
		return parent;
	}

	return null;
    }

    /**
     * Finds the parent entity over the parentEntity entity tree. It loops over the embeddables.
     *
     * @param entityClassName
     * @param parentEntity
     * @return
     * @throws Exception
     */
    public static MetaEntity findParentEntity(String entityClassName, MetaEntity parentEntity) throws Exception {
	if (parentEntity.getEntityClass().getName().equals(entityClassName))
	    return parentEntity;

	for (MetaAttribute a : parentEntity.getAttributes()) {
	    if (!a.isEmbedded())
		continue;

	    if (a.getEmbeddableMetaEntity().getEntityClass().getName().equals(entityClassName))
		return a.getEmbeddableMetaEntity();

	    MetaEntity entity = findParentEntity(entityClassName, a.getEmbeddableMetaEntity());
	    if (entity != null)
		return entity;
	}

	return null;
    }

    public static MetaAttribute findAttributeFromPath(String path, MetaEntity toEntity) {
	String[] ss = path.split("\\.");
	if (ss.length == 0)
	    return null;

	if (ss.length == 1)
	    return toEntity.getAttribute(path);

	MetaAttribute a = toEntity.getAttribute(ss[0]);
	// it's an embedded
	MetaAttribute last = a;
	MetaAttribute tmp = null;
	for (int i = 1; i < ss.length; ++i) {
	    List<MetaAttribute> attributes = last.getEmbeddableMetaEntity().getAttributes();
	    tmp = null;
	    for (MetaAttribute attribute : attributes) {
		if (attribute.getName().equals(ss[i]))
		    tmp = attribute;
	    }

	    if (tmp == null)
		return null;

	    last = tmp;
	}

	return last;
    }
}
