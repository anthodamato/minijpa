package org.tinyjpa.jpa;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.persistence.Cache;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Query;
import javax.persistence.SynchronizationType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.spi.PersistenceUnitInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.MetaEntity;
import org.tinyjpa.jpa.metamodel.MetamodelFactory;
import org.tinyjpa.metadata.EntityContext;
import org.tinyjpa.metadata.EntityDelegate;
import org.tinyjpa.metadata.Parser;

public class EntityManagerFactoryImpl implements EntityManagerFactory {
	private Logger LOG = LoggerFactory.getLogger(EntityManagerFactoryImpl.class);
	private PersistenceUnitInfo persistenceUnitInfo;
	private Map map;
	/**
	 * The key used is the full class name.
	 */
	private Map<String, MetaEntity> entities;
	private Metamodel metamodel;

	public EntityManagerFactoryImpl(PersistenceUnitInfo persistenceUnitInfo, @SuppressWarnings("rawtypes") Map map) {
		super();
		this.persistenceUnitInfo = persistenceUnitInfo;
		this.map = map;
	}

	private synchronized Map<String, MetaEntity> createEntities() throws Exception {
		// if the entities have been already parsed they are saved in the EntityContext.
		// It must reuse them. Just one MetaEntity instance for each class name must
		// exists.
		Optional<EntityContext> optional = EntityDelegate.getInstance()
				.getEntityContext(persistenceUnitInfo.getPersistenceUnitName());
		if (optional.isPresent()) {
			LOG.info("Persistence Unit Entities already parsed");
			return optional.get().getEntities();
		}

		// collects existing meta entities
		Map<String, MetaEntity> existingMetaEntities = new HashMap<>();
		for (String className : persistenceUnitInfo.getManagedClassNames()) {
			Optional<MetaEntity> optionalMetaEntity = EntityDelegate.getInstance().getMetaEntity(className);
			if (optionalMetaEntity.isPresent())
				existingMetaEntities.put(className, optionalMetaEntity.get());
		}

		LOG.info("Parsing entities...");
		Map<String, MetaEntity> entities = new Parser().createMetaEntities(persistenceUnitInfo.getManagedClassNames());
		// replaces the existing meta entities
		entities.putAll(existingMetaEntities);
//		for (Map.Entry<String, MetaEntity> entry : existingMetaEntities.entrySet()) {
//			entities.put(entry.getKey(), entry.getValue());
//		}

		EntityDelegate.getInstance()
				.addEntityContext(new EntityContext(persistenceUnitInfo.getPersistenceUnitName(), entities));
		return entities;
	}

	public EntityManager createEntityManager() {
		synchronized (persistenceUnitInfo) {
			if (entities == null)
				try {
					entities = createEntities();
				} catch (Exception e) {
					LOG.error("Unable to read entities: " + e.getMessage());
				}
		}

		return new EntityManagerImpl(this, persistenceUnitInfo, entities);
	}

	public EntityManager createEntityManager(@SuppressWarnings("rawtypes") Map map) {
		synchronized (persistenceUnitInfo) {
			if (entities == null)
				try {
					entities = createEntities();
				} catch (Exception e) {
					LOG.error("Unable to read entities: " + e.getMessage());
				}
		}

		return new EntityManagerImpl(this, persistenceUnitInfo, entities);
	}

	public EntityManager createEntityManager(SynchronizationType synchronizationType) {
		// TODO Auto-generated method stub
		return null;
	}

	public EntityManager createEntityManager(SynchronizationType synchronizationType,
			@SuppressWarnings("rawtypes") Map map) {
		// TODO Auto-generated method stub
		return null;
	}

	public CriteriaBuilder getCriteriaBuilder() {
		// TODO Auto-generated method stub
		return null;
	}

	public Metamodel getMetamodel() {
		if (metamodel == null)
			try {
				metamodel = new MetamodelFactory(entities).build();
			} catch (Exception e) {
				LOG.error(e.getMessage());
			}

		return metamodel;
	}

	public boolean isOpen() {
		// TODO Auto-generated method stub
		return false;
	}

	public void close() {
		// TODO Auto-generated method stub

	}

	public Map<String, Object> getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	public Cache getCache() {
		// TODO Auto-generated method stub
		return null;
	}

	public PersistenceUnitUtil getPersistenceUnitUtil() {
		// TODO Auto-generated method stub
		return null;
	}

	public void addNamedQuery(String name, Query query) {
		// TODO Auto-generated method stub

	}

	public <T> T unwrap(Class<T> cls) {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph) {
		// TODO Auto-generated method stub

	}

}
