package org.tinyjpa.jpa;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.persistence.EntityTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.ConnectionProvider;
import org.tinyjpa.metadata.EntityDelegate;

public class EntityTransactionImpl implements EntityTransaction {
	private Logger LOG = LoggerFactory.getLogger(EntityTransactionImpl.class);
	private PersistenceContext persistenceContext;

	public EntityTransactionImpl(PersistenceContext persistenceContext) {
		super();
		this.persistenceContext = persistenceContext;
	}

	@Override
	public void begin() {
//		abstractEntityManager.beginTransaction();
	}

	@Override
	public void commit() {
		Connection connection = null;
		try {
			connection = new ConnectionProvider().getConnection(persistenceContext.getPersistenceUnitInfo());
		} catch (SQLException e) {
			LOG.error(e.getMessage());
			return;
		}

		try {
			new PersistenceHelper(persistenceContext).persist(connection, EntityDelegate.getInstance().getChanges(),
					persistenceContext.getPersistenceUnitInfo());
			connection.commit();
		} catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException | SQLException e) {
			LOG.error(e.getMessage());
			try {
				connection.rollback();
			} catch (SQLException e1) {
				LOG.error(e1.getMessage());
			}
		}

		try {
			connection.close();
		} catch (SQLException e) {
			LOG.error(e.getMessage());
		}
	}

	@Override
	public void rollback() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRollbackOnly() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean getRollbackOnly() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isActive() {
		// TODO Auto-generated method stub
		return false;
	}

}
