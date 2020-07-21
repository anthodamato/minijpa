package org.tinyjpa.metadata;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.tinyjpa.jdbc.Attribute;
import org.tinyjpa.jdbc.Entity;

public class EntityContext {
	private Map<String, Entity> entities;

	public EntityContext(Map<String, Entity> entities) {
		super();
		this.entities = entities;
	}

	public Entity getEntity(String entityClassName) {
		return entities.get(entityClassName);
	}

	public Attribute findEmbeddedAttribute(String className) {
		for (Map.Entry<String, Entity> entry : entities.entrySet()) {
			Entity entity = entry.getValue();
			Attribute attribute = findEmbeddedAttribute(className, entity.getAttributes());
			if (attribute != null)
				return attribute;
		}

		return null;
	}

	private Attribute findEmbeddedAttribute(String className, List<Attribute> attributes) {
		for (Attribute attribute : attributes) {
			if (attribute.isEmbedded()) {
				if (attribute.getType().getName().equals(className)) {
					return attribute;
				}

				Attribute a = findEmbeddedAttribute(className, attribute.getEmbeddedAttributes());
				if (a != null)
					return a;
			}
		}

		return null;
	}

	public Set<Entity> getEntities() {
		return new HashSet<>(entities.values());
	}
}
