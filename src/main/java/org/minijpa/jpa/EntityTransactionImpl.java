package org.minijpa.jpa;

import java.sql.SQLException;

import javax.persistence.EntityTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityTransactionImpl implements EntityTransaction {

    private final Logger LOG = LoggerFactory.getLogger(EntityTransactionImpl.class);
    private final AbstractEntityManager abstractEntityManager;
    private boolean active = false;
    private boolean rollbackOnly = false;

    public EntityTransactionImpl(AbstractEntityManager abstractEntityManager) {
	super();
	this.abstractEntityManager = abstractEntityManager;
    }

    @Override
    public void begin() {
//		if (active)
//			throw new IllegalStateException("Transaction already active");

	try {
	    abstractEntityManager.connectionHolder.getConnection();
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
		abstractEntityManager.connectionHolder.rollback();
		return;
	    } catch (SQLException e1) {
		LOG.error(e1.getMessage());
		return;
	    }
	}

	abstractEntityManager.flush();
	try {
	    abstractEntityManager.connectionHolder.commit();
	    LOG.info("Commit");
	} catch (SQLException e) {
	    LOG.error(e.getMessage());
	    try {
		abstractEntityManager.connectionHolder.rollback();
	    } catch (SQLException e1) {
		LOG.error(e1.getMessage());
	    }

	    return;
	}

	try {
	    abstractEntityManager.connectionHolder.closeConnection();
	} catch (SQLException e) {
	    LOG.error(e.getMessage());
	}

	this.active = false;
	abstractEntityManager.persistenceContext.resetLockType();
    }

    @Override
    public void rollback() {
	if (!active)
	    throw new IllegalStateException("Transaction not active");

	try {
	    abstractEntityManager.connectionHolder.rollback();
	    LOG.info("Rollback");
	} catch (SQLException e) {
	    LOG.error(e.getMessage());
	    try {
		abstractEntityManager.connectionHolder.rollback();
	    } catch (SQLException e1) {
		LOG.error(e1.getMessage());
	    }

	    return;
	}

	try {
	    abstractEntityManager.connectionHolder.closeConnection();
	} catch (SQLException e) {
	    LOG.error(e.getMessage());
	}

	try {
	    abstractEntityManager.persistenceContext.detachAll();
	} catch (Exception ex) {
	    LOG.error(ex.getMessage());
	}

	this.active = false;
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
