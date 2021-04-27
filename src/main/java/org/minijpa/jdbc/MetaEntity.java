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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class MetaEntity {

    private Class<?> entityClass;
    private String name;
    private String tableName;
    private String alias;
    private Pk id;
    /**
     * Collection of basic and relationship attributes.
     */
    private List<MetaAttribute> attributes;
    private List<MetaEntity> embeddables = Collections.emptyList();
    private Method readMethod; // used for embeddables
    private Method writeMethod; // used for embeddables
    private String path; // used for embeddables
    private boolean embeddedId;
    private final List<JoinColumnAttribute> joinColumnAttributes = new ArrayList<>();
    // used to build the metamodel. The 'attributes' field contains the
    // MappedSuperclass attributes
    private MetaEntity mappedSuperclassEntity;
    private Method modificationAttributeReadMethod;
    private Optional<Method> lazyLoadedAttributeReadMethod = Optional.empty();
    private Optional<Method> lockTypeAttributeReadMethod = Optional.empty();
    private Optional<Method> lockTypeAttributeWriteMethod = Optional.empty();
    private Optional<Method> entityStatusAttributeReadMethod = Optional.empty();
    private Optional<Method> entityStatusAttributeWriteMethod = Optional.empty();

    private MetaEntity() {
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

    public Pk getId() {
	return id;
    }

    public boolean isEmbeddedId() {
	return embeddedId;
    }

    public List<MetaAttribute> getAttributes() {
	return attributes;
    }

    public List<MetaEntity> getEmbeddables() {
	return embeddables;
    }

    public Method getReadMethod() {
	return readMethod;
    }

    public Method getWriteMethod() {
	return writeMethod;
    }

    public String getPath() {
	return path;
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

	return null;
    }

    public Optional<MetaAttribute> findAttribute(String name) {
	List<MetaAttribute> list = expandAllAttributes();
	return list.stream().filter(a -> a.getName().equals(name)).findFirst();
    }

    public Optional<MetaEntity> getEmbeddable(String name) {
	return embeddables.stream().filter(e -> e.name.equals(name)).findFirst();
    }

    public List<MetaAttribute> expandAttributes() {
	List<MetaAttribute> list = new ArrayList<>();
	attributes.forEach(a -> {
	    list.addAll(a.expand());
	});

	return list;
    }

    public List<MetaAttribute> expandAllAttributes() {
	List<MetaAttribute> list = new ArrayList<>();
	if (id != null)
	    list.addAll(id.getAttributes());

	attributes.forEach(a -> {
	    list.addAll(a.expand());
	});

	list.addAll(expandEmbeddables());

	return list;
    }

    public List<MetaAttribute> expandEmbeddables() {
	List<MetaAttribute> list = new ArrayList<>();

	embeddables.forEach(e -> {
	    list.addAll(e.expandAllAttributes());
	});

	return list;
    }

    public List<JoinColumnAttribute> expandJoinColumnAttributes() {
	List<JoinColumnAttribute> jcas = new ArrayList<>(joinColumnAttributes);
	for (MetaEntity metaEntity : embeddables) {
	    jcas.addAll(metaEntity.expandJoinColumnAttributes());
	}

	return jcas;
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

    @Override
    public String toString() {
	return super.toString() + " class: " + entityClass.getName() + "; tableName: " + tableName;
    }

    public List<MetaAttribute> notNullableAttributes() {
	return attributes.stream().filter(a -> !a.isNullable()).collect(Collectors.toList());
    }

    private void findEmbeddables(Set<MetaEntity> embeddableSet) {
	for (MetaEntity metaEntity : embeddables) {
	    embeddableSet.add(metaEntity);
	    metaEntity.findEmbeddables(embeddableSet);
	}
    }

    public Set<MetaEntity> findEmbeddables() {
	Set<MetaEntity> embeddableSet = new HashSet<>();
	findEmbeddables(embeddableSet);
	return embeddableSet;
    }

    public boolean hasVersionAttribute() {
	return attributes.stream().filter(a -> a.isVersion() && a.isBasic() && !a.isId()).findFirst().isPresent();
    }

    public Optional<MetaAttribute> getVersionAttribute() {
	return attributes.stream().filter(a -> a.isVersion() && a.isBasic() && !a.isId()).findFirst();
    }

    public static class Builder {

	private Class<?> entityClass;
	private String name;
	private String tableName;
	private String alias;
	private Pk id;
	private boolean embeddedId;
	private List<MetaAttribute> attributes;
	private List<MetaEntity> embeddables;
	private Method readMethod; // used for embeddables
	private Method writeMethod; // used for embeddables
	private String path; // used for embeddables
	private MetaEntity mappedSuperclassEntity;
	private Method modificationAttributeReadMethod;
	private Optional<Method> lazyLoadedAttributeReadMethod;
	private Optional<Method> lockTypeAttributeReadMethod;
	private Optional<Method> lockTypeAttributeWriteMethod;
	private Optional<Method> entityStatusAttributeReadMethod;
	private Optional<Method> entityStatusAttributeWriteMethod;

	public Builder withEntityClass(Class<?> entityClass) {
	    this.entityClass = entityClass;
	    return this;
	}

	public Builder withName(String name) {
	    this.name = name;
	    return this;
	}

	public Builder withTableName(String tableName) {
	    this.tableName = tableName;
	    return this;
	}

	public Builder withAlias(String alias) {
	    this.alias = alias;
	    return this;
	}

	public Builder withId(Pk id) {
	    this.id = id;
	    return this;
	}

	public Builder isEmbeddedId(boolean id) {
	    this.embeddedId = id;
	    return this;
	}

	public Builder withAttributes(List<MetaAttribute> attributes) {
	    this.attributes = attributes;
	    return this;
	}

	public Builder withEmbeddables(List<MetaEntity> embeddables) {
	    this.embeddables = embeddables;
	    return this;
	}

	public Builder withReadMethod(Method method) {
	    this.readMethod = method;
	    return this;
	}

	public Builder withWriteMethod(Method method) {
	    this.writeMethod = method;
	    return this;
	}

	public Builder withPath(String path) {
	    this.path = path;
	    return this;
	}

	public Builder withMappedSuperclassEntity(MetaEntity mappedSuperclassEntity) {
	    this.mappedSuperclassEntity = mappedSuperclassEntity;
	    return this;
	}

	public Builder withModificationAttributeReadMethod(Method modificationAttributeReadMethod) {
	    this.modificationAttributeReadMethod = modificationAttributeReadMethod;
	    return this;
	}

	public Builder withLazyLoadedAttributeReadMethod(Optional<Method> lazyLoadedAttributeReadMethod) {
	    this.lazyLoadedAttributeReadMethod = lazyLoadedAttributeReadMethod;
	    return this;
	}

	public Builder withLockTypeAttributeReadMethod(Optional<Method> lockTypeAttributeReadMethod) {
	    this.lockTypeAttributeReadMethod = lockTypeAttributeReadMethod;
	    return this;
	}

	public Builder withLockTypeAttributeWriteMethod(Optional<Method> lockTypeAttributeWriteMethod) {
	    this.lockTypeAttributeWriteMethod = lockTypeAttributeWriteMethod;
	    return this;
	}

	public Builder withEntityStatusAttributeReadMethod(Optional<Method> entityStatusAttributeReadMethod) {
	    this.entityStatusAttributeReadMethod = entityStatusAttributeReadMethod;
	    return this;
	}

	public Builder withEntityStatusAttributeWriteMethod(Optional<Method> entityStatusAttributeWriteMethod) {
	    this.entityStatusAttributeWriteMethod = entityStatusAttributeWriteMethod;
	    return this;
	}

	public MetaEntity build() {
	    MetaEntity metaEntity = new MetaEntity();
	    metaEntity.entityClass = entityClass;
	    metaEntity.name = name;
	    metaEntity.tableName = tableName;
	    metaEntity.alias = alias;
	    metaEntity.id = id;
	    metaEntity.embeddedId = embeddedId;
	    metaEntity.attributes = attributes;
	    metaEntity.embeddables = embeddables;
	    metaEntity.readMethod = readMethod;
	    metaEntity.writeMethod = writeMethod;
	    metaEntity.path = path;
	    metaEntity.mappedSuperclassEntity = mappedSuperclassEntity;
	    metaEntity.modificationAttributeReadMethod = modificationAttributeReadMethod;
	    metaEntity.lazyLoadedAttributeReadMethod = lazyLoadedAttributeReadMethod;
	    metaEntity.lockTypeAttributeReadMethod = lockTypeAttributeReadMethod;
	    metaEntity.lockTypeAttributeWriteMethod = lockTypeAttributeWriteMethod;
	    metaEntity.entityStatusAttributeReadMethod = entityStatusAttributeReadMethod;
	    metaEntity.entityStatusAttributeWriteMethod = entityStatusAttributeWriteMethod;
	    return metaEntity;
	}
    }
}
