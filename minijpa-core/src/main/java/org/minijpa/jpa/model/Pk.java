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
package org.minijpa.jpa.model;

import java.lang.reflect.Method;
import java.util.List;

import org.minijpa.jdbc.FetchParameter;
import org.minijpa.jdbc.ModelValueArray;
import org.minijpa.jdbc.QueryParameter;
import org.minijpa.jpa.db.PkGeneration;

/**
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public interface Pk {

    PkGeneration getPkGeneration();

    boolean isComposite();

    boolean isEmbedded();

    default boolean isIdClass() {
        return false;
    }

    MetaAttribute getAttribute();

    List<MetaAttribute> getAttributes();

    Class<?> getType();

    String getName();

    Object buildValue(ModelValueArray<FetchParameter> modelValueArray) throws Exception;

    List<QueryParameter> queryParameters(Object value) throws Exception;

    void expand(
            Object value,
            ModelValueArray<AbstractMetaAttribute> modelValueArray) throws Exception;


    Object readValue(Object entityInstance) throws Exception;


    void writeValue(Object entityInstance, Object value) throws Exception;


    /**
     * Converts the 'value' read from a resultSet to an object of class returned by
     * <code>getType</code>.
     * <p>
     * This method is called only to convert the generated key of an identity column.
     *
     * @param value returned by the result set
     * @return the primary key value
     */
    default Object convertGeneratedKey(Object value) {
        return ((Number) value).longValue();
    }

    default Object checkClass(Object pkValue) throws Exception {
        return pkValue;
    }
}
