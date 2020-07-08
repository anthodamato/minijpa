package org.tinyjpa.metadata;

import java.util.Map;

import org.tinyjpa.jdbc.Entity;
import org.tinyjpa.jdbc.db.AttributeLoader;
import org.tinyjpa.jdbc.db.EntityContainer;

public class EntityContainerContext {
	private Map<String, Entity> entities;
	private EntityContainer entityContainer;
	private AttributeLoader attributeLoader;

	public EntityContainerContext(Map<String, Entity> entities, EntityContainer entityContainer,
			AttributeLoader attributeLoader) {
		super();
		this.entities = entities;
		this.entityContainer = entityContainer;
		this.attributeLoader = attributeLoader;
	}

	public Map<String, Entity> getEntities() {
		return entities;
	}

	public EntityContainer getEntityContainer() {
		return entityContainer;
	}

	public AttributeLoader getAttributeLoader() {
		return attributeLoader;
	}

	public Entity getEntity(String entityClassName) {
		return entities.get(entityClassName);
	}

}
