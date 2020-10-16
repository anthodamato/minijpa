package org.tinyjpa.jpa;

import java.sql.Connection;
import java.sql.SQLException;

import javax.persistence.EntityTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityTransactionImpl implements EntityTransaction {
	private Logger LOG = LoggerFactory.getLogger(EntityTransactionImpl.class);
	private AbstractEntityManager abstractEntityManager;
	private boolean active = false;
	private boolean rollbackOnly = false;
	private Connection connection;

	public EntityTransactionImpl(AbstractEntityManager abstractEntityManager) {
		super();
		this.abstractEntityManager = abstractEntityManager;
	}

	@Override
	public void begin() {
		if (active)
			throw new IllegalStateException("Transaction already active");

		try {
			connection = abstractEntityManager.connectionHolder.getConnection();
		} catch (SQLException e) {
			LOG.error(e.getMessage());
			return;
		}

		this.active = true;
	}

	@Override
	public void commit() {
		if (!active)
			throw new IllegalStateException("Transaction not active");

		if (getRollbackOnly()) {
			LOG.warn("Rollback transaction event");
			try {
				connection.rollback();
				return;
			} catch (SQLException e1) {
				LOG.error(e1.getMessage());
				return;
			}
		}

		try {
			connection.commit();
			LOG.info("Commit Done");
		} catch (SQLException e) {
			LOG.error(e.getMessage());
			try {
				connection.rollback();
			} catch (SQLException e1) {
				LOG.error(e1.getMessage());
			}
		}

//		try {
//			connection.close();
//		} catch (SQLException e) {
//			LOG.error(e.getMessage());
//		}
	}

	@Override
	public void rollback() {
		if (!active)
			throw new IllegalStateException("Transaction not active");

		try {
			connection.rollback();
		} catch (SQLException e) {
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
	public void setRollbackOnly() {
		this.rollbackOnly = true;
	}

	@Override
	public boolean getRollbackOnly() {
		return rollbackOnly;
	}

	@Override
	public boolean isActive() {
		return active;
	}

}
