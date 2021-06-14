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

import java.sql.SQLException;

import javax.persistence.EntityTransaction;
import javax.persistence.RollbackException;

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

	try {
	    abstractEntityManager.flush();
	} catch (Exception e) {
	    LOG.error(e.getMessage());
	    throw new RollbackException(e.getMessage());
	}

	try {
	    abstractEntityManager.connectionHolder.commit();
	    LOG.info("Commit");
	} catch (SQLException e) {
	    LOG.error(e.getMessage());
	    throw new RollbackException(e.getMessage());
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
