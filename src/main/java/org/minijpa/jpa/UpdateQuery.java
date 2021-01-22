package org.minijpa.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.TransactionRequiredException;
import javax.persistence.criteria.CriteriaUpdate;

import org.minijpa.jpa.db.JdbcEntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateQuery extends AbstractQuery {
	private Logger LOG = LoggerFactory.getLogger(UpdateQuery.class);
	private CriteriaUpdate<?> criteriaUpdate;
	private EntityManager entityManager;

	public UpdateQuery(CriteriaUpdate<?> criteriaUpdate, EntityManager entityManager,
			JdbcEntityManager jdbcEntityManager) {
		super();
		this.criteriaUpdate = criteriaUpdate;
		this.entityManager = entityManager;
		this.jdbcEntityManager = jdbcEntityManager;
	}

	public CriteriaUpdate<?> getCriteriaUpdate() {
		return criteriaUpdate;
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
			return jdbcEntityManager.update(this);
		} catch (Exception e) {
			LOG.error(e.getMessage());
			throw new PersistenceException(e.getMessage());
		}
	}

}
