package org.tinyjpa.jpa;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContextType;
import javax.persistence.spi.PersistenceUnitInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.ConnectionHolder;
import org.tinyjpa.jdbc.Entity;

public abstract class AbstractEntityManager implements EntityManager {
	private Logger LOG = LoggerFactory.getLogger(getClass());

	protected Map<String, Entity> entities;
	protected PersistenceUnitInfo persistenceUnitInfo;
	protected PersistenceContextImpl persistenceContext;
	protected PersistenceContextType persistenceContextType = PersistenceContextType.TRANSACTION;
	protected ConnectionHolder connectionHolder;

	public PersistenceContextImpl getPersistenceContext() {
		return persistenceContext;
	}

	public ConnectionHolder getConnectionHolder() {
		return connectionHolder;
	}

//	public Connection createConnection() throws SQLException {
//		connection = new ConnectionProviderImpl(persistenceUnitInfo).getConnection();
//		return connection;
//	}
}
