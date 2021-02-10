package org.minijpa.jpa;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Parameter;
import javax.persistence.PersistenceException;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import org.minijpa.jpa.db.JdbcEntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiniTypedQuery<X> extends AbstractQuery implements TypedQuery<X> {

    private final Logger LOG = LoggerFactory.getLogger(MiniTypedQuery.class);
    private final CriteriaQuery<?> criteriaQuery;

    public MiniTypedQuery(CriteriaQuery<?> criteriaQuery, JdbcEntityManager jdbcCriteriaEntityManager) {
	super();
	this.criteriaQuery = criteriaQuery;
	this.jdbcEntityManager = jdbcCriteriaEntityManager;
    }

    public CriteriaQuery<?> getCriteriaQuery() {
	return criteriaQuery;
    }

    @Override
    public List<X> getResultList() {
	List<?> list = null;
	try {
	    if (flushModeType == FlushModeType.AUTO)
		jdbcEntityManager.flush();

	    LOG.info("getResultList: select criteriaQuery=" + criteriaQuery);
	    list = jdbcEntityManager.select(this);
	} catch (Exception e) {
	    LOG.error(e.getMessage());
	    throw new PersistenceException(e.getMessage());
	}

	return (List<X>) list;
    }

    @Override
    public int executeUpdate() {
	throw new IllegalStateException("Update call made from a Select context");
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
    public boolean isBound(Parameter<?> param) {
	// TODO Auto-generated method stub
	return false;
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
	List<?> list = null;
	try {
	    if (flushModeType == FlushModeType.AUTO)
		jdbcEntityManager.flush();

	    list = jdbcEntityManager.select(this);
	} catch (Exception e) {
	    LOG.error(e.getMessage());
	    throw new PersistenceException(e.getMessage());
	}

	if (list.isEmpty())
	    throw new NoResultException("No result to return");

	if (list.size() > 1)
	    throw new NonUniqueResultException("More than one result to return");

	return (X) list.get(0);
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
	super.setParameter(param, value);
	return this;
    }

    @Override
    public TypedQuery<X> setParameter(Parameter<Calendar> param, Calendar value, TemporalType temporalType) {
	super.setParameter(param, value, temporalType);
	return this;
    }

    @Override
    public TypedQuery<X> setParameter(Parameter<Date> param, Date value, TemporalType temporalType) {
	super.setParameter(param, value, temporalType);
	return this;
    }

    @Override
    public TypedQuery<X> setParameter(String name, Object value) {
	super.setParameter(name, value);
	return this;
    }

    @Override
    public TypedQuery<X> setParameter(String name, Calendar value, TemporalType temporalType) {
	super.setParameter(name, value, temporalType);
	return this;
    }

    @Override
    public TypedQuery<X> setParameter(String name, Date value, TemporalType temporalType) {
	super.setParameter(name, value, temporalType);
	return this;
    }

    @Override
    public TypedQuery<X> setParameter(int position, Object value) {
	super.setParameter(position, value);
	return this;
    }

    @Override
    public TypedQuery<X> setParameter(int position, Calendar value, TemporalType temporalType) {
	super.setParameter(position, value, temporalType);
	return this;
    }

    @Override
    public TypedQuery<X> setParameter(int position, Date value, TemporalType temporalType) {
	super.setParameter(position, value, temporalType);
	return this;
    }

    @Override
    public TypedQuery<X> setFlushMode(FlushModeType flushMode) {
	this.flushModeType = flushMode;
	return this;
    }

    @Override
    public TypedQuery<X> setLockMode(LockModeType lockMode) {
	// TODO Auto-generated method stub
	return null;
    }

}
