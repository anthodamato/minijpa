package org.tinyjpa.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.AttrValue;
import org.tinyjpa.jdbc.Attribute;

public final class EntityDelegate implements EntityListener {
	protected Logger LOG = LoggerFactory.getLogger(EntityDelegate.class);

	private static EntityDelegate entityDelegate = new EntityDelegate();
	private Map<String, Entity> entities;
	/**
	 * (key, value) is (Entity, Map<entity instance, AttrValue>)
	 */
	private Map<Entity, Map<Object, List<AttrValue>>> changes = new HashMap<>();
	private List<Object> ignoreEntityInstances = new ArrayList<Object>();

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
		Map<Object, List<AttrValue>> map = changes.get(entity);
		if (map == null) {
			map = new HashMap<>();
			changes.put(entity, map);
		}

		List<AttrValue> instanceAttrs = map.get(entityInstance);
		if (instanceAttrs == null) {
			instanceAttrs = new ArrayList<>();
			map.put(entityInstance, instanceAttrs);
		}

		Attribute attribute = entity.getAttribute(attributeName);
//		LOG.info("set: attributeName=" + attributeName + "; attribute=" + attribute);
		Optional<AttrValue> optional = instanceAttrs.stream().filter(a -> a.getAttribute() == attribute).findFirst();
		if (optional.isPresent()) {
			AttrValue attrValue = optional.get();
			attrValue.setValue(value);
		} else {
			AttrValue attrValue = new AttrValue(attribute, value);
			instanceAttrs.add(attrValue);
		}
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

	public Map<Entity, Map<Object, List<AttrValue>>> getChanges() {
		return changes;
	}

	public void addIgnoreEntityInstance(Object object) {
		ignoreEntityInstances.add(object);
	}

	public void removeIgnoreEntityInstance(Object object) {
		ignoreEntityInstances.remove(object);
	}
}
