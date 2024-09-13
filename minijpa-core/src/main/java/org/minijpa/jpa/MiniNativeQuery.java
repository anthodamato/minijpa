/*
 * Copyright 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.minijpa.jpa;

import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceException;
import javax.persistence.TransactionRequiredException;

import org.minijpa.jpa.db.JdbcEntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author adamato
 */
public class MiniNativeQuery extends AbstractQuery {

    private final Logger LOG = LoggerFactory.getLogger(MiniNativeQuery.class);

    private final String sqlString;
    private final Class<?> resultClass;
    private final String resultSetMapping;
    private final EntityManager entityManager;

    public MiniNativeQuery(
            String sqlString,
            Class<?> resultClass,
            String resultSetMapping,
            EntityManager entityManager,
            JdbcEntityManager jdbcEntityManager) {
        this.sqlString = sqlString;
        this.resultClass = resultClass;
        this.resultSetMapping = resultSetMapping;
        this.entityManager = entityManager;
        this.jdbcEntityManager = jdbcEntityManager;
    }

    public String getSqlString() {
        return sqlString;
    }

    public Class<?> getResultClass() {
        return resultClass;
    }

    public String getResultSetMapping() {
        return resultSetMapping;
    }

    @Override
    public List getResultList() {
        List<?> list;
        try {
            if (flushModeType == FlushModeType.AUTO)
                jdbcEntityManager.flush();

            list = jdbcEntityManager.selectNative(this);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new PersistenceException(e.getMessage());
        }

        return list;
    }

    @Override
    public Object getSingleResult() {
        List<?> list = null;
        try {
            if (flushModeType == FlushModeType.AUTO)
                jdbcEntityManager.flush();

            list = jdbcEntityManager.selectNative(this);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new PersistenceException(e.getMessage());
        }

        if (list.isEmpty())
            throw new NoResultException("No result to return");

        if (list.size() > 1)
            throw new NonUniqueResultException("More than one result to return");

        return list.get(0);
    }

    @Override
    public int executeUpdate() {
        if (!entityManager.getTransaction().isActive())
            throw new TransactionRequiredException("Update requires an active transaction");

        try {
            return jdbcEntityManager.update(sqlString, this);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new PersistenceException(e.getMessage());
        }
    }

}
