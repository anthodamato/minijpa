package org.tinyjpa.jpa.criteria;

import java.util.Map;

import javax.persistence.criteria.CriteriaQuery;

import org.tinyjpa.jdbc.MetaEntity;
import org.tinyjpa.jdbc.SqlStatement;
import org.tinyjpa.jdbc.db.DbJdbc;

public interface DbJdbcCriteria extends DbJdbc {
	public SqlStatement select(CriteriaQuery<?> criteriaQuery, Map<String, MetaEntity> entities) throws Exception;

}
