package org.minijpa.jpa;

import java.util.List;

import javax.persistence.PersistenceException;
import javax.persistence.criteria.CriteriaUpdate;

import org.minijpa.jpa.db.JdbcEntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateQuery extends AbstractQuery {
	private Logger LOG = LoggerFactory.getLogger(UpdateQuery.class);
	private CriteriaUpdate<?> criteriaUpdate;

	public UpdateQuery(CriteriaUpdate<?> criteriaUpdate, JdbcEntityManager jdbcCriteriaEntityManager) {
		super();
		this.criteriaUpdate = criteriaUpdate;
		this.jdbcCriteriaEntityManager = jdbcCriteriaEntityManager;
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
		try {
			return jdbcCriteriaEntityManager.update(this);
		} catch (Exception e) {
			LOG.error(e.getMessage());
			throw new PersistenceException(e.getMessage());
		}
	}

}
