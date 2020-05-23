package org.tinyjpa.jpa;

import java.beans.IntrospectionException;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.metadata.Entity;
import org.tinyjpa.metadata.EntityDelegate;
import org.tinyjpa.metadata.EntityEnhancer;
import org.tinyjpa.metadata.Parser;

import javassist.CannotCompileException;
import javassist.NotFoundException;

public class EntityManagerFactoryImpl implements EntityManagerFactory {
	private Logger LOG = LoggerFactory.getLogger(EntityManagerFactoryImpl.class);
	private PersistenceUnitInfo persistenceUnitInfo;
	private Map map;
	/**
	 * The key used is the full class name.
	 */
	private static Map<String, Entity> entities;

	public EntityManagerFactoryImpl(PersistenceUnitInfo persistenceUnitInfo, @SuppressWarnings("rawtypes") Map map) {
		super();
		this.persistenceUnitInfo = persistenceUnitInfo;
		this.map = map;
	}

	private synchronized Map<String, Entity> createEntities() throws ClassNotFoundException, IntrospectionException {
		Parser parser = new Parser();
		Map<String, Entity> entities = new HashMap<>();
		for (String className : persistenceUnitInfo.getManagedClassNames()) {
			try {
				new EntityEnhancer().enhance(className);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NotFoundException
					| CannotCompileException e) {
				LOG.error(e.getMessage());
			}

			Entity entity = parser.parse(className);
			if (entity != null)
				entities.put(className, entity);
		}

		EntityDelegate.getInstance().setEntities(entities);
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

		return new EntityManagerImpl(persistenceUnitInfo, entities);
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

		return new EntityManagerImpl(persistenceUnitInfo, entities);
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
		// TODO Auto-generated method stub
		return null;
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
