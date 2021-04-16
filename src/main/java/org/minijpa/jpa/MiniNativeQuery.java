/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa;

import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceException;
import javax.persistence.TransactionRequiredException;
import org.minijpa.jpa.db.JdbcEntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author adamato
 */
public class MiniNativeQuery extends AbstractQuery {

    private final Logger LOG = LoggerFactory.getLogger(MiniNativeQuery.class);

    private final String sqlString;
    private final Optional<Class<?>> resultClass;
    private final Optional<String> resultSetMapping;
    private final EntityManager entityManager;

    public MiniNativeQuery(String sqlString, Optional<Class<?>> resultClass,
	    Optional<String> resultSetMapping,
	    EntityManager entityManager,
	    JdbcEntityManager jdbcEntityManager) {
	this.sqlString = sqlString;
	this.resultClass = resultClass;
	this.resultSetMapping = resultSetMapping;
	this.entityManager = entityManager;
	this.jdbcEntityManager = jdbcEntityManager;
    }

    public String getSqlString() {
	return sqlString;
    }

    public Optional<Class<?>> getResultClass() {
	return resultClass;
    }

    public Optional<String> getResultSetMapping() {
	return resultSetMapping;
    }

    @Override
    public List getResultList() {
	List<?> list = null;
	try {
	    if (flushModeType == FlushModeType.AUTO)
		jdbcEntityManager.flush();

	    list = jdbcEntityManager.selectNative(this);
	} catch (Exception e) {
	    LOG.error(e.getMessage());
	    throw new PersistenceException(e.getMessage());
	}

	return list;
    }

    @Override
    public Object getSingleResult() {
	List<?> list = null;
	try {
	    if (flushModeType == FlushModeType.AUTO)
		jdbcEntityManager.flush();

	    list = jdbcEntityManager.selectNative(this);
	} catch (Exception e) {
	    LOG.error(e.getMessage());
	    throw new PersistenceException(e.getMessage());
	}

	if (list.isEmpty())
	    throw new NoResultException("No result to return");

	if (list.size() > 1)
	    throw new NonUniqueResultException("More than one result to return");

	return list.get(0);
    }

    @Override
    public int executeUpdate() {
	if (!entityManager.getTransaction().isActive())
	    throw new TransactionRequiredException("Update requires an active transaction");

	try {
	    return jdbcEntityManager.update(sqlString, this);
	} catch (Exception e) {
	    LOG.error(e.getMessage());
	    throw new PersistenceException(e.getMessage());
	}
    }

}
