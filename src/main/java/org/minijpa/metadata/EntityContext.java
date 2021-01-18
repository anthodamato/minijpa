package org.minijpa.metadata;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;

public class EntityContext {
	private String persistenceUnitName;
	private Map<String, MetaEntity> entities;

	public EntityContext(String persistenceUnitName, Map<String, MetaEntity> entities) {
		super();
		this.persistenceUnitName = persistenceUnitName;
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

	public String getPersistenceUnitName() {
		return persistenceUnitName;
	}

	public Set<MetaEntity> getMetaEntities() {
		return new HashSet<>(entities.values());
	}

	public Map<String, MetaEntity> getEntities() {
		return entities;
	}

}
