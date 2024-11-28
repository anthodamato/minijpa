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
import org.minijpa.jpa.db.AttributeFetchParameter;
import org.minijpa.jpa.db.PkGeneration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class EmbeddedPk implements Pk {

    private static final Logger LOG = LoggerFactory.getLogger(EmbeddedPk.class);
    private final MetaEntity entity;
    private final PkGeneration pkGeneration = new PkGeneration();

    public EmbeddedPk(MetaEntity entity) {
        this.entity = entity;
    }

    @Override
    public PkGeneration getPkGeneration() {
        return pkGeneration;
    }

    @Override
    public boolean isEmbedded() {
        return true;
    }

    @Override
    public boolean isComposite() {
        return entity.getAttributes().size() > 1;
    }

    @Override
    public MetaAttribute getAttribute() {
        return null;
    }

    @Override
    public List<MetaAttribute> getAttributes() {
        return entity.getBasicAttributes();
    }

    @Override
    public Class<?> getType() {
        return entity.getEntityClass();
    }

    @Override
    public String getName() {
        return entity.getName();
    }

    @Override
    public Method getReadMethod() {
        return entity.getReadMethod();
    }

    @Override
    public Method getWriteMethod() {
        return entity.getWriteMethod();
    }

    @Override
    public Object buildValue(ModelValueArray<FetchParameter> modelValueArray) throws Exception {
        Object pkObject = getType().getConstructor().newInstance();
        buildPK(modelValueArray, getAttributes(), pkObject);
        return pkObject;
    }

    private void buildPK(
            ModelValueArray<FetchParameter> modelValueArray,
            List<MetaAttribute> attributes,
            Object pkObject) throws Exception {
        for (MetaAttribute a : attributes) {
            int index = indexOfAttribute(modelValueArray, a);
            LOG.debug("buildPK: index={}", index);
            if (index == -1) {
                throw new IllegalArgumentException("Column '" + a.getColumnName() + "' is missing");
            }

            a.getWriteMethod().invoke(pkObject, modelValueArray.getValue(index));
        }
    }


    private int indexOfAttribute(
            ModelValueArray<FetchParameter> modelValueArray,
            MetaAttribute attribute) {
        LOG.debug("indexOfAttribute: attribute={}", attribute);
        for (int i = 0; i < modelValueArray.size(); ++i) {
            LOG.debug("indexOfAttribute: ((AttributeFetchParameter) modelValueArray.getModel(i)).getAttribute()={}", ((AttributeFetchParameter) modelValueArray.getModel(i)).getAttribute());
            if (((AttributeFetchParameter) modelValueArray.getModel(i)).getAttribute() == attribute) {
                return i;
            }
        }

        return -1;
    }

}
