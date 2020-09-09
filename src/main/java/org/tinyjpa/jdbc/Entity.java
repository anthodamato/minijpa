package org.tinyjpa.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Entity {
	private Class<?> clazz;
	private String tableName;
	private String alias;
	private Attribute id;
	private List<Attribute> attributes;
	private List<JoinColumnAttribute> joinColumnAttributes = new ArrayList<>();
	// used to build the metamodel. The 'attributes' field contains the
	// MappedSuperclass attributes
	private Entity mappedSuperclassEntity;

	public Entity(Class<?> clazz, String tableName, String alias, Attribute id, List<Attribute> attributes,
			Entity mappedSuperclassEntity) {
		super();
		this.clazz = clazz;
		this.tableName = tableName;
		this.alias = alias;
		this.id = id;
		this.attributes = attributes;
		this.mappedSuperclassEntity = mappedSuperclassEntity;
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

	public List<Attribute> getAttributes() {
		return attributes;
	}

	public Attribute getAttribute(String name) {
		for (Attribute attribute : attributes) {
			if (attribute.getName().equals(name))
				return attribute;
		}

		if (id.getName().equals(name))
			return id;

		return null;
	}

	public Attribute getId() {
		return id;
	}

	public List<Attribute> expandAttributes() {
		List<Attribute> list = new ArrayList<>();
		for (Attribute a : attributes) {
			list.addAll(a.expand());
		}

		return list;
	}

	public Attribute findAttributeWithMappedBy(String mappedBy) {
		for (Attribute attribute : attributes) {
			if (attribute.getRelationship() != null && mappedBy.equals(attribute.getRelationship().getMappedBy()))
				return attribute;
		}

		return null;
	}

	public List<JoinColumnAttribute> getJoinColumnAttributes() {
		return joinColumnAttributes;
	}

	public List<Attribute> getRelationshipAttributes() {
		return attributes.stream().filter(a -> a.getRelationship() != null).collect(Collectors.toList());
	}

	public Entity getMappedSuperclassEntity() {
		return mappedSuperclassEntity;
	}

	@Override
	public String toString() {
		return getClass().getName() + "@ Class: " + clazz.getName() + "; tableName: " + tableName;
	}

}
