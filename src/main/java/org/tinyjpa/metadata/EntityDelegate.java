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

public class EntityDelegate implements EntityListener {
	private Logger LOG = LoggerFactory.getLogger(EntityDelegate.class);

	private static EntityDelegate entityDelegate = new EntityDelegate();
	private Map<String, Entity> entities;
	/**
	 * (key, value) is (Entity, Map<entity instance, AttrValue>)
	 */
	private Map<Entity, Map<Object, List<AttrValue>>> changes = new HashMap<>();
	private boolean ignoreChanges = false;

	public static EntityDelegate getInstance() {
		return entityDelegate;
	}

	@Override
	public Object get(Object value, String attributeName, Object entityInstance) {
		return value;
	}

	@Override
	public void set(Object value, String attributeName, Object entityInstance) {
		if (ignoreChanges)
			return;

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

	public void setEntities(Map<String, Entity> entities) {
		this.entities = entities;
	}

	public Map<Entity, Map<Object, List<AttrValue>>> getChanges() {
		return changes;
	}

	public void setIgnoreChanges(boolean ignoreChanges) {
		this.ignoreChanges = ignoreChanges;
	}

}
