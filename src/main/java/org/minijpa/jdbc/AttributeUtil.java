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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import org.minijpa.jdbc.db.EntityInstanceBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttributeUtil {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(AttributeUtil.class);

    public static final Function<FetchParameter, MetaAttribute> fetchParameterToMetaAttribute = f -> f.getAttribute();

    public static Object buildPK(MetaEntity entity, ModelValueArray<FetchParameter> modelValueArray) throws Exception {
	MetaAttribute id = entity.getId();
	if (id.isEmbedded()) {
	    Object pkObject = id.getType().getConstructor().newInstance();
	    buildPK(entity, modelValueArray, id.getEmbeddableMetaEntity().getAttributes(), id, pkObject);
	    return pkObject;
	}

	int index = modelValueArray.indexOfModel(fetchParameterToMetaAttribute, id);
	if (index == -1)
	    throw new IllegalArgumentException("Column '" + id.getColumnName() + "' is missing");

	return modelValueArray.getValue(index);
    }

    private static void buildPK(MetaEntity entity, ModelValueArray<FetchParameter> modelValueArray,
	    List<MetaAttribute> attributes, MetaAttribute id, Object pkObject) throws Exception {
	for (MetaAttribute a : attributes) {
	    if (a.isEmbedded())
		buildPK(entity, modelValueArray, a.getEmbeddableMetaEntity().getAttributes(), id, pkObject);
	    else {
		int index = modelValueArray.indexOfModel(fetchParameterToMetaAttribute, a);
		if (index == -1)
		    throw new IllegalArgumentException("Column '" + a.getColumnName() + "' is missing");

		a.getWriteMethod().invoke(pkObject, modelValueArray.getValue(index));
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

    public static boolean isBasicAttribute(Class<?> c) {
	if (c == String.class)
	    return true;

	if (c == Long.class)
	    return true;

	if (c == BigInteger.class)
	    return true;

	if (c == Boolean.class)
	    return true;

	if (c == Character.class)
	    return true;

	if (c == BigDecimal.class)
	    return true;

	if (c == Double.class)
	    return true;

	if (c == Float.class)
	    return true;

	if (c == Integer.class)
	    return true;

	if (c == Date.class)
	    return true;

	if (c == LocalDate.class)
	    return true;

	if (c == LocalDateTime.class)
	    return true;

	if (c == OffsetDateTime.class)
	    return true;

	if (c == OffsetTime.class)
	    return true;

	if (c == Calendar.class)
	    return true;

	if (c == Timestamp.class)
	    return true;

	if (c == LocalTime.class)
	    return true;

	if (c.isPrimitive()) {
	    if (c.getName().equals("byte"))
		return true;

	    if (c.getName().equals("short"))
		return true;

	    if (c.getName().equals("int"))
		return true;

	    if (c.getName().equals("long"))
		return true;

	    if (c.getName().equals("float"))
		return true;

	    if (c.getName().equals("double"))
		return true;

	    if (c.getName().equals("boolean"))
		return true;

	    if (c.getName().equals("char"))
		return true;
	}

	return false;
    }

    public static Object increaseVersionValue(MetaEntity metaEntity, Object currentValue) throws Exception {
	if (!metaEntity.hasVersionAttribute())
	    return null;

	MetaAttribute attribute = metaEntity.getVersionAttribute().get();
	Class<?> type = attribute.getType();
	if (type == Integer.class || (type.isPrimitive() && type.getName().equals("int"))) {
	    Integer v = (Integer) currentValue;
	    return v + 1;
	} else if (type == Short.class || (type.isPrimitive() && type.getName().equals("short"))) {
	    Short v = (Short) currentValue;
	    return v + 1;
	} else if (type == Long.class || (type.isPrimitive() && type.getName().equals("long"))) {
	    Long v = (Long) currentValue;
	    return v + 1;
	} else if (type == Timestamp.class) {
	    Timestamp v = (Timestamp) currentValue;
	    return new Timestamp(v.getTime() + 100);
	}

	return null;
    }

}
