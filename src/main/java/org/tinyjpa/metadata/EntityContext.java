package org.tinyjpa.metadata;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.tinyjpa.jdbc.MetaAttribute;
import org.tinyjpa.jdbc.MetaEntity;

public class EntityContext {
	private Map<String, MetaEntity> entities;

	public EntityContext(Map<String, MetaEntity> entities) {
		super();
		this.entities = entities;
	}

	public MetaEntity getEntity(String entityClassName) {
		return entities.get(entityClassName);
	}

	public MetaAttribute findEmbeddedAttribute(String className) {
		for (Map.Entry<String, MetaEntity> entry : entities.entrySet()) {
			MetaEntity entity = entry.getValue();
			MetaAttribute attribute = findEmbeddedAttribute(className, entity.getAttributes());
			if (attribute != null)
				return attribute;
		}

		return null;
	}

	private MetaAttribute findEmbeddedAttribute(String className, List<MetaAttribute> attributes) {
		for (MetaAttribute attribute : attributes) {
			if (attribute.isEmbedded()) {
				if (attribute.getType().getName().equals(className)) {
					return attribute;
				}

				MetaAttribute a = findEmbeddedAttribute(className, attribute.getEmbeddedAttributes());
				if (a != null)
					return a;
			}
		}

		return null;
	}

	public Set<MetaEntity> getEntities() {
		return new HashSet<>(entities.values());
	}
}
