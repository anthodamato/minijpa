package org.tinyjpa.jpa;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContextType;
import javax.persistence.spi.PersistenceUnitInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.ConnectionProvider;
import org.tinyjpa.metadata.Entity;

public abstract class AbstractEntityManager implements EntityManager {
	private Logger LOG = LoggerFactory.getLogger(getClass());

	protected Map<String, Entity> entities;
	protected PersistenceUnitInfo persistenceUnitInfo;
	protected PersistenceContextImpl persistenceContext;
	protected PersistenceContextType persistenceContextType = PersistenceContextType.TRANSACTION;
	protected Connection connection;

	public PersistenceContextImpl getPersistenceContext() {
		return persistenceContext;
	}

	public Connection createConnection() throws SQLException {
		connection = new ConnectionProvider().getConnection(persistenceContext.getPersistenceUnitInfo());
		return connection;
	}
}
