package org.tinyjpa.jpa;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Parameter;
import javax.persistence.PersistenceException;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jpa.criteria.JdbcCriteriaEntityManager;

public class TypedQueryImpl<X> implements TypedQuery<X> {
	private Logger LOG = LoggerFactory.getLogger(TypedQueryImpl.class);
	protected CriteriaQuery<?> criteriaQuery;
	protected JdbcCriteriaEntityManager jdbcCriteriaEntityManager;

	public TypedQueryImpl(CriteriaQuery<?> criteriaQuery, JdbcCriteriaEntityManager jdbcCriteriaEntityManager) {
		super();
		this.criteriaQuery = criteriaQuery;
		this.jdbcCriteriaEntityManager = jdbcCriteriaEntityManager;
	}

	@Override
	public List<X> getResultList() {
		List<?> list = null;
		try {
//			list = criteriaJdbcEntityManager.loadAllFields(criteriaQuery.getSelection().getJavaType());
			jdbcCriteriaEntityManager.flush();
			list = jdbcCriteriaEntityManager.select(criteriaQuery);
		} catch (Exception e) {
			LOG.error(e.getMessage());
			throw new PersistenceException(e.getMessage());
		}

		return (List<X>) list;
	}

	@Override
	public int executeUpdate() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMaxResults() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getFirstResult() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Map<String, Object> getHints() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Parameter<?>> getParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Parameter<?> getParameter(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Parameter<T> getParameter(String name, Class<T> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Parameter<?> getParameter(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Parameter<T> getParameter(int position, Class<T> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isBound(Parameter<?> param) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <T> T getParameterValue(Parameter<T> param) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getParameterValue(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getParameterValue(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FlushModeType getFlushMode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LockModeType getLockMode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T unwrap(Class<T> cls) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public X getSingleResult() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypedQuery<X> setMaxResults(int maxResult) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypedQuery<X> setFirstResult(int startPosition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypedQuery<X> setHint(String hintName, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> TypedQuery<X> setParameter(Parameter<T> param, T value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypedQuery<X> setParameter(Parameter<Calendar> param, Calendar value, TemporalType temporalType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypedQuery<X> setParameter(Parameter<Date> param, Date value, TemporalType temporalType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypedQuery<X> setParameter(String name, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypedQuery<X> setParameter(String name, Calendar value, TemporalType temporalType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypedQuery<X> setParameter(String name, Date value, TemporalType temporalType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypedQuery<X> setParameter(int position, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypedQuery<X> setParameter(int position, Calendar value, TemporalType temporalType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypedQuery<X> setParameter(int position, Date value, TemporalType temporalType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypedQuery<X> setFlushMode(FlushModeType flushMode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypedQuery<X> setLockMode(LockModeType lockMode) {
		// TODO Auto-generated method stub
		return null;
	}

}
