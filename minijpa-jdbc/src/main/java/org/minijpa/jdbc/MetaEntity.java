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
package org.minijpa.jdbc;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.minijpa.jdbc.relationship.JoinColumnMapping;
import org.minijpa.jdbc.relationship.Relationship;

public class MetaEntity {

	private Class<?> entityClass;
	private String name;
	private String tableName;
	private Pk id;
	/**
	 * Collection of basic and relationship attributes.
	 */
	private List<MetaAttribute> attributes;
	/**
	 * Basic attributes
	 */
	private List<MetaAttribute> basicAttributes = Collections.unmodifiableList(Collections.emptyList());
	private List<MetaAttribute> relationshipAttributes = Collections.emptyList();
	private List<MetaEntity> embeddables = Collections.emptyList();
	private Method readMethod; // used for embeddables
	private Method writeMethod; // used for embeddables
	private String path; // used for embeddables
	private boolean embeddedId;
	private final List<JoinColumnMapping> joinColumnMappings = new ArrayList<>();
	// used to build the metamodel. The 'attributes' field contains the
	// MappedSuperclass attributes
	private MetaEntity mappedSuperclassEntity;
	private Method modificationAttributeReadMethod;
	private Optional<Method> lazyLoadedAttributeReadMethod = Optional.empty();
	private Optional<Method> lockTypeAttributeReadMethod = Optional.empty();
	private Optional<Method> lockTypeAttributeWriteMethod = Optional.empty();
	private Optional<Method> entityStatusAttributeReadMethod = Optional.empty();
	private Optional<Method> entityStatusAttributeWriteMethod = Optional.empty();
	private Optional<Method> joinColumnPostponedUpdateAttributeReadMethod = Optional.empty();

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

	public Pk getId() {
		return id;
	}

	public boolean isEmbeddedId() {
		return embeddedId;
	}

	public List<MetaAttribute> getAttributes() {
		return attributes;
	}

	public List<MetaAttribute> getBasicAttributes() {
		return basicAttributes;
	}

	public List<MetaAttribute> getRelationshipAttributes() {
		return relationshipAttributes;
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

	public List<JoinColumnMapping> getJoinColumnMappings() {
		return joinColumnMappings;
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

	public Optional<Method> getJoinColumnPostponedUpdateAttributeReadMethod() {
		return joinColumnPostponedUpdateAttributeReadMethod;
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

	public List<MetaAttribute> expandAllAttributes() {
		List<MetaAttribute> list = new ArrayList<>();
		if (id != null)
			list.addAll(id.getAttributes());

		basicAttributes.forEach(a -> {
			list.add(a);
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
		List<JoinColumnAttribute> jcas = new ArrayList<>();
		joinColumnMappings.forEach(joinColumnMapping -> {
			for (int i = 0; i < joinColumnMapping.size(); ++i) {
				jcas.add(joinColumnMapping.get(i));
			}
		});

		embeddables.forEach(metaEntity -> {
			jcas.addAll(metaEntity.expandJoinColumnAttributes());
		});

		return jcas;
	}

	public Optional<MetaAttribute> findJoinColumnMappingAttribute(String attributeName) {
		Optional<JoinColumnMapping> o = joinColumnMappings.stream()
				.filter(j -> j.getAttribute().getName().equals(attributeName)).findFirst();
		if (o.isPresent())
			return Optional.of(o.get().getAttribute());

		for (MetaEntity embeddable : embeddables) {
			Optional<MetaAttribute> optional = embeddable.findJoinColumnMappingAttribute(attributeName);
			if (optional.isPresent())
				return Optional.of(optional.get());
		}

		return Optional.empty();
	}

	public List<JoinColumnMapping> expandJoinColumnMappings() {
		List<JoinColumnMapping> jcms = new ArrayList<>();
		jcms.addAll(joinColumnMappings);

		embeddables.forEach(metaEntity -> {
			jcms.addAll(metaEntity.expandJoinColumnMappings());
		});

		return jcms;
	}

	public List<MetaAttribute> expandRelationshipAttributes() {
		List<MetaAttribute> list = new ArrayList<>();
		list.addAll(relationshipAttributes);
		embeddables.forEach(e -> {
			list.addAll(e.expandRelationshipAttributes());
		});

		return list;
	}

	public MetaAttribute findAttributeByMappedBy(String mappedBy) {
		for (MetaAttribute attribute : relationshipAttributes) {
			if (mappedBy.equals(attribute.getRelationship().getMappedBy().get()))
				return attribute;
		}

		return null;
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
		return basicAttributes.stream().filter(a -> a.isVersion() && !a.isId()).findFirst().isPresent();
	}

	public Optional<MetaAttribute> getVersionAttribute() {
		return basicAttributes.stream().filter(a -> a.isVersion() && !a.isId()).findFirst();
	}

	public List<MetaAttribute> getCascadeAttributes(Cascade... cascades) {
		List<MetaAttribute> attrs = new ArrayList<>();
		getRelationshipAttributes().forEach(attribute -> {
			Relationship r = attribute.getRelationship();
			if (r.isOwner() && r.hasAnyCascades(cascades))
				attrs.add(attribute);
		});

		return attrs;
	}

	public static class Builder {

		private Class<?> entityClass;
		private String name;
		private String tableName;
		private Pk id;
		private boolean embeddedId;
		private List<MetaAttribute> attributes;
		private List<MetaAttribute> basicAttributes;
		private List<MetaAttribute> relationshipAttributes;
		private List<MetaEntity> embeddables;
		private Method readMethod; // used for embeddables
		private Method writeMethod; // used for embeddables
		private String path; // used for embeddables
		private MetaEntity mappedSuperclassEntity;
		private Method modificationAttributeReadMethod;
		private Optional<Method> lazyLoadedAttributeReadMethod = Optional.empty();
		private Optional<Method> lockTypeAttributeReadMethod = Optional.empty();
		private Optional<Method> lockTypeAttributeWriteMethod = Optional.empty();
		private Optional<Method> entityStatusAttributeReadMethod = Optional.empty();
		private Optional<Method> entityStatusAttributeWriteMethod = Optional.empty();
		private Optional<Method> joinColumnPostponedUpdateAttributeReadMethod = Optional.empty();

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

		public Builder withBasicAttributes(List<MetaAttribute> attributes) {
			this.basicAttributes = attributes;
			return this;
		}

		public Builder withRelationshipAttributes(List<MetaAttribute> attributes) {
			this.relationshipAttributes = attributes;
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

		public Builder withJoinColumnPostponedUpdateAttributeReadMethod(
				Optional<Method> joinColumnPostponedUpdateAttributeReadMethod) {
			this.joinColumnPostponedUpdateAttributeReadMethod = joinColumnPostponedUpdateAttributeReadMethod;
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
			metaEntity.id = id;
			metaEntity.embeddedId = embeddedId;
			metaEntity.attributes = attributes;
			metaEntity.basicAttributes = basicAttributes;
			metaEntity.relationshipAttributes = relationshipAttributes;
			metaEntity.embeddables = embeddables;
			metaEntity.readMethod = readMethod;
			metaEntity.writeMethod = writeMethod;
			metaEntity.path = path;
			metaEntity.mappedSuperclassEntity = mappedSuperclassEntity;
			metaEntity.modificationAttributeReadMethod = modificationAttributeReadMethod;
			metaEntity.lazyLoadedAttributeReadMethod = lazyLoadedAttributeReadMethod;
			metaEntity.joinColumnPostponedUpdateAttributeReadMethod = joinColumnPostponedUpdateAttributeReadMethod;
			metaEntity.lockTypeAttributeReadMethod = lockTypeAttributeReadMethod;
			metaEntity.lockTypeAttributeWriteMethod = lockTypeAttributeWriteMethod;
			metaEntity.entityStatusAttributeReadMethod = entityStatusAttributeReadMethod;
			metaEntity.entityStatusAttributeWriteMethod = entityStatusAttributeWriteMethod;
			return metaEntity;
		}
	}
}
