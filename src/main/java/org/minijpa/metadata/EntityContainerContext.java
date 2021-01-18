package org.minijpa.metadata;

import java.util.Map;

import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.db.AttributeLoader;
import org.minijpa.jdbc.db.EntityContainer;

public class EntityContainerContext {
	private Map<String, MetaEntity> entities;
	private EntityContainer entityContainer;
	private AttributeLoader attributeLoader;

	public EntityContainerContext(Map<String, MetaEntity> entities, EntityContainer entityContainer,
			AttributeLoader attributeLoader) {
		super();
		this.entities = entities;
		this.entityContainer = entityContainer;
		this.attributeLoader = attributeLoader;
	}

	public Map<String, MetaEntity> getEntities() {
		return entities;
	}

	public EntityContainer getEntityContainer() {
		return entityContainer;
	}

	public AttributeLoader getAttributeLoader() {
		return attributeLoader;
	}

	public MetaEntity getEntity(String entityClassName) {
		return entities.get(entityClassName);
	}

}
