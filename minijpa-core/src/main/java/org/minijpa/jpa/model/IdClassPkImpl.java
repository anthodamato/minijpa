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
import org.minijpa.jpa.MetaEntityHelper;
import org.minijpa.jpa.db.AttributeFetchParameter;
import org.minijpa.jpa.db.PkGeneration;
import org.minijpa.metadata.BeanUtil;
import org.minijpa.metadata.enhancer.EnhAttribute;
import org.minijpa.metadata.enhancer.IdClassPropertyData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class IdClassPkImpl implements IdClassPk {

    private static final Logger log = LoggerFactory.getLogger(IdClassPkImpl.class);
    private final List<MetaAttribute> attributes;
    private final Class<?> pkClass;
    private RelationshipMetaAttribute relationshipMetaAttribute;
    private IdClassPropertyData idClassPropertyData;
    private final PkGeneration pkGeneration = new PkGeneration();

    public IdClassPkImpl(
            List<MetaAttribute> attributes,
            Class<?> pkClass,
            RelationshipMetaAttribute relationshipMetaAttribute,
            IdClassPropertyData idClassPropertyData) {
        this.attributes = attributes;
        this.pkClass = pkClass;
        this.relationshipMetaAttribute = relationshipMetaAttribute;
        this.idClassPropertyData = idClassPropertyData;
    }

    @Override
    public boolean isIdClass() {
        return true;
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
        if (attributes.size() > 1)
            return true;

        if (attributes.size() == 1 && relationshipMetaAttribute != null)
            return true;

        return false;
    }

    @Override
    public MetaAttribute getAttribute() {
        return null;
    }

    @Override
    public List<MetaAttribute> getAttributes() {
        return attributes;
    }

    @Override
    public RelationshipMetaAttribute getRelationshipMetaAttribute() {
        return relationshipMetaAttribute;
    }

    @Override
    public Class<?> getType() {
        return pkClass;
    }

    @Override
    public String getName() {
        return "IdClass";
    }

    @Override
    public Object readValue(Object entityInstance) throws Exception {
        log.debug("Id Class Pk -> Reading Value -> Entity Instance = {}", entityInstance);
        Object pkObject;
        try {
            pkObject = pkClass.getConstructor().newInstance();
        } catch (Exception e) {
            log.error("Id Class Pk -> Reading Value -> Exception = {}", e.getMessage());
            throw e;
        }

        for (AbstractMetaAttribute abstractMetaAttribute : attributes) {
            Object attributeValue = abstractMetaAttribute.getReadMethod().invoke(entityInstance);
            if (abstractMetaAttribute instanceof MetaAttribute) {
                findAndSetPropertyValue(abstractMetaAttribute.name, pkObject, attributeValue, abstractMetaAttribute.getType());
            }
        }

        log.debug("Id Class Pk -> Reading Value -> Relationship Attribute = {}", relationshipMetaAttribute);
        if (relationshipMetaAttribute != null) {
            Object attributeValue = relationshipMetaAttribute.getReadMethod().invoke(entityInstance);
            log.debug("Id Class Pk -> Reading Value -> Relationship Attribute Value = {}", attributeValue);
            if (attributeValue == null)
                throw new IllegalStateException("Relationship attribute value, in composite primary key, is null");

            MetaEntity entity = relationshipMetaAttribute.getRelationship().getAttributeType();
            log.debug("Id Class Pk -> Reading Value -> Relationship Attribute Type = {}", entity);
            Object idValue = entity.getId().readValue(attributeValue);
            log.debug("Id Class Pk -> Reading Value -> Relationship Attribute Id Value = {}", idValue);
            findAndSetForeignKeyValue(relationshipMetaAttribute.name, pkObject, idValue, entity.getId());
        }

        log.debug("Id Class Pk -> Reading Value -> Pk Object = {}", pkObject);
        return pkObject;
    }


    @Override
    public void writeValue(Object entityInstance, Object pkValue) throws Exception {
        log.debug("Id Class Pk -> Writing Value -> Entity Instance = {}", entityInstance);
        log.debug("Id Class Pk -> Writing Value -> Pk value = {}", pkValue);
        for (AbstractMetaAttribute a : attributes) {
            if (a instanceof MetaAttribute) {
                Method method = pkValue.getClass().getMethod(a.getReadMethod().getName());
                Object value = method.invoke(pkValue);
                a.getWriteMethod().invoke(entityInstance, value);
            }
        }
    }


    @Override
    public Object buildValue(ModelValueArray<FetchParameter> modelValueArray) throws Exception {
        Object pkObject = getType().getConstructor().newInstance();
        buildPk(modelValueArray, pkObject);
        return pkObject;
    }


    private void buildPk(
            ModelValueArray<FetchParameter> modelValueArray,
            Object pkObject) throws Exception {
        modelValueArray.getValues().forEach(v -> log.debug("Id Class Pk -> Build Pk -> Value = {}", v));
        modelValueArray.getModels().forEach(f -> log.debug("Id Class Pk -> Build Pk -> Model = {}", f));
        log.debug("Id Class Pk -> Build Pk -> Relationship Attribute = {}", relationshipMetaAttribute);
        if (relationshipMetaAttribute != null) {
            Pk foreignPk = relationshipMetaAttribute.getRelationship().getAttributeType().getId();
            log.debug("Id Class Pk -> Build Pk -> Foreign Key = {}", foreignPk);
            if (foreignPk.isComposite()) {
                Object foreignKeyValue = foreignPk.buildValue(modelValueArray);
                log.debug("Id Class Pk -> Build Pk -> Composite Foreign Key Value = {}", foreignKeyValue);
                Method method = pkObject.getClass().getMethod(relationshipMetaAttribute.getWriteMethod().getName(), foreignKeyValue.getClass());
                method.invoke(pkObject, foreignKeyValue);
            } else {
                AbstractMetaAttribute key = foreignPk.getAttribute();
                int index = indexOfAttribute(modelValueArray, key);
                if (index != -1) {
                    Class<?> type = relationshipMetaAttribute.getRelationship().getAttributeType().getId().getAttribute().getType();
                    Method method = pkObject.getClass().getMethod(relationshipMetaAttribute.getWriteMethod().getName(), type);
                    Object value = modelValueArray.getValue(index);
                    log.debug("Id Class Pk -> Build Pk -> Basic Foreign Key Value = {}", value);
                    method.invoke(pkObject, value);
                }
            }
        }

        log.debug("Id Class Pk -> Build Pk -> Pk Object = {}", pkObject);
        for (AbstractMetaAttribute a : attributes) {
            int index = indexOfAttribute(modelValueArray, a);
            if (index == -1) {
                throw new IllegalArgumentException("Column '" + a.getColumnName() + "' is missing");
            }

            Object value = modelValueArray.getValue(index);
            log.debug("Id Class Pk -> Build Pk -> Attribute Value = {}", value);
            if (a instanceof MetaAttribute) {
                Method method = pkObject.getClass().getMethod(a.getWriteMethod().getName(), a.getReadMethod().getReturnType());
                method.invoke(pkObject, value);
            }
        }
    }


    @Override
    public void expand(Object value, ModelValueArray<AbstractMetaAttribute> modelValueArray) throws Exception {
        for (AbstractMetaAttribute a : getAttributes()) {
            if (a instanceof RelationshipMetaAttribute)
                continue;

            Object v = findAndGetPropertyValue(a.name, value);
            modelValueArray.add(a, v);
        }
    }


    @Override
    public List<QueryParameter> queryParameters(Object value) throws Exception {
        ModelValueArray<AbstractMetaAttribute> modelValueArray = new ModelValueArray<>();
        expand(value, modelValueArray);
        List<QueryParameter> queryParameters = new ArrayList<>(MetaEntityHelper.convertAVToQP(modelValueArray));
        return queryParameters;
    }


    private int indexOfAttribute(
            ModelValueArray<FetchParameter> modelValueArray,
            AbstractMetaAttribute attribute) {
        for (int i = 0; i < modelValueArray.size(); ++i) {
            if (attribute instanceof MetaAttribute) {
                if (((AttributeFetchParameter) modelValueArray.getModel(i)).getAttribute() == attribute) {
                    return i;
                }
            } else if (attribute instanceof RelationshipMetaAttribute) {
                RelationshipMetaAttribute relationshipMetaAttribute = (RelationshipMetaAttribute) attribute;
                AbstractMetaAttribute abstractMetaAttribute = relationshipMetaAttribute.getRelationship().getAttributeType().getId().getAttribute();
                if (((AttributeFetchParameter) modelValueArray.getModel(i)).getAttribute() == abstractMetaAttribute) {
                    return i;
                }
            }
        }

        return -1;
    }


    private void findAndSetPropertyValue(
            String propertyName,
            Object pkInstance,
            Object value,
            Class<?> valueClass) throws Exception {
        Method method = pkClass.getDeclaredMethod(BeanUtil.getSetterMethodName(propertyName), valueClass);
        method.invoke(pkInstance, value);
    }


    private void findAndSetForeignKeyValue(
            String propertyName,
            Object pkInstance,
            Object value,
            Pk foreignKey) throws Exception {
        if (foreignKey.isComposite()) {
            try {
                Method method = pkClass.getMethod(BeanUtil.getSetterMethodName(propertyName), value.getClass());
                method.invoke(pkInstance, value);
            } catch (Exception e) {
                log.error("Id Class Pk -> Setting Foreign Key Value -> Ex Class = {}", e.getClass());
                throw e;
            }

            try {
                Method method = pkClass.getDeclaredMethod(BeanUtil.getSetterMethodName(propertyName), value.getClass());
                method.invoke(pkInstance, value);
            } catch (Exception e) {
                log.error("Id Class Pk -> Setting Foreign Key Value -> Ex Class = {}", e.getClass());
                throw e;
            }
        } else {
            Method method = pkClass.getDeclaredMethod(BeanUtil.getSetterMethodName(propertyName), foreignKey.getAttribute().getType());
            method.invoke(pkInstance, value);
        }
    }


    private Object findAndGetPropertyValue(
            String propertyName,
            Object pkInstance) throws Exception {
        Method method = pkInstance.getClass().getDeclaredMethod(BeanUtil.getGetterMethodName(propertyName));
        return method.invoke(pkInstance);
    }


    @Override
    public Object checkClass(Object pkValue) throws Exception {
        if (pkValue.getClass() == pkClass)
            return pkValue;

        Object pkObject = pkClass.getConstructor().newInstance();
        assignAttributes(pkObject, pkValue, idClassPropertyData);
        return pkObject;
    }


    private void assignAttributes(
            Object pkObject,
            Object oldPkValue,
            IdClassPropertyData idClassPropertyData) throws Exception {
        for (EnhAttribute enhAttribute : idClassPropertyData.getEnhAttributes()) {
            Method getMethod = oldPkValue.getClass().getDeclaredMethod(enhAttribute.getGetMethod());
            Object value = getMethod.invoke(oldPkValue);
            if (value == null)
                continue;

            IdClassPropertyData nested = idClassPropertyData.getNested();
            if (nested != null && nested.getClassName().equals(value.getClass().getName())) {
                Object pkNestedObject = nested.getClassType().getConstructor().newInstance();

                Method getNestedMethod = pkObject.getClass().getDeclaredMethod(enhAttribute.getGetMethod());
                Method setMethod = pkObject.getClass().getDeclaredMethod(enhAttribute.getSetMethod(), getNestedMethod.getReturnType());
                setMethod.invoke(pkObject, pkNestedObject);

                assignAttributes(pkNestedObject, value, nested);
            } else {
                Method setMethod = pkObject.getClass().getDeclaredMethod(enhAttribute.getSetMethod(), getMethod.getReturnType());
                setMethod.invoke(pkObject, value);
            }
        }
    }
}
