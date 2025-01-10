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
package org.minijpa.jpa.model.relationship;

import org.minijpa.jdbc.ModelValueArray;
import org.minijpa.jdbc.QueryParameter;
import org.minijpa.jpa.model.MetaAttribute;
import org.minijpa.jpa.model.Pk;
import org.minijpa.jpa.model.RelationshipMetaAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public interface JoinColumnMapping {
    Logger log = LoggerFactory.getLogger(JoinColumnMapping.class);

    RelationshipMetaAttribute getAttribute();

    boolean isComposite();

    int size();

    JoinColumnAttribute get(int index);

    JoinColumnAttribute get();

    List<JoinColumnAttribute> getJoinColumnAttributes();

    Pk getForeignKey();

    boolean isLazy();

    default void expand(
            Object value,
            ModelValueArray<JoinColumnAttribute> modelValueArray) throws Exception {
        for (int i = 0; i < size(); ++i) {
            JoinColumnAttribute joinColumnAttribute = get(i);
            MetaAttribute a = get(i).getForeignKeyAttribute();
            log.debug("expand: value={}", value);

            Method[] methods = value.getClass().getDeclaredMethods();
            for (Method method : methods) {
                log.debug("expand: method.getName()={}", method.getName());
            }

            Method method = value.getClass().getMethod(a.getReadMethod().getName());
            log.debug("expand: a={}", a);
            log.debug("expand: a.getReadMethod()={}", a.getReadMethod());
//            Object v = a.getReadMethod().invoke(value);
            Object v = method.invoke(value);
            log.debug("expand: v={}", v);
            modelValueArray.add(joinColumnAttribute, v);
        }
    }

    default List<QueryParameter> queryParameters(Object value) throws Exception {
        List<QueryParameter> list = new ArrayList<>();
        ModelValueArray<JoinColumnAttribute> modelValueArray = new ModelValueArray<>();
//        expand(joinColumnMapping, value, modelValueArray);
        expand(value, modelValueArray);
        for (int i = 0; i < modelValueArray.size(); ++i) {
            JoinColumnAttribute joinColumnAttribute = modelValueArray.getModel(i);
//            MetaAttribute attribute = joinColumnAttribute.getForeignKeyAttribute();
//            LOG.debug("convertAVToQP: joinColumnAttribute.getColumnName()={}",
//                    joinColumnAttribute.getColumnName());
//            QueryParameter queryParameter = new QueryParameter(joinColumnAttribute.getColumnName(),
//                    modelValueArray.getValue(i), attribute.getSqlType(), attribute.getAttributeMapper());
            QueryParameter queryParameter = joinColumnAttribute.queryParameter(modelValueArray.getValue(i));
            list.add(queryParameter);
        }

        return list;
    }
}
