package org.tinyjpa.jpa;

import java.lang.reflect.InvocationTargetException;
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
import org.tinyjpa.metadata.enhancer.BytecodeEnhancerProvider;
import org.tinyjpa.metadata.enhancer.EnhEntity;

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
		Map<String, MetaEntity> entities = new HashMap<>();
		Parser parser = new Parser();
		for (String className : persistenceUnitInfo.getManagedClassNames()) {
			LOG.info("createEntities: className=" + className);
			EnhEntity enhEntity = BytecodeEnhancerProvider.getInstance().getBytecodeEnhancer().enhance(className);
			MetaEntity metaEntity = parser.parse(enhEntity, entities.values());
			entities.put(enhEntity.getClassName(), metaEntity);
		}

		// replaces the existing meta entities
		entities.putAll(existingMetaEntities);

		parser.fillRelationships(entities);

		EntityDelegate.getInstance()
				.addEntityContext(new EntityContext(persistenceUnitInfo.getPersistenceUnitName(), entities));

		return entities;
	}

	@Override
	public EntityManager createEntityManager() {
		synchronized (persistenceUnitInfo) {
			if (entities == null)
				try {
					entities = createEntities();
					LOG.info("createEntityManager: createEntities entities=" + entities);
				} catch (Exception e) {
					LOG.error("Unable to read entities: " + e.getMessage());
					if (e instanceof InvocationTargetException) {
						InvocationTargetException targetException = (InvocationTargetException) e;
						if (targetException.getTargetException() != null)
							LOG.error("Unable to read entities: " + targetException.getTargetException().getMessage());
					}

					if (e.getStackTrace() != null) {
						LOG.error("stacktrace:");
						for (StackTraceElement element : e.getStackTrace()) {
							LOG.error(element.getClassName() + "." + element.getMethodName() + " - "
									+ element.getLineNumber());
						}
					}
				}
		}

		return new EntityManagerImpl(this, persistenceUnitInfo, entities);
	}

	@Override
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

	@Override
	public EntityManager createEntityManager(SynchronizationType synchronizationType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EntityManager createEntityManager(SynchronizationType synchronizationType,
			@SuppressWarnings("rawtypes") Map map) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaBuilder getCriteriaBuilder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Metamodel getMetamodel() {
		if (metamodel == null)
			try {
				metamodel = new MetamodelFactory(entities).build();
			} catch (Exception e) {
				LOG.error(e.getMessage());
			}

		return metamodel;
	}

	@Override
	public boolean isOpen() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<String, Object> getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Cache getCache() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PersistenceUnitUtil getPersistenceUnitUtil() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addNamedQuery(String name, Query query) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> T unwrap(Class<T> cls) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph) {
		// TODO Auto-generated method stub

	}

}
