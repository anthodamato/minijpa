package org.tinyjpa.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MetaEntity {
	private Class<?> clazz;
	private String tableName;
	private String alias;
	private MetaAttribute id;
	private List<MetaAttribute> attributes;
	private List<JoinColumnAttribute> joinColumnAttributes = new ArrayList<>();
	// used to build the metamodel. The 'attributes' field contains the
	// MappedSuperclass attributes
	private MetaEntity mappedSuperclassEntity;
	private List<MetaEntity> embeddables;

	public MetaEntity(Class<?> clazz, String tableName, String alias, MetaAttribute id, List<MetaAttribute> attributes,
			MetaEntity mappedSuperclassEntity, List<MetaEntity> embeddables) {
		super();
		this.clazz = clazz;
		this.tableName = tableName;
		this.alias = alias;
		this.id = id;
		this.attributes = attributes;
		this.mappedSuperclassEntity = mappedSuperclassEntity;
		this.embeddables = embeddables;
	}

	public Class<?> getClazz() {
		return clazz;
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

	@Override
	public String toString() {
		return getClass().getName() + "@ Class: " + clazz.getName() + "; tableName: " + tableName;
	}

}
