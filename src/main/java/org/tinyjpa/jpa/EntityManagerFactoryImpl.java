package org.tinyjpa.jpa;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import org.tinyjpa.metadata.enhancer.EnhEntity;
import org.tinyjpa.metadata.enhancer.EnhEntityRegistry;
import org.tinyjpa.metadata.enhancer.javassist.AttributeData;
import org.tinyjpa.metadata.enhancer.javassist.ClassInspector;
import org.tinyjpa.metadata.enhancer.javassist.EntityEnhancer;
import org.tinyjpa.metadata.enhancer.javassist.ManagedData;

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
		ClassInspector classInspector = new ClassInspector();
		EntityEnhancer entityEnhancer = new EntityEnhancer();
		Parser parser = new Parser();
//		Map<String, MetaEntity> entities = parser.createMetaEntities(persistenceUnitInfo.getManagedClassNames());
		List<EnhEntity> parsedEntities = new ArrayList<>();
		parsedEntities.addAll(EnhEntityRegistry.getInstance().getEnhEntities());

		List<ManagedData> inspectedClasses = new ArrayList<>(EnhEntityRegistry.getInstance().getInspectedClasses());
		for (String className : persistenceUnitInfo.getManagedClassNames()) {
			LOG.info("createEntities: className=" + className);
			LOG.info("createEntities: EnhEntityRegistry.getInstance()=" + EnhEntityRegistry.getInstance());
			Optional<EnhEntity> optionalEnhEntity = EnhEntityRegistry.getInstance().getEnhEntity(className);
			if (optionalEnhEntity.isPresent()) {
				LOG.info("createEntities: className=" + className + " found in registry");
				MetaEntity metaEntity = parser.parse(optionalEnhEntity.get(), entities.values());
				EnhEntity enhEntity = optionalEnhEntity.get();
				parsedEntities.add(enhEntity);
				entities.put(enhEntity.getClassName(), metaEntity);
			} else {
				ManagedData managedData = classInspector.inspect(className, inspectedClasses);
				LOG.info("createEntities: className=" + className + "; managedData=" + managedData);
				if (managedData == null)
					continue;

				EnhEntity enhMappedSuperclassEntity = null;
				if (managedData.mappedSuperclass != null) {
					enhMappedSuperclassEntity = entityEnhancer.enhance(managedData.mappedSuperclass, parsedEntities);
				}

				EnhEntity enhEntity = entityEnhancer.enhance(managedData, parsedEntities);
				enhEntity.setMappedSuperclass(enhMappedSuperclassEntity);
				LOG.info("createEntities: className=" + className + "; enhEntity=" + enhEntity);

				EnhEntityRegistry.getInstance().add(enhEntity);
				EnhEntityRegistry.getInstance().add(managedData);
				if (managedData.mappedSuperclass != null)
					EnhEntityRegistry.getInstance().add(managedData.mappedSuperclass);

				for (AttributeData attributeData : managedData.getDataAttributes()) {
					if (attributeData.getEmbeddedData() != null)
						EnhEntityRegistry.getInstance().add(attributeData.getEmbeddedData());
				}

				LOG.info("createEntities: className=" + className + " added to registry");

				MetaEntity metaEntity = parser.parse(enhEntity, entities.values());

				entities.put(enhEntity.getClassName(), metaEntity);
			}
		}

		// replaces the existing meta entities
		entities.putAll(existingMetaEntities);

		parser.fillRelationships(entities);

		EntityDelegate.getInstance()
				.addEntityContext(new EntityContext(persistenceUnitInfo.getPersistenceUnitName(), entities));
		return entities;
	}

	public EntityManager createEntityManager() {
		LOG.info("createEntityManager: entities=" + entities);
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
