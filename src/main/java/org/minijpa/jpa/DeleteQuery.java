package org.minijpa.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.TransactionRequiredException;
import javax.persistence.criteria.CriteriaDelete;

import org.minijpa.jpa.db.JdbcEntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteQuery extends AbstractQuery {

    private final Logger LOG = LoggerFactory.getLogger(DeleteQuery.class);
    private final CriteriaDelete<?> criteriaDelete;
    private final EntityManager entityManager;

    public DeleteQuery(CriteriaDelete<?> criteriaDelete, EntityManager entityManager,
	    JdbcEntityManager jdbcEntityManager) {
	super();
	this.criteriaDelete = criteriaDelete;
	this.entityManager = entityManager;
	this.jdbcEntityManager = jdbcEntityManager;
    }

    public CriteriaDelete<?> getCriteriaDelete() {
	return criteriaDelete;
    }

    @Override
    public List getResultList() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Object getSingleResult() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public int executeUpdate() {
	if (!entityManager.getTransaction().isActive())
	    throw new TransactionRequiredException("Update requires an active transaction");

	try {
	    return jdbcEntityManager.delete(this);
	} catch (Exception e) {
	    LOG.error(e.getMessage());
	    throw new PersistenceException(e.getMessage());
	}
    }

}
