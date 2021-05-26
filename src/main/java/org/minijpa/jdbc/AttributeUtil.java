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
import java.util.Optional;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttributeUtil {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(AttributeUtil.class);

    public static final Function<FetchParameter, MetaAttribute> fetchParameterToMetaAttribute = f -> f.getAttribute();

    public static Object buildPK(Pk id, ModelValueArray<FetchParameter> modelValueArray) throws Exception {
	if (id.isEmbedded()) {
	    Object pkObject = id.getType().getConstructor().newInstance();
	    buildPK(modelValueArray, id.getAttributes(), pkObject);
	    return pkObject;
	}

	int index = modelValueArray.indexOfModel(fetchParameterToMetaAttribute, id.getAttribute());
	if (index == -1)
	    throw new IllegalArgumentException("Column '" + id.getAttribute().getColumnName() + "' is missing");

	return modelValueArray.getValue(index);
    }

    private static void buildPK(ModelValueArray<FetchParameter> modelValueArray,
	    List<MetaAttribute> attributes, Object pkObject) throws Exception {
//	LOG.debug("buildPK: pkObject=" + pkObject);
	for (MetaAttribute a : attributes) {
	    int index = modelValueArray.indexOfModel(fetchParameterToMetaAttribute, a);
	    LOG.debug("buildPK: index=" + index);
	    if (index == -1)
		throw new IllegalArgumentException("Column '" + a.getColumnName() + "' is missing");

	    a.getWriteMethod().invoke(pkObject, modelValueArray.getValue(index));
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

    public static Object getIdValue(Pk id, Object entityInstance) throws Exception {
	return id.getReadMethod().invoke(entityInstance);
    }

    public static MetaAttribute findAttributeFromPath(String path, MetaEntity toEntity) {
	String[] ss = path.split("\\.");
	if (ss.length == 0)
	    return null;

	if (ss.length == 1)
	    return toEntity.getAttribute(path);

	Optional<MetaEntity> optional = toEntity.getEmbeddable(ss[0]);
	if (optional.isEmpty())
	    return null;

	// it's an embedded
	MetaEntity embeddable = optional.get();
	for (int i = 1; i < ss.length; ++i) {
	    Optional<MetaEntity> opt = embeddable.getEmbeddable(ss[i]);
	    if (opt.isPresent()) {
		embeddable = opt.get();
	    } else {
		MetaAttribute attribute = embeddable.getAttribute(ss[i]);
		if (attribute == null)
		    return null;

		if (i == ss.length - 1)
		    return attribute;

		return null;
	    }
	}

	return null;
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

	if (c.isEnum())
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
