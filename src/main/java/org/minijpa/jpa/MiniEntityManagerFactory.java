package org.minijpa.jpa;

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

import org.minijpa.jdbc.DbTypeMapper;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jpa.criteria.MiniCriteriaBuilder;
import org.minijpa.jpa.db.DbConfigurationList;
import org.minijpa.jpa.metamodel.MetamodelFactory;
import org.minijpa.metadata.EntityContext;
import org.minijpa.metadata.EntityDelegate;
import org.minijpa.metadata.Parser;
import org.minijpa.metadata.enhancer.BytecodeEnhancerProvider;
import org.minijpa.metadata.enhancer.EnhEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiniEntityManagerFactory implements EntityManagerFactory {

    private Logger LOG = LoggerFactory.getLogger(MiniEntityManagerFactory.class);
    private EntityManagerType entityManagerType;
    private PersistenceUnitInfo persistenceUnitInfo;
    private Map<String, Object> properties = new HashMap<>();
    private Map map;
    /**
     * The key used is the full class name.
     */
    private Map<String, MetaEntity> entities;
    private Metamodel metamodel;

    public MiniEntityManagerFactory(EntityManagerType entityManagerType, PersistenceUnitInfo persistenceUnitInfo,
	    @SuppressWarnings("rawtypes") Map map) {
	super();
	this.entityManagerType = entityManagerType;
	this.persistenceUnitInfo = persistenceUnitInfo;
	this.map = map;
    }

    public EntityManagerType getEntityManagerType() {
	return entityManagerType;
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
	DbTypeMapper dbTypeMapper = DbConfigurationList.getInstance().getDbConfiguration(persistenceUnitInfo)
		.getDbTypeMapper();
	Parser parser = new Parser(dbTypeMapper);
	for (String className : persistenceUnitInfo.getManagedClassNames()) {
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
		    LOG.error("stacktrace: ");
		    for (StackTraceElement element : e.getStackTrace()) {
			LOG.error(element.getClassName() + "." + element.getMethodName() + " - "
				+ element.getLineNumber());
		    }
		}

		throw new IllegalStateException(e.getMessage());
	    }
	}

	return new MiniEntityManager(this, persistenceUnitInfo, entities);
    }

    @Override
    public EntityManager createEntityManager(@SuppressWarnings("rawtypes") Map map) {
	synchronized (persistenceUnitInfo) {
	    if (entities == null)
				try {
		entities = createEntities();
	    } catch (Exception e) {
		LOG.error("Unable to read entities: " + e.getMessage());
		throw new IllegalStateException(e.getMessage());
	    }
	}

	return new MiniEntityManager(this, persistenceUnitInfo, entities);
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
	synchronized (persistenceUnitInfo) {
	    if (entities == null)
				try {
		entities = createEntities();
	    } catch (Exception e) {
		LOG.error("Unable to read entities: " + e.getMessage());
		throw new IllegalStateException(e.getMessage());
	    }
	}

	return new MiniCriteriaBuilder(getMetamodel(), entities);
    }

    @Override
    public Metamodel getMetamodel() {
	if (metamodel == null) {
	    synchronized (persistenceUnitInfo) {
		if (entities == null)
					try {
		    entities = createEntities();
		} catch (Exception e) {
		    LOG.error("Unable to read entities: " + e.getMessage());
		    throw new IllegalStateException(e.getMessage());
		}
	    }

	    try {
		metamodel = new MetamodelFactory(entities).build();
	    } catch (Exception e) {
		LOG.error(e.getMessage());
	    }
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
	return properties;
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
