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
import org.minijpa.jpa.db.namedquery.MiniNamedQueryMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author adamato
 */
public class MiniNamedQuery extends AbstractQuery {

    private final Logger log = LoggerFactory.getLogger(MiniNamedQuery.class);

    private final MiniNamedQueryMapping miniNamedQueryMapping;
    private final EntityManager entityManager;

    public MiniNamedQuery(MiniNamedQueryMapping miniNamedQueryMapping,
                          EntityManager entityManager,
                          JdbcEntityManager jdbcEntityManager) {
        this.miniNamedQueryMapping = miniNamedQueryMapping;
        this.entityManager = entityManager;
        this.jdbcEntityManager = jdbcEntityManager;
    }


    private Map<String, Object> buildHints() {
        if (miniNamedQueryMapping.getHints() == null)
            return getHints();

        Map<String, Object> map = new HashMap<>(getHints());
        map.putAll(miniNamedQueryMapping.getHints());
        return map;
    }

    @Override
    public List getResultList() {
        try {
            if (flushModeType == FlushModeType.AUTO)
                jdbcEntityManager.flush();

            return jdbcEntityManager.selectJpql(
                    miniNamedQueryMapping.getStatementParameters(),
                    getParameterMap(),
                    buildHints(),
                    miniNamedQueryMapping.getLockType(),
                    null);
        } catch (RuntimeException e) {
            log.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new PersistenceException(e.getMessage());
        }
    }

    @Override
    public Object getSingleResult() {
        List<?> list = null;
        try {
            if (flushModeType == FlushModeType.AUTO)
                jdbcEntityManager.flush();

            list = jdbcEntityManager.selectJpql(
                    miniNamedQueryMapping.getStatementParameters(),
                    getParameterMap(),
                    buildHints(),
                    miniNamedQueryMapping.getLockType(),
                    null);
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

        return list.get(0);
    }

    @Override
    public int executeUpdate() {
        if (!entityManager.getTransaction().isActive())
            throw new TransactionRequiredException("Update requires an active transaction");

        try {
            // TODO implementation missing
            return 0;
//            return jdbcEntityManager.update(jpqlString, this);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new PersistenceException(e.getMessage());
        }
    }

}
