package org.minijpa.jpa;


import javax.persistence.EntityManager;
import javax.persistence.PersistenceContextType;
import javax.persistence.spi.PersistenceUnitInfo;

import org.minijpa.jdbc.ConnectionHolder;
import org.minijpa.metadata.PersistenceUnitContext;

public abstract class AbstractEntityManager implements EntityManager {

    protected PersistenceUnitContext persistenceUnitContext;
    protected PersistenceUnitInfo persistenceUnitInfo;
    protected MiniPersistenceContext persistenceContext;
    protected PersistenceContextType persistenceContextType = PersistenceContextType.TRANSACTION;
    protected ConnectionHolder connectionHolder;

    public MiniPersistenceContext getPersistenceContext() {
	return persistenceContext;
    }

    public ConnectionHolder getConnectionHolder() {
	return connectionHolder;
    }

    public PersistenceUnitContext getPersistenceUnitContext() {
	return persistenceUnitContext;
    }

}
