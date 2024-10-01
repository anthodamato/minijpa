package org.minijpa.jpa;

import org.minijpa.jpa.db.JdbcEntityManager;
import org.minijpa.jpa.db.namedquery.MiniNamedQueryMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MiniNativeTypedQuery<T> extends AbstractTypedQuery<T> implements NativeQuery {
    private final Logger log = LoggerFactory.getLogger(MiniNativeTypedQuery.class);
    private final String sqlString;
    private final Class<?> resultClass;
    private final String resultSetMapping;
    private final EntityManager entityManager;
    private final Map<String, Object> additionalHints;

    public MiniNativeTypedQuery(
            String sqlString,
            Class<?> resultClass,
            String resultSetMapping,
            Map<String, Object> additionalHints,
            EntityManager entityManager,
            JdbcEntityManager jdbcEntityManager) {
        super(jdbcEntityManager);
        this.sqlString = sqlString;
        this.resultClass = resultClass;
        this.additionalHints = additionalHints;
        this.resultSetMapping = resultSetMapping;
        this.entityManager = entityManager;
    }

    @Override
    public String getSql() {
        return sqlString;
    }

    @Override
    public Class<?> getResultClass() {
        return resultClass;
    }

    @Override
    public String getResultSetMapping() {
        return resultSetMapping;
    }

    @Override
    public Map<String, Object> getAdditionalHints() {
        return additionalHints;
    }

    private Map<String, Object> buildHints() {
        if (additionalHints == null || additionalHints.isEmpty())
            return getHints();

        Map<String, Object> map = new HashMap<>(getHints());
        map.putAll(additionalHints);
        return map;
    }


    @Override
    public List<T> getResultList() {
        List<?> list;
        try {
            if (flushModeType == FlushModeType.AUTO)
                jdbcEntityManager.flush();

            list = jdbcEntityManager.selectNative(this);
        } catch (RuntimeException e) {
            log.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new PersistenceException(e.getMessage());
        }

        return (List<T>) list;
    }

    @Override
    public T getSingleResult() {
        List<?> list = null;
        try {
            if (flushModeType == FlushModeType.AUTO)
                jdbcEntityManager.flush();

            list = jdbcEntityManager.selectNative(this);
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
