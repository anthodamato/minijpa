/*
 * Copyright 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    @Override
    public EntityManager createEntityManager() {
	synchronized (persistenceUnitInfo) {
	    if (persistenceUnitContext == null)
	    try {
		persistenceUnitContext = PersistenceUnitContextManager.getInstance().get(persistenceUnitInfo);
		LOG.debug("createEntityManager: createEntities entities=" + persistenceUnitContext.getEntities());
		persistenceUnitContext.getEntities().forEach((k, v) -> {
		    LOG.debug("createEntityManager: v.getName()=" + v.getName());
		    v.getBasicAttributes().forEach(a -> LOG.debug("createEntityManager: ba a.getName()=" + a.getName()));
		});
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
	LOG.debug("getMetamodel: metamodel=" + metamodel);
	if (metamodel == null) {
	    synchronized (persistenceUnitInfo) {
		if (persistenceUnitContext == null)
		try {
		    persistenceUnitContext = PersistenceUnitContextManager.getInstance().get(persistenceUnitInfo);
		} catch (Exception e) {
		    LOG.error("Unable to read entities: " + e.getMessage());
		    throw new IllegalStateException(e.getMessage());
		}
	    }

	    persistenceUnitContext.getEntities().forEach((k, v) -> {
		LOG.debug("getMetamodel: v.getName()=" + v.getName());
		v.getBasicAttributes().forEach(a -> LOG.debug("getMetamodel: ba a.getName()=" + a.getName()));
	    });
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
