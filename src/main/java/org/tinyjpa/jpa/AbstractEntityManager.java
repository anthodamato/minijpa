package org.tinyjpa.jpa;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContextType;
import javax.persistence.spi.PersistenceUnitInfo;

import org.tinyjpa.metadata.Entity;

public abstract class AbstractEntityManager implements EntityManager {
	protected Map<String, Entity> entities;
	protected PersistenceUnitInfo persistenceUnitInfo;
	protected PersistenceContextImpl persistenceContext;
	protected PersistenceContextType persistenceContextType = PersistenceContextType.TRANSACTION;

	public PersistenceContextImpl getPersistenceContext() {
		return persistenceContext;
	}

	public void beginTransaction() {
//		if (persistenceContextType.equals(PersistenceContextType.TRANSACTION))
//			persistenceContext = new PersistenceContext(entities);
	}

	public void commitTransaction()
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException {
//		new JdbcRunner().persist(EntityDelegate.getInstance().getChanges(), persistenceUnitInfo);
	}

}
