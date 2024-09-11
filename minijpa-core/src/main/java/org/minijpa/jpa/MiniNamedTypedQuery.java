package org.minijpa.jpa;

import org.minijpa.jpa.db.JdbcEntityManager;
import org.minijpa.jpa.db.namedquery.MiniNamedQueryMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.FlushModeType;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MiniNamedTypedQuery<T> extends AbstractTypedQuery<T> {
    private final Logger log = LoggerFactory.getLogger(MiniNamedTypedQuery.class);
    private final MiniNamedQueryMapping miniNamedQueryMapping;
    private final Class<T> resultClass;

    public MiniNamedTypedQuery(
            JdbcEntityManager jdbcCriteriaEntityManager,
            MiniNamedQueryMapping miniNamedQueryMapping,
            Class<T> resultClass) {
        super(jdbcCriteriaEntityManager);
        this.miniNamedQueryMapping = miniNamedQueryMapping;
        this.resultClass = resultClass;
    }


    private Map<String, Object> buildHints() {
        if (miniNamedQueryMapping.getHints() == null)
            return getHints();

        Map<String, Object> map = new HashMap<>(getHints());
        map.putAll(miniNamedQueryMapping.getHints());
        return map;
    }


    @Override
    public List<T> getResultList() {
        try {
            if (flushModeType == FlushModeType.AUTO)
                jdbcEntityManager.flush();

            return (List<T>) jdbcEntityManager.selectJpql(
                    miniNamedQueryMapping.getStatementParameters(),
                    getParameterMap(),
                    buildHints(),
                    miniNamedQueryMapping.getLockType(),
                    resultClass);
        } catch (RuntimeException e) {
            log.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new PersistenceException(e.getMessage());
        }
    }

    @Override
    public T getSingleResult() {
        List<?> list = null;
        try {
            if (flushModeType == FlushModeType.AUTO)
                jdbcEntityManager.flush();

            list = jdbcEntityManager.selectJpql(
                    miniNamedQueryMapping.getStatementParameters(),
                    getParameterMap(),
                    buildHints(),
                    miniNamedQueryMapping.getLockType(),
                    resultClass);
        } catch (RuntimeException e) {
            log.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new PersistenceException(e.getMessage());
        }

        if (list.isEmpty())
            throw new NoResultException("No result to return");

        if (list.size() > 1)
            throw new NonUniqueResultException("More than one result to return");

        return (T) list.get(0);
    }
}
