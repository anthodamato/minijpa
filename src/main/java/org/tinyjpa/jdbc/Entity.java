package org.tinyjpa.jdbc;

import java.util.ArrayList;
import java.util.List;

public class Entity {
	private Class<?> clazz;
	private String tableName;
	private Attribute id;
	private List<Attribute> attributes;

	public Entity(Class<?> clazz, String tableName, Attribute id, List<Attribute> attributes) {
		super();
		this.clazz = clazz;
		this.tableName = tableName;
		this.id = id;
		this.attributes = attributes;
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public String getTableName() {
		return tableName;
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
			list.addAll(a.expandAttribute());
		}

		return list;
	}

}
