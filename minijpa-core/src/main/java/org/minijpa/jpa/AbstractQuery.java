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

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.minijpa.jpa.db.JdbcEntityManager;

public abstract class AbstractQuery implements Query {

    protected JdbcEntityManager jdbcEntityManager;
    protected FlushModeType flushModeType = FlushModeType.AUTO;
    private final Map<Parameter<?>, Object> parameterValues = new HashMap<>();

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
        parameterValues.put(param, value);
        return this;
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
        MiniParameter<?> parameter = new MiniParameter<>(name, null, null);
        parameterValues.put(parameter, value);
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
        MiniParameter<?> parameter = new MiniParameter<>(null, position, null);
        parameterValues.put(parameter, value);
        return this;
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
        return parameterValues.keySet();
    }

    @Override
    public Parameter<?> getParameter(String name) {
        Optional<Parameter<?>> optional = ParameterUtils.findParameterByName(name, parameterValues);
        if (optional.isEmpty())
            throw new IllegalArgumentException("Parameter '" + name + "' not found");

        return optional.get();
    }

    @Override
    public <T> Parameter<T> getParameter(String name, Class<T> type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Parameter<?> getParameter(int position) {
        Optional<Parameter<?>> optional = ParameterUtils.findParameterByPosition(position, parameterValues);
        if (optional.isEmpty())
            throw new IllegalArgumentException("Parameter at position '" + position + "' not found");

        return optional.get();
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
        if (!parameterValues.containsKey(param))
            throw new IllegalArgumentException("Parameter not found: " + param);

        return (T) parameterValues.get(param);
    }

    @Override
    public Object getParameterValue(String name) {
        Optional<Parameter<?>> optional = ParameterUtils.findParameterByName(name, parameterValues);
        if (optional.isEmpty())
            throw new IllegalArgumentException("Parameter '" + name + "' not found");

        return parameterValues.get(optional.get());
    }

    @Override
    public Object getParameterValue(int position) {
        Optional<Parameter<?>> optional = ParameterUtils.findParameterByPosition(position, parameterValues);
        if (optional.isEmpty())
            throw new IllegalArgumentException("Parameter at position '" + position + "' not found");

        return parameterValues.get(optional.get());
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
