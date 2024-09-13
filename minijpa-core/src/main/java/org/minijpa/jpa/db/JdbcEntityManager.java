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
package org.minijpa.jpa.db;

import java.util.List;
import java.util.Map;

import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;

import org.minijpa.jpa.DeleteQuery;
import org.minijpa.jpa.MiniNativeQuery;
import org.minijpa.jpa.UpdateQuery;
import org.minijpa.jpa.model.MetaEntity;

public interface JdbcEntityManager {

    public void persist(MetaEntity entity, Object entityInstance, MiniFlushMode tinyFlushMode) throws Exception;

    public void flush() throws Exception;

    public List<?> selectCriteriaQuery(Query query, CriteriaQuery criteriaQuery) throws Exception;

    public List<?> selectJpql(
            StatementParameters statementParameters,
            Map<Parameter<?>, Object> parameterMap,
            Map<String, Object> hints,
            LockType lockType,
            Class<?> resultClass);

    public List<?> selectJpql(String jpqlStatement,
                              Map<Parameter<?>, Object> parameterMap,
                              Map<String, Object> hints,
                              Class<?> resultClass) throws Exception;

    public List<?> selectNative(MiniNativeQuery query) throws Exception;

    public int update(String sqlString, Query query) throws Exception;

    public int update(UpdateQuery updateQuery) throws Exception;

    public int delete(DeleteQuery deleteQuery) throws Exception;

    public void remove(Object entity, MiniFlushMode miniFlushMode) throws Exception;

    public void detach(Object entity) throws Exception;
}
