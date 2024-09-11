package org.minijpa.jpa;

import org.minijpa.jpa.db.JdbcEntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.FlushModeType;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceException;
import java.util.List;

public class MiniJpqlTypedQuery<T> extends AbstractTypedQuery<T> {
    private final Logger LOG = LoggerFactory.getLogger(MiniJpqlTypedQuery.class);
    private final String jpqlString;
    private final Class<T> resultClass;

    public MiniJpqlTypedQuery(
            JdbcEntityManager jdbcCriteriaEntityManager,
            String jpqlString,
            Class<T> resultClass) {
        super(jdbcCriteriaEntityManager);
        this.jpqlString = jpqlString;
        this.resultClass = resultClass;
    }

    public String getJpqlString() {
        return jpqlString;
    }

    @Override
    public List<T> getResultList() {
        List<T> list = null;
        try {
            if (flushModeType == FlushModeType.AUTO)
                jdbcEntityManager.flush();

            list = (List<T>) jdbcEntityManager.selectJpql(jpqlString, getParameterMap(), getHints(), resultClass);
        } catch (RuntimeException e) {
            LOG.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new PersistenceException(e.getMessage());
        }

        return list;
    }

    @Override
    public T getSingleResult() {
        List<?> list = null;
        try {
            if (flushModeType == FlushModeType.AUTO)
                jdbcEntityManager.flush();

            list = jdbcEntityManager.selectJpql(jpqlString, getParameterMap(), getHints(), resultClass);
        } catch (RuntimeException e) {
            LOG.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new PersistenceException(e.getMessage());
        }

        if (list.isEmpty())
            throw new NoResultException("No result to return");

        if (list.size() > 1)
            throw new NonUniqueResultException("More than one result to return");

        return (T) list.get(0);
    }
}
