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

import org.minijpa.jpa.db.JdbcEntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.List;

/**
 * @author adamato
 */
public class MiniJpqlQuery extends AbstractQuery {

    private final Logger LOG = LoggerFactory.getLogger(MiniJpqlQuery.class);

    private final String jpqlString;
    private final EntityManager entityManager;

    public MiniJpqlQuery(String jpqlString,
                         EntityManager entityManager,
                         JdbcEntityManager jdbcEntityManager) {
        this.jpqlString = jpqlString;
        this.entityManager = entityManager;
        this.jdbcEntityManager = jdbcEntityManager;
    }

    public String getJpqlString() {
        return jpqlString;
    }

    @Override
    public List getResultList() {
        List<?> list = null;
        try {
            if (flushModeType == FlushModeType.AUTO)
                jdbcEntityManager.flush();

            list = jdbcEntityManager.selectJpql(jpqlString, getParameterMap(), getHints(), null);
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
    public Object getSingleResult() {
        List<?> list = null;
        try {
            if (flushModeType == FlushModeType.AUTO)
                jdbcEntityManager.flush();

            list = jdbcEntityManager.selectJpql(jpqlString, getParameterMap(), getHints(), null);
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

        return list.get(0);
    }


    @Override
    public int executeUpdate() {
        if (!entityManager.getTransaction().isActive())
            throw new TransactionRequiredException("Update requires an active transaction");

        // TODO implementation missing
        try {
            return jdbcEntityManager.update(jpqlString, this);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new PersistenceException(e.getMessage());
        }
    }

}
