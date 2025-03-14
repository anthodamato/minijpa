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

import org.minijpa.jdbc.FetchParameter;
import org.minijpa.jdbc.ModelValueArray;
import org.minijpa.jdbc.QueryParameter;
import org.minijpa.jpa.db.AttributeFetchParameter;
import org.minijpa.jpa.db.PkGeneration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class BasicAttributePk implements Pk {
    private static final Logger LOG = LoggerFactory.getLogger(BasicAttributePk.class);

    private final MetaAttribute attribute;
    private final PkGeneration pkGeneration;
    private final List<MetaAttribute> attributes;

    public BasicAttributePk(MetaAttribute attribute, PkGeneration pkGeneration) {
        this.attribute = attribute;
        this.pkGeneration = pkGeneration;
        this.attributes = List.of(attribute);
    }

    @Override
    public PkGeneration getPkGeneration() {
        return pkGeneration;
    }

    @Override
    public boolean isEmbedded() {
        return false;
    }

    @Override
    public boolean isComposite() {
        return false;
    }

    @Override
    public MetaAttribute getAttribute() {
        return attribute;
    }

    @Override
    public List<MetaAttribute> getAttributes() {
        return attributes;
    }

    @Override
    public Class<?> getType() {
        return attribute.getType();
    }

    @Override
    public String getName() {
        return attribute.getName();
    }

    @Override
    public Object readValue(Object entityInstance) throws Exception {
        return attribute.getReadMethod().invoke(entityInstance);
    }

    @Override
    public void writeValue(Object entityInstance, Object value) throws Exception {
        attribute.getWriteMethod().invoke(entityInstance, value);
    }

    @Override
    public Object buildValue(ModelValueArray<FetchParameter> modelValueArray) throws Exception {
        int index = indexOfAttribute(modelValueArray, getAttribute());
        if (index == -1) {
            throw new IllegalArgumentException(
                    "Column '" + getAttribute().getColumnName() + "' not found");
        }

        return modelValueArray.getValue(index);
    }

    @Override
    public void expand(Object value, ModelValueArray<AbstractMetaAttribute> modelValueArray) throws Exception {
        modelValueArray.add(getAttribute(), value);
    }

    @Override
    public List<QueryParameter> queryParameters(Object value) throws Exception {
        List<QueryParameter> list = new ArrayList<>();
        list.add(getAttribute().queryParameter(value));
        return list;
    }

    private int indexOfAttribute(
            ModelValueArray<FetchParameter> modelValueArray,
            AbstractMetaAttribute attribute) {
        for (int i = 0; i < modelValueArray.size(); ++i) {
            if (((AttributeFetchParameter) modelValueArray.getModel(i)).getAttribute() == attribute) {
                return i;
            }
        }

        return -1;
    }

}
