package org.tinyjpa.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.AttributeValue;
import org.tinyjpa.jdbc.MetaAttribute;
import org.tinyjpa.jdbc.MetaEntity;
import org.tinyjpa.jdbc.db.AttributeLoader;

public final class EntityDelegate implements EntityListener {
	protected Logger LOG = LoggerFactory.getLogger(EntityDelegate.class);

	private static EntityDelegate entityDelegate = new EntityDelegate();

	private EntityContextManager entityContextManager = new EntityContextManager();
	private EntityContainerContextManager entityContainerContextManager = new EntityContainerContextManager();

	/**
	 * (key, value) is (Entity, Map<entity instance, List<AttrValue>>>)
	 * 
	 * Collects entity attributes changes.
	 */
	private Map<MetaEntity, Map<Object, List<AttributeValue>>> changes = new HashMap<>();
	/**
	 * (key, value) is (Map<embedded instance, List<AttrValue>>>)
	 * 
	 * Collects embedded attributes changes.
	 */
	private Map<Object, List<AttributeValue>> embeddedChanges = new HashMap<>();
	private List<Object> ignoreEntityInstances = new ArrayList<Object>();

	/**
	 * The loaded lazy attributes. <br>
	 * (key, value) is (Map<owner entity instance class, Map<owner entity instance,
	 * Set<Attribute>>>)
	 */
	private Map<Class<?>, Map<Object, Set<MetaAttribute>>> loadedLazyAttributes = new HashMap<>();

	public static EntityDelegate getInstance() {
		return entityDelegate;
	}

	/**
	 * This method is called just one time to setup the data structures. If the
	 * related data structures are created dynamically and multiple entity managers
	 * are used race conditions may occur.
	 * 
	 * @param entities
	 */
	private synchronized void initDataStructures(Set<MetaEntity> entities) {
		for (MetaEntity entity : entities) {
			Map<Object, List<AttributeValue>> map = changes.get(entity);
			if (map == null) {
				map = new HashMap<>();
				changes.put(entity, map);
				LOG.info("initDataStructures: entity=" + entity);
			}
		}

		for (MetaEntity entity : entities) {
			Map<Object, Set<MetaAttribute>> map = loadedLazyAttributes.get(entity.getEntityClass());
			if (map == null) {
				map = new HashMap<>();
				loadedLazyAttributes.put(entity.getEntityClass(), map);
			}
		}
	}

	@Override
	public void set(Object value, String attributeName, Object entityInstance) {
		for (Object object : ignoreEntityInstances) {
			if (object == entityInstance)
				return;
		}

		MetaEntity entity = entityContextManager.getEntity(entityInstance.getClass().getName());
		LOG.info("set: this=" + this + "; getClass().getClassLoader()=" + getClass().getClassLoader()
				+ "; Thread.currentThread()=" + Thread.currentThread());
		LOG.info("set: entityInstance=" + entityInstance + "; attributeName=" + attributeName + "; value=" + value);
		if (entity == null) {
			// it's an embedded attribute
			List<AttributeValue> instanceAttrs = embeddedChanges.get(entityInstance);
			if (instanceAttrs == null) {
				instanceAttrs = new ArrayList<>();
				embeddedChanges.put(entityInstance, instanceAttrs);
			}

			MetaAttribute parentAttribute = entityContextManager
					.findEmbeddedAttribute(entityInstance.getClass().getName());
			MetaAttribute attribute = parentAttribute.findChildByName(attributeName);
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
		List<AttributeValue> instanceAttrs = map.get(entityInstance);
		if (instanceAttrs == null) {
			instanceAttrs = new ArrayList<>();
			map.put(entityInstance, instanceAttrs);
		}

		LOG.info("set: instanceAttrs=" + instanceAttrs + "; entityInstance=" + entityInstance + "; entity=" + entity
				+ "; map=" + map);
		MetaAttribute attribute = entity.getAttribute(attributeName);
		LOG.info("set: attributeName=" + attributeName + "; attribute=" + attribute);
		Optional<AttributeValue> optional = instanceAttrs.stream().filter(a -> a.getAttribute() == attribute)
				.findFirst();
		LOG.info("set: optional.isPresent()=" + optional.isPresent());
		if (optional.isPresent()) {
			AttributeValue attrValue = optional.get();
			attrValue.setValue(value);
		} else {
			AttributeValue attrValue = new AttributeValue(attribute, value);
			instanceAttrs.add(attrValue);
			LOG.info("set: attrValue=" + attrValue + "; instanceAttrs=" + instanceAttrs);
		}
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
		MetaEntity entity = entityContextManager.getEntity(entityInstance.getClass().getName());
		if (entity == null)
			return;

		removeEmbeddedChanges(entity.getAttributes(), entityInstance);
		Map<Object, List<AttributeValue>> map = changes.get(entity);
		LOG.info("removeChanges: map=" + map);
//		if (map == null)
//			return;

		map.remove(entityInstance);
	}

	private void removeEmbeddedChanges(List<MetaAttribute> attributes, Object entityInstance) {
		for (MetaAttribute attribute : attributes) {
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

	public Optional<List<AttributeValue>> getChanges(MetaEntity entity, Object entityInstance) {
		LOG.info("getChanges: this=" + this + "; getClass().getClassLoader()=" + getClass().getClassLoader()
				+ "; Thread.currentThread()=" + Thread.currentThread());
		Map<Object, List<AttributeValue>> map = changes.get(entity);
		List<AttributeValue> instanceAttrs = map.get(entityInstance);
		LOG.info("getChanges: instanceAttrs=" + instanceAttrs + "; entityInstance=" + entityInstance + "; entity="
				+ entity + "; map=" + map);
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
		LOG.info("get: entityInstance=" + entityInstance + "; attributeName=" + attributeName);
		if (entityContainerContextManager.isEmpty() || !entityContainerContextManager.isLoadedFromDb(entityInstance))
			return value;

		MetaEntity entity = entityContextManager.getEntity(entityInstance.getClass().getName());
		MetaAttribute a = entity.getAttribute(attributeName);
		LOG.info("get: a=" + a + "; a.isLazy()=" + a.isLazy());
		if (a.isLazy() && !isLazyAttributeLoaded(entityInstance, a)) {
			AttributeLoader attributeLoader = entityContainerContextManager
					.findByEntity(entityInstance.getClass().getName());
			try {
				value = attributeLoader.load(entityInstance, a);
				setLazyAttributeLoaded(entityInstance, a);
			} catch (Exception e) {
				LOG.error(e.getMessage());
				throw new IllegalArgumentException(e.getMessage());
			}
		}

		return value;
	}

	private boolean isLazyAttributeLoaded(Object entityInstance, MetaAttribute a) {
		Map<Object, Set<MetaAttribute>> map = loadedLazyAttributes.get(entityInstance.getClass());
//		if (map == null)
//			return false;

		Set<MetaAttribute> attributes = map.get(entityInstance);
		if (attributes == null)
			return false;

		return attributes.contains(a);
	}

	public void setLazyAttributeLoaded(Object entityInstance, MetaAttribute a) {
		Map<Object, Set<MetaAttribute>> map = loadedLazyAttributes.get(entityInstance.getClass());
//		if (map == null) {
//			map = new HashMap<>();
//			loadedLazyAttributes.put(entityInstance.getClass(), map);
//		}

		Set<MetaAttribute> attributes = map.get(entityInstance);
		if (attributes == null) {
			attributes = new HashSet<>();
			map.put(entityInstance, attributes);
		}

		attributes.add(a);
	}

	public void removeLazyAttributeLoaded(Object entityInstance, MetaAttribute a) {
		Map<Object, Set<MetaAttribute>> map = loadedLazyAttributes.get(entityInstance.getClass());
//		if (map == null)
//			return;

		Set<MetaAttribute> attributes = map.get(entityInstance);
		if (attributes == null)
			return;

		attributes.remove(a);
		if (attributes.isEmpty()) {
			map.remove(entityInstance);
//			if (map.isEmpty())
//				loadedLazyAttributes.remove(entityInstance.getClass());
		}
	}

	public Map<MetaEntity, Map<Object, List<AttributeValue>>> getChanges() {
		return changes;
	}

	public void addIgnoreEntityInstance(Object object) {
		ignoreEntityInstances.add(object);
	}

	public void removeIgnoreEntityInstance(Object object) {
		ignoreEntityInstances.remove(object);
	}

//	private boolean isNewInstance(Object entityInstance) throws Exception {
//		if (entities == null)
//			return true;
//
//		Entity entity = entities.get(entityInstance.getClass().getName());
//		Object pk = AttributeUtil.getIdValue(entity, entityInstance);
//		if (pk == null)
//			return true;
//
//		return !entityContainer.isSaved(entityInstance);
//	}

	private class EntityContextManager {
		private List<EntityContext> entityContexts = new ArrayList<>();

		public void add(EntityContext entityContext) {
			entityContexts.add(entityContext);
		}

		public MetaEntity getEntity(String entityClassName) {
			for (EntityContext entityContext : entityContexts) {
				MetaEntity entity = entityContext.getEntity(entityClassName);
				if (entity != null)
					return entity;
			}

			return null;
		}

		public MetaAttribute findEmbeddedAttribute(String className) {
			for (EntityContext entityContext : entityContexts) {
				MetaAttribute attribute = entityContext.findEmbeddedAttribute(className);
				if (attribute != null)
					return attribute;
			}

			return null;
		}

		public Optional<EntityContext> getEntityContext(String persistenceUnitName) {
			return entityContexts.stream().filter(e -> e.getPersistenceUnitName().equals(persistenceUnitName))
					.findFirst();
		}
	}

	public void addEntityContext(EntityContext entityContext) {
		entityContextManager.add(entityContext);
		initDataStructures(entityContext.getMetaEntities());
	}

	public Optional<MetaEntity> getMetaEntity(String className) {
		MetaEntity metaEntity = entityContextManager.getEntity(className);
		if (metaEntity == null)
			return Optional.empty();

		return Optional.of(metaEntity);
	}

	public Optional<EntityContext> getEntityContext(String persistenceUnitName) {
		return entityContextManager.getEntityContext(persistenceUnitName);
	}

	private class EntityContainerContextManager {
		private List<EntityContainerContext> entityContainerContexts = new ArrayList<>();

		public void add(EntityContainerContext entityManagerContext) {
			entityContainerContexts.add(entityManagerContext);
		}

		public AttributeLoader findByEntity(String className) {
			for (EntityContainerContext entityContainerContext : entityContainerContexts) {
				MetaEntity entity = entityContainerContext.getEntity(className);
				if (entity != null)
					return entityContainerContext.getAttributeLoader();
			}

			return null;
		}

		public boolean isEmpty() {
			return entityContainerContexts.isEmpty();
		}

		public boolean isLoadedFromDb(Object entityInstance) {
			for (EntityContainerContext entityContainerContext : entityContainerContexts) {
				if (entityContainerContext.getEntityContainer().isLoadedFromDb(entityInstance))
					return true;
			}

			return false;
		}
	}

	public void addEntityManagerContext(EntityContainerContext entityManagerContext) {
		entityContainerContextManager.add(entityManagerContext);
	}
}
