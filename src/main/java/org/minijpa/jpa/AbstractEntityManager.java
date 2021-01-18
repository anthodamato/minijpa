package org.minijpa.jpa;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContextType;
import javax.persistence.spi.PersistenceUnitInfo;

import org.minijpa.jdbc.ConnectionHolder;
import org.minijpa.jdbc.MetaEntity;

public abstract class AbstractEntityManager implements EntityManager {
	protected Map<String, MetaEntity> entities;
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

	public Map<String, MetaEntity> getEntities() {
		return entities;
	}

}
