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
import java.util.Calendar;
import java.util.Date;

public abstract class AbstractTypedQuery<X> extends AbstractQuery implements TypedQuery<X> {

    private final Logger LOG = LoggerFactory.getLogger(AbstractTypedQuery.class);

    public AbstractTypedQuery(JdbcEntityManager jdbcCriteriaEntityManager) {
        super();
        this.jdbcEntityManager = jdbcCriteriaEntityManager;
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
    public boolean isBound(Parameter<?> param) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
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
        super.setHint(hintName, value);
        return this;
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
        super.setLockMode(lockMode);
        return this;
    }

}
