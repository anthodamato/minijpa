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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class MetaEntity {

    private final Class<?> entityClass;
    private final String name;
    private final String tableName;
    private final String alias;
    private final MetaAttribute id;
    /**
     * Collection of simple, relationship and embeddable attributes.
     */
    private final List<MetaAttribute> attributes;
    private final List<JoinColumnAttribute> joinColumnAttributes = new ArrayList<>();
    // used to build the metamodel. The 'attributes' field contains the
    // MappedSuperclass attributes
    private final MetaEntity mappedSuperclassEntity;
    private final Method modificationAttributeReadMethod;
    private final Optional<Method> lazyLoadedAttributeReadMethod;
    private final Optional<Method> lockTypeAttributeReadMethod;
    private final Optional<Method> lockTypeAttributeWriteMethod;
    private final Optional<Method> entityStatusAttributeReadMethod;
    private final Optional<Method> entityStatusAttributeWriteMethod;

    public MetaEntity(Class<?> entityClass, String name, String tableName, String alias, MetaAttribute id,
	    List<MetaAttribute> attributes, MetaEntity mappedSuperclassEntity,
	    Method modificationAttributeReadMethod, Optional<Method> lazyLoadedAttributeReadMethod,
	    Optional<Method> lockTypeAttributeReadMethod, Optional<Method> lockTypeAttributeWriteMethod,
	    Optional<Method> entityStatusAttributeReadMethod, Optional<Method> entityStatusAttributeWriteMethod) {
	super();
	this.entityClass = entityClass;
	this.name = name;
	this.tableName = tableName;
	this.alias = alias;
	this.id = id;
	this.attributes = attributes;
	this.mappedSuperclassEntity = mappedSuperclassEntity;
	this.modificationAttributeReadMethod = modificationAttributeReadMethod;
	this.lazyLoadedAttributeReadMethod = lazyLoadedAttributeReadMethod;
	this.lockTypeAttributeReadMethod = lockTypeAttributeReadMethod;
	this.lockTypeAttributeWriteMethod = lockTypeAttributeWriteMethod;
	this.entityStatusAttributeReadMethod = entityStatusAttributeReadMethod;
	this.entityStatusAttributeWriteMethod = entityStatusAttributeWriteMethod;
    }

    public Class<?> getEntityClass() {
	return entityClass;
    }

    public String getName() {
	return name;
    }

    public String getTableName() {
	return tableName;
    }

    public String getAlias() {
	return alias;
    }

    public List<MetaAttribute> getAttributes() {
	return attributes;
    }

    public List<JoinColumnAttribute> getJoinColumnAttributes() {
	return joinColumnAttributes;
    }

    public MetaEntity getMappedSuperclassEntity() {
	return mappedSuperclassEntity;
    }

    public Method getModificationAttributeReadMethod() {
	return modificationAttributeReadMethod;
    }

    public Optional<Method> getLazyLoadedAttributeReadMethod() {
	return lazyLoadedAttributeReadMethod;
    }

    public Optional<Method> getLockTypeAttributeReadMethod() {
	return lockTypeAttributeReadMethod;
    }

    public Optional<Method> getLockTypeAttributeWriteMethod() {
	return lockTypeAttributeWriteMethod;
    }

    public Optional<Method> getEntityStatusAttributeReadMethod() {
	return entityStatusAttributeReadMethod;
    }

    public Optional<Method> getEntityStatusAttributeWriteMethod() {
	return entityStatusAttributeWriteMethod;
    }

    public MetaAttribute getAttribute(String name) {
	for (MetaAttribute attribute : attributes) {
	    if (attribute.getName().equals(name))
		return attribute;
	}

	if (id.getName().equals(name))
	    return id;

	return null;
    }

    public MetaAttribute getId() {
	return id;
    }

    public List<MetaAttribute> expandAttributes() {
	List<MetaAttribute> list = new ArrayList<>();
	for (MetaAttribute a : attributes) {
	    list.addAll(a.expand());
	}

	return list;
    }

    public List<MetaAttribute> expandAllAttributes() {
	List<MetaAttribute> list = new ArrayList<>();
	list.addAll(id.expand());
	for (MetaAttribute a : attributes) {
	    list.addAll(a.expand());
	}

	return list;
    }

    public MetaAttribute findAttributeByMappedBy(String mappedBy) {
	for (MetaAttribute attribute : attributes) {
	    if (attribute.getRelationship() != null && mappedBy.equals(attribute.getRelationship().getMappedBy()))
		return attribute;
	}

	return null;
    }

    public List<MetaAttribute> getRelationshipAttributes() {
	return attributes.stream().filter(a -> a.getRelationship() != null).collect(Collectors.toList());
    }

    public boolean isEmbeddedAttribute(String name) {
	Optional<MetaAttribute> optional = attributes.stream().filter(a -> a.getName().equals(name) && a.isEmbedded())
		.findFirst();
	return optional.isPresent();
    }

    @Override
    public String toString() {
	return super.toString() + " class: " + entityClass.getName() + "; tableName: " + tableName;
    }

    public List<MetaAttribute> notNullableAttributes() {
	return attributes.stream().filter(a -> !a.isNullable()).collect(Collectors.toList());
    }

    public void findEmbeddables(Set<MetaEntity> embeddables) {
	for (MetaAttribute enhAttribute : attributes) {
	    if (enhAttribute.isEmbedded()) {
		MetaEntity metaEntity = enhAttribute.getEmbeddableMetaEntity();
		embeddables.add(metaEntity);

		metaEntity.findEmbeddables(embeddables);
	    }
	}
    }

    public boolean hasVersionAttribute() {
	return attributes.stream().filter(a -> a.isVersion() && a.isBasic() && !a.isId()).findFirst().isPresent();
    }

    public Optional<MetaAttribute> getVersionAttribute() {
	return attributes.stream().filter(a -> a.isVersion() && a.isBasic() && !a.isId()).findFirst();
    }
}
