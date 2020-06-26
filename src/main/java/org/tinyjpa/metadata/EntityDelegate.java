package org.tinyjpa.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.spi.PersistenceUnitInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.Attribute;
import org.tinyjpa.jdbc.AttributeValue;
import org.tinyjpa.jdbc.Entity;
import org.tinyjpa.jdbc.db.AttributeLoader;

public final class EntityDelegate implements EntityListener {
	protected Logger LOG = LoggerFactory.getLogger(EntityDelegate.class);

	private static EntityDelegate entityDelegate = new EntityDelegate();
	private Map<String, Entity> entities;
	/**
	 * (key, value) is (Entity, Map<entity instance, List<AttrValue>>>)
	 * 
	 * Collects entity attributes changes.
	 */
	private Map<Entity, Map<Object, List<AttributeValue>>> changes = new HashMap<>();
	/**
	 * (key, value) is (Map<embedded instance, List<AttrValue>>>)
	 * 
	 * Collects embedded attributes changes.
	 */
	private Map<Object, List<AttributeValue>> embeddedChanges = new HashMap<>();
	private List<Object> ignoreEntityInstances = new ArrayList<Object>();

	private Map<PersistenceUnitInfo, AttributeLoader> attributeLoaders = new HashMap<>();

	public static EntityDelegate getInstance() {
		return entityDelegate;
	}

	@Override
	public void set(Object value, String attributeName, Object entityInstance) {
		for (Object object : ignoreEntityInstances) {
			if (object == entityInstance)
				return;
		}

		Entity entity = entities.get(entityInstance.getClass().getName());
		LOG.info("set: entityInstance=" + entityInstance);
		if (entity == null) {
			// it's an embedded attribute
			List<AttributeValue> instanceAttrs = embeddedChanges.get(entityInstance);
			if (instanceAttrs == null) {
				instanceAttrs = new ArrayList<>();
				embeddedChanges.put(entityInstance, instanceAttrs);
			}

			Attribute parentAttribute = findEmbeddedAttribute(entityInstance.getClass().getName());
			Attribute attribute = parentAttribute.findChildByName(attributeName);
			Optional<AttributeValue> optional = instanceAttrs.stream().filter(a -> a.getAttribute() == attribute)
					.findFirst();
			if (optional.isPresent()) {
				AttributeValue attrValue = optional.get();
				attrValue.setValue(value);
			} else {
				AttributeValue attrValue = new AttributeValue(attribute, value);
				instanceAttrs.add(attrValue);
			}

			return;
		}

		Map<Object, List<AttributeValue>> map = changes.get(entity);
		if (map == null) {
			map = new HashMap<>();
			changes.put(entity, map);
		}

		List<AttributeValue> instanceAttrs = map.get(entityInstance);
		if (instanceAttrs == null) {
			instanceAttrs = new ArrayList<>();
			map.put(entityInstance, instanceAttrs);
		}

		Attribute attribute = entity.getAttribute(attributeName);
//		LOG.info("set: attributeName=" + attributeName + "; attribute=" + attribute);
		Optional<AttributeValue> optional = instanceAttrs.stream().filter(a -> a.getAttribute() == attribute)
				.findFirst();
		if (optional.isPresent()) {
			AttributeValue attrValue = optional.get();
			attrValue.setValue(value);
		} else {
			AttributeValue attrValue = new AttributeValue(attribute, value);
			instanceAttrs.add(attrValue);
		}
	}

	private Attribute findEmbeddedAttribute(String className) {
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

	public Optional<List<AttributeValue>> findEmbeddedAttrValues(Object embeddedInstance) {
		for (Map.Entry<Object, List<AttributeValue>> entry : embeddedChanges.entrySet()) {
			LOG.info("findEmbeddedAttrValues: entry.getKey()=" + entry.getKey());
		}

		List<AttributeValue> attrValues = embeddedChanges.get(embeddedInstance);
		if (attrValues == null)
			return Optional.empty();

		return Optional.of(attrValues);
	}

	public void removeChanges(Object entityInstance) {
		Entity entity = entities.get(entityInstance.getClass().getName());
		if (entity == null)
			return;

		removeEmbeddedChanges(entity.getAttributes(), entityInstance);
		Map<Object, List<AttributeValue>> map = changes.get(entity);
		if (map == null)
			return;

		map.remove(entityInstance);
	}

	private void removeEmbeddedChanges(List<Attribute> attributes, Object entityInstance) {
		for (Attribute attribute : attributes) {
			if (attribute.isId() || !attribute.isEmbedded())
				continue;

			Object value = null;
			try {
				value = attribute.getReadMethod().invoke(entityInstance);
			} catch (Exception e) {
				LOG.error(e.getMessage());
				continue;
			}

			embeddedChanges.remove(value);
			removeEmbeddedChanges(attribute.getEmbeddedAttributes(), value);
		}
	}

	public Optional<List<AttributeValue>> getChanges(Entity entity, Object entityInstance) {
		Map<Object, List<AttributeValue>> map = changes.get(entity);
		if (map == null)
			return Optional.empty();

		List<AttributeValue> instanceAttrs = map.get(entityInstance);
		if (instanceAttrs == null)
			return Optional.empty();

		return Optional.of(instanceAttrs);
	}

	@Override
	public void set(byte value, String attributeName, Object entityInstance) {
		set(new Byte(value), attributeName, entityInstance);
	}

	@Override
	public void set(short value, String attributeName, Object entityInstance) {
		set(new Short(value), attributeName, entityInstance);
	}

	@Override
	public void set(int value, String attributeName, Object entityInstance) {
		set(new Integer(value), attributeName, entityInstance);
	}

	@Override
	public void set(long value, String attributeName, Object entityInstance) {
		set(new Long(value), attributeName, entityInstance);
	}

	@Override
	public void set(float value, String attributeName, Object entityInstance) {
		set(new Float(value), attributeName, entityInstance);
	}

	@Override
	public void set(double value, String attributeName, Object entityInstance) {
		set(new Double(value), attributeName, entityInstance);
	}

	@Override
	public void set(char value, String attributeName, Object entityInstance) {
		set(new Character(value), attributeName, entityInstance);
	}

	@Override
	public void set(boolean value, String attributeName, Object entityInstance) {
		set(new Boolean(value), attributeName, entityInstance);
	}

	@Override
	public Object get(Object value, String attributeName, Object entityInstance) {
		if (value != null)
			return value;

		Entity entity = entities.get(entityInstance.getClass().getName());
		Attribute a = entity.getAttribute(attributeName);
		LOG.info("get: a=" + a + "; a.isLazy()=" + a.isLazy() + "; entityInstance=" + entityInstance);
		if (a.isLazy()) {
			AttributeLoader attributeLoader = findAttributeLoader(entityInstance);
			if (attributeLoader != null) {
				try {
					value = attributeLoader.load(entityInstance, a);
				} catch (Exception e) {
					LOG.error(e.getMessage());
				}
			}
		}

		return value;
	}

	@Override
	public byte get(byte value, String attributeName, Object entityInstance) {
		return value;
	}

	@Override
	public short get(short value, String attributeName, Object entityInstance) {
		return value;
	}

	@Override
	public int get(int value, String attributeName, Object entityInstance) {
		return value;
	}

	@Override
	public long get(long value, String attributeName, Object entityInstance) {
		return value;
	}

	@Override
	public float get(float value, String attributeName, Object entityInstance) {
		return value;
	}

	@Override
	public double get(double value, String attributeName, Object entityInstance) {
		return value;
	}

	@Override
	public char get(char value, String attributeName, Object entityInstance) {
		return value;
	}

	@Override
	public boolean get(boolean value, String attributeName, Object entityInstance) {
		return value;
	}

	public void setEntities(Map<String, Entity> entities) {
		this.entities = entities;
	}

	public Map<Entity, Map<Object, List<AttributeValue>>> getChanges() {
		return changes;
	}

	public void addIgnoreEntityInstance(Object object) {
		ignoreEntityInstances.add(object);
	}

	public void removeIgnoreEntityInstance(Object object) {
		ignoreEntityInstances.remove(object);
	}

	public void addAttributeLoader(PersistenceUnitInfo persistenceUnitInfo, AttributeLoader attributeLoader) {
		attributeLoaders.put(persistenceUnitInfo, attributeLoader);
	}

	private AttributeLoader findAttributeLoader(Object entityInstance) {
		if (attributeLoaders.isEmpty())
			return null;

		if (attributeLoaders.size() == 1) {
			for (Map.Entry<PersistenceUnitInfo, AttributeLoader> entry : attributeLoaders.entrySet()) {
				return entry.getValue();
			}
		}

		for (Map.Entry<PersistenceUnitInfo, AttributeLoader> entry : attributeLoaders.entrySet()) {
			PersistenceUnitInfo persistenceUnitInfo = entry.getKey();
			if (persistenceUnitInfo.getManagedClassNames().contains(entityInstance.getClass().getName()))
				return entry.getValue();
		}

		return null;
	}
}
