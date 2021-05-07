package org.minijpa.jpa;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

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

import org.minijpa.jpa.criteria.MiniCriteriaBuilder;
import org.minijpa.jpa.metamodel.MetamodelFactory;
import org.minijpa.metadata.PersistenceUnitContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiniEntityManagerFactory implements EntityManagerFactory {

    private final Logger LOG = LoggerFactory.getLogger(MiniEntityManagerFactory.class);
    private final EntityManagerType entityManagerType;
    private final PersistenceUnitInfo persistenceUnitInfo;
    private final Map<String, Object> properties = new HashMap<>();
    private final Map map;
    /**
     * The key used is the full class name.
     */
    private PersistenceUnitContext persistenceUnitContext;
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

//    private synchronized PersistenceUnitContext createPersistenceUnitContext() throws Exception {
//	// if the entities have been already parsed they are saved in the EntityContext.
//	// It must reuse them. Just one MetaEntity instance for each class name must
//	// exists.
//	Optional<PersistenceUnitContext> optional = EntityDelegate.getInstance()
//		.getEntityContext(persistenceUnitInfo.getPersistenceUnitName());
//	if (optional.isPresent()) {
//	    LOG.debug("Persistence Unit Entities already parsed");
//	    return optional.get();
//	}
//
//	// collects existing meta entities
//	Map<String, MetaEntity> existingMetaEntities = new HashMap<>();
//	for (String className : persistenceUnitInfo.getManagedClassNames()) {
//	    Optional<MetaEntity> optionalMetaEntity = EntityDelegate.getInstance().getMetaEntity(className);
//	    if (optionalMetaEntity.isPresent())
//		existingMetaEntities.put(className, optionalMetaEntity.get());
//	}
//
//	LOG.info("Parsing entities...");
//	Map<String, MetaEntity> entityMap = new HashMap<>();
//	DbConfiguration dbConfiguration = DbConfigurationList.getInstance().getDbConfiguration(persistenceUnitInfo);
//	Parser parser = new Parser(dbConfiguration);
//	for (String className : persistenceUnitInfo.getManagedClassNames()) {
//	    EnhEntity enhEntity = BytecodeEnhancerProvider.getInstance().getBytecodeEnhancer().enhance(className);
//	    MetaEntity metaEntity = parser.parse(enhEntity, entityMap.values());
//	    entityMap.put(enhEntity.getClassName(), metaEntity);
//	}
//
//	// replaces the existing meta entities
//	entityMap.putAll(existingMetaEntities);
//
//	parser.fillRelationships(entityMap);
//	Optional<Map<String, QueryResultMapping>> queryResultMappings = parser.parseSqlResultSetMappings(entityMap);
//
//	PersistenceUnitContext puc = new PersistenceUnitContext(persistenceUnitInfo.getPersistenceUnitName(),
//		entityMap, queryResultMappings);
//	EntityDelegate.getInstance().addPersistenceUnitContext(puc);
//	return puc;
//    }
    @Override
    public EntityManager createEntityManager() {
	synchronized (persistenceUnitInfo) {
	    if (persistenceUnitContext == null)
	    try {
//		persistenceUnitContext = createPersistenceUnitContext();
		persistenceUnitContext = PersistenceUnitContextManager.getInstance().get(persistenceUnitInfo);
		LOG.debug("createEntityManager: createEntities entities=" + persistenceUnitContext.getEntities());
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

	return new MiniEntityManager(this, persistenceUnitInfo, persistenceUnitContext);
    }

    @Override
    public EntityManager createEntityManager(@SuppressWarnings("rawtypes") Map map) {
	synchronized (persistenceUnitInfo) {
	    if (persistenceUnitContext == null)
				try {
//		persistenceUnitContext = createPersistenceUnitContext();
		persistenceUnitContext = PersistenceUnitContextManager.getInstance().get(persistenceUnitInfo);
	    } catch (Exception e) {
		LOG.error("Unable to read entities: " + e.getMessage());
		throw new IllegalStateException(e.getMessage());
	    }
	}

	return new MiniEntityManager(this, persistenceUnitInfo, persistenceUnitContext);
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
	    if (persistenceUnitContext == null)
	        try {
//		persistenceUnitContext = createPersistenceUnitContext();
		persistenceUnitContext = PersistenceUnitContextManager.getInstance().get(persistenceUnitInfo);
	    } catch (Exception e) {
		LOG.error("Unable to read entities: " + e.getMessage());
		throw new IllegalStateException(e.getMessage());
	    }
	}

	return new MiniCriteriaBuilder(getMetamodel(), persistenceUnitContext);
    }

    @Override
    public Metamodel getMetamodel() {
	if (metamodel == null) {
	    synchronized (persistenceUnitInfo) {
		if (persistenceUnitContext == null)
		try {
//		    persistenceUnitContext = createPersistenceUnitContext();
		    persistenceUnitContext = PersistenceUnitContextManager.getInstance().get(persistenceUnitInfo);
		} catch (Exception e) {
		    LOG.error("Unable to read entities: " + e.getMessage());
		    throw new IllegalStateException(e.getMessage());
		}
	    }

	    try {
		metamodel = new MetamodelFactory(persistenceUnitContext.getEntities()).build();
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
