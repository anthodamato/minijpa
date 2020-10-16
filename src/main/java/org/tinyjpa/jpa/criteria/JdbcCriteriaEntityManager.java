package org.tinyjpa.jpa.criteria;

import java.util.List;

import javax.persistence.criteria.CriteriaQuery;

import org.tinyjpa.jdbc.db.JdbcEntityManager;

public interface JdbcCriteriaEntityManager extends JdbcEntityManager {
	public List<Object> select(CriteriaQuery<?> criteriaQuery) throws Exception;
}
