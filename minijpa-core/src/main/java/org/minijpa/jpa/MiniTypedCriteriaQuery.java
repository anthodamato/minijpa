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

import java.util.*;

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

public class MiniTypedCriteriaQuery<X> extends AbstractTypedQuery<X> {

    private final Logger LOG = LoggerFactory.getLogger(MiniTypedCriteriaQuery.class);
    private final CriteriaQuery<?> criteriaQuery;

    public MiniTypedCriteriaQuery(CriteriaQuery<?> criteriaQuery, JdbcEntityManager jdbcCriteriaEntityManager) {
        super(jdbcCriteriaEntityManager);
        this.criteriaQuery = criteriaQuery;
    }

    public CriteriaQuery<?> getCriteriaQuery() {
        return criteriaQuery;
    }

    @Override
    public List<X> getResultList() {
        try {
            if (flushModeType == FlushModeType.AUTO)
                jdbcEntityManager.flush();

            return (List<X>) jdbcEntityManager.selectCriteriaQuery(this, criteriaQuery);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new PersistenceException(e.getMessage());
        }
    }


    @Override
    public X getSingleResult() {
        List<?> list = null;
        try {
            if (flushModeType == FlushModeType.AUTO)
                jdbcEntityManager.flush();

            list = jdbcEntityManager.selectCriteriaQuery(this, criteriaQuery);
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

}
