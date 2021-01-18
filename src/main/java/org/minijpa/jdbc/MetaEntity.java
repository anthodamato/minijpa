package org.minijpa.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MetaEntity {
	private Class<?> entityClass;
	private String name;
	private String tableName;
	private String alias;
	private MetaAttribute id;
	private List<MetaAttribute> attributes;
	private List<JoinColumnAttribute> joinColumnAttributes = new ArrayList<>();
	// used to build the metamodel. The 'attributes' field contains the
	// MappedSuperclass attributes
	private MetaEntity mappedSuperclassEntity;
	// used to build the metamodel. The 'attributes' field contains the
	// Embeddable attributes
	private List<MetaEntity> embeddables;

	public MetaEntity(Class<?> entityClass, String name, String tableName, String alias, MetaAttribute id,
			List<MetaAttribute> attributes, MetaEntity mappedSuperclassEntity, List<MetaEntity> embeddables) {
		super();
		this.entityClass = entityClass;
		this.name = name;
		this.tableName = tableName;
		this.alias = alias;
		this.id = id;
		this.attributes = attributes;
		this.mappedSuperclassEntity = mappedSuperclassEntity;
		this.embeddables = embeddables;
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

	public List<MetaEntity> getEmbeddables() {
		return embeddables;
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

	public MetaAttribute findAttributeWithMappedBy(String mappedBy) {
		for (MetaAttribute attribute : attributes) {
			if (attribute.getRelationship() != null && mappedBy.equals(attribute.getRelationship().getMappedBy()))
				return attribute;
		}

		return null;
	}

	public List<JoinColumnAttribute> getJoinColumnAttributes() {
		return joinColumnAttributes;
	}

	public List<MetaAttribute> getRelationshipAttributes() {
		return attributes.stream().filter(a -> a.getRelationship() != null).collect(Collectors.toList());
	}

	public MetaEntity getMappedSuperclassEntity() {
		return mappedSuperclassEntity;
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

}
