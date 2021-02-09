package org.minijpa.jpa;

import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Parameter;
import javax.persistence.PersistenceException;
import javax.persistence.TransactionRequiredException;
import javax.persistence.criteria.CriteriaDelete;
import org.minijpa.jpa.criteria.CriteriaUtils;

import org.minijpa.jpa.db.JdbcEntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteQuery extends AbstractQuery {

    private Logger LOG = LoggerFactory.getLogger(DeleteQuery.class);
    private CriteriaDelete<?> criteriaDelete;
    private EntityManager entityManager;
    private Set<Parameter<?>> parameters;

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
    public Parameter<?> getParameter(String name) {
	return CriteriaUtils.findParameterByName(parameters, name);
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
