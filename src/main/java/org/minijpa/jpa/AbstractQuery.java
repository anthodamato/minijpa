package org.minijpa.jpa;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.minijpa.jpa.criteria.CriteriaUtils;
import org.minijpa.jpa.db.JdbcEntityManager;

public abstract class AbstractQuery implements Query {
	protected JdbcEntityManager jdbcCriteriaEntityManager;
	protected FlushModeType flushModeType = FlushModeType.AUTO;
	protected Map<String, Object> namedParameters = new HashMap<>();
	protected Set<Parameter<?>> parameters;

	@Override
	public Query setMaxResults(int maxResult) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMaxResults() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Query setFirstResult(int startPosition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getFirstResult() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Query setHint(String hintName, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> getHints() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Query setParameter(Parameter<T> param, T value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Query setParameter(Parameter<Calendar> param, Calendar value, TemporalType temporalType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Query setParameter(Parameter<Date> param, Date value, TemporalType temporalType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Query setParameter(String name, Object value) {
		namedParameters.put(name, value);
		return this;
	}

	@Override
	public Query setParameter(String name, Calendar value, TemporalType temporalType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Query setParameter(String name, Date value, TemporalType temporalType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Query setParameter(int position, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Query setParameter(int position, Calendar value, TemporalType temporalType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Query setParameter(int position, Date value, TemporalType temporalType) {
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
		return CriteriaUtils.findParameterByName(parameters, name);
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
	public Query setFlushMode(FlushModeType flushMode) {
		this.flushModeType = flushMode;
		return this;
	}

	@Override
	public FlushModeType getFlushMode() {
		return flushModeType;
	}

	@Override
	public Query setLockMode(LockModeType lockMode) {
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

}
