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
        log.debug("readValue: entityInstance={}", entityInstance);
        Object pkObject;
        try {
            pkObject = pkClass.getConstructor().newInstance();
        } catch (Exception e) {
            log.debug("readValue: e={}", e.getMessage());
            log.debug("readValue: e.getClass()={}", e.getClass());
            log.debug("readValue: pkClass.getConstructor()={}", pkClass.getConstructor());
            throw e;
        }

        log.debug("readValue: 1 pkObject={}", pkObject);
        log.debug("readValue: attributes.size()={}", attributes.size());
        for (AbstractMetaAttribute abstractMetaAttribute : attributes) {
            log.debug("readValue: abstractMetaAttribute={}", abstractMetaAttribute);
            Object attributeValue = abstractMetaAttribute.getReadMethod().invoke(entityInstance);
            log.debug("readValue: attributeValue={}", attributeValue);
            if (abstractMetaAttribute instanceof MetaAttribute) {
                findAndSetPropertyValue(abstractMetaAttribute.name, pkObject, attributeValue, abstractMetaAttribute.getType());
            }
        }

        if (relationshipMetaAttribute != null) {
            Object attributeValue = relationshipMetaAttribute.getReadMethod().invoke(entityInstance);
            log.debug("readValue: relationshipMetaAttribute attributeValue={}", attributeValue);
            if (attributeValue == null)
                throw new IllegalStateException("Relationship attribute value, in composite primary key, is null");

            MetaEntity entity = relationshipMetaAttribute.getRelationship().getAttributeType();
            Object idValue = entity.getId().readValue(attributeValue);
            log.debug("readValue: idValue={}", idValue);
            log.debug("readValue: relationshipMetaAttribute.name={}", relationshipMetaAttribute.name);
            findAndSetForeignKeyValue(relationshipMetaAttribute.name, pkObject, idValue, entity.getId());
        }

        log.debug("readValue: 2 pkObject={}", pkObject);
        return pkObject;
    }


    @Override
    public void writeValue(Object entityInstance, Object pkValue) throws Exception {
        log.info("writeValue: entityInstance={}", entityInstance);
        log.info("writeValue: pkValue={}", pkValue);
        for (AbstractMetaAttribute a : attributes) {
            if (a instanceof MetaAttribute) {
                Method method = pkValue.getClass().getMethod(a.getReadMethod().getName());
                log.debug("writeValue: method={}", method);
                Object value = method.invoke(pkValue);
                a.getWriteMethod().invoke(entityInstance, value);
            }
        }
    }


    @Override
    public Object buildValue(ModelValueArray<FetchParameter> modelValueArray) throws Exception {
        Object pkObject = getType().getConstructor().newInstance();
        buildPK(modelValueArray, pkObject);
        return pkObject;
    }


    private void buildPK(
            ModelValueArray<FetchParameter> modelValueArray,
            Object pkObject) throws Exception {
        log.info("buildPK: modelValueArray={}", modelValueArray);
        modelValueArray.getValues().forEach(v -> log.debug("buildPk: v={}", v));
        modelValueArray.getModels().forEach(f -> log.debug("buildPk: f={}", f));
        if (relationshipMetaAttribute != null) {
            log.debug("buildPk: relationshipMetaAttribute.getRelationship().getAttributeType().getId().getAttribute()={}",
                    relationshipMetaAttribute.getRelationship().getAttributeType().getId().getAttribute());

            Pk foreignPk = relationshipMetaAttribute.getRelationship().getAttributeType().getId();
            if (foreignPk.isComposite()) {
                Object foreignKeyValue = foreignPk.buildValue(modelValueArray);
                log.info("buildPK: foreignKeyValue={}", foreignKeyValue);
                log.info("buildPK: relationshipMetaAttribute.getWriteMethod().getName()={}", relationshipMetaAttribute.getWriteMethod().getName());
                Method method = pkObject.getClass().getMethod(relationshipMetaAttribute.getWriteMethod().getName(), foreignKeyValue.getClass());
                log.info("buildPK: method={}", method);
                method.invoke(pkObject, foreignKeyValue);
            } else {
                AbstractMetaAttribute key = foreignPk.getAttribute();
                int index = indexOfAttribute(modelValueArray, key);
                log.debug("buildPK: r index={}", index);
                if (index != -1) {
                    Class<?> type = relationshipMetaAttribute.getRelationship().getAttributeType().getId().getAttribute().getType();
                    Method method = pkObject.getClass().getMethod(relationshipMetaAttribute.getWriteMethod().getName(), type);
                    Object value = modelValueArray.getValue(index);
                    method.invoke(pkObject, value);
                }
            }
        }

        log.debug("buildPK: pkObject={}", pkObject);
        for (AbstractMetaAttribute a : attributes) {
            int index = indexOfAttribute(modelValueArray, a);
            log.debug("buildPK: index={}", index);
            if (index == -1) {
                throw new IllegalArgumentException("Column '" + a.getColumnName() + "' is missing");
            }

            Object value = modelValueArray.getValue(index);
            log.debug("buildPK: value={}", value);
            log.debug("buildPK: value.getClass()={}", value.getClass());
            log.debug("buildPK: a.getWriteMethod().getName()={}", a.getWriteMethod().getName());
            log.debug("buildPK: a.getReadMethod().getReturnType()={}", a.getReadMethod().getReturnType());
//            Method method;
            if (a instanceof MetaAttribute) {
                Method method = pkObject.getClass().getMethod(a.getWriteMethod().getName(), a.getReadMethod().getReturnType());
                log.debug("buildPK: method={}", method);
                method.invoke(pkObject, value);
                log.debug("buildPK: assigned");
            }
//            else {
//                RelationshipMetaAttribute relationshipMetaAttribute = (RelationshipMetaAttribute) a;
//                Class<?> type = relationshipMetaAttribute.getRelationship().getAttributeType().getId().getAttribute().getType();
//                method = pkObject.getClass().getMethod(a.getWriteMethod().getName(), type);
//            }
//
//            log.debug("buildPK: method={}", method);
//            method.invoke(pkObject, value);
//            log.debug("buildPK: assigned");
        }

//        if (relationshipMetaAttribute != null) {
//            Class<?> type = relationshipMetaAttribute.getRelationship().getAttributeType().getId().getAttribute().getType();
//            Method method = pkObject.getClass().getMethod(relationshipMetaAttribute.getWriteMethod().getName(), type);
//            method.invoke(pkObject, value);
//        }
    }


    @Override
    public void expand(Object value, ModelValueArray<AbstractMetaAttribute> modelValueArray) throws Exception {
        for (AbstractMetaAttribute a : getAttributes()) {
            if (a instanceof RelationshipMetaAttribute)
                continue;

            log.debug("expand: a={}", a);
            log.debug("expand: a.getReadMethod()={}", a.getReadMethod());
            log.debug("expand: value={}", value);
            Object v = findAndGetPropertyValue(a.name, value);
            modelValueArray.add(a, v);
        }
    }


    @Override
    public List<QueryParameter> queryParameters(Object value) throws Exception {
        ModelValueArray<AbstractMetaAttribute> modelValueArray = new ModelValueArray<>();
        expand(value, modelValueArray);
        log.debug("queryParameters: modelValueArray={}", modelValueArray);
        List<QueryParameter> queryParameters = new ArrayList<>(MetaEntityHelper.convertAVToQP(modelValueArray));
        log.debug("queryParameters: queryParameters={}", queryParameters);
        return queryParameters;
    }


    private int indexOfAttribute(
            ModelValueArray<FetchParameter> modelValueArray,
            AbstractMetaAttribute attribute) {
        log.debug("indexOfAttribute: attribute={}", attribute);
        for (int i = 0; i < modelValueArray.size(); ++i) {
            if (attribute instanceof MetaAttribute) {
                log.debug("indexOfAttribute: {} ((AttributeFetchParameter) modelValueArray.getModel(i)).getAttribute()={}", i, ((AttributeFetchParameter) modelValueArray.getModel(i)).getAttribute());
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
        log.debug("findAndSetPropertyValue: propertyName={}", propertyName);
        log.debug("findAndSetPropertyValue: valueClass={}", valueClass);
        Method method = pkClass.getDeclaredMethod(BeanUtil.getSetterMethodName(propertyName), valueClass);
        log.debug("findAndSetPropertyValue: method={}", method);
        method.invoke(pkInstance, value);
        log.debug("findAndSetPropertyValue: end");
    }


    private void findAndSetForeignKeyValue(
            String propertyName,
            Object pkInstance,
            Object value,
            Pk foreignKey) throws Exception {
        log.debug("findAndSetForeignKeyValue: propertyName={}", propertyName);
        if (foreignKey.isComposite()) {
            Method[] methods = pkClass.getDeclaredMethods();
            try {
                Method method = pkClass.getMethod(BeanUtil.getSetterMethodName(propertyName), value.getClass());
                method.invoke(pkInstance, value);
            } catch (Exception e) {
                log.debug("findAndSetForeignKeyValue: 1 e.getClass()={}", e.getClass());
                throw e;
            }

            try {
                Method method = pkClass.getDeclaredMethod(BeanUtil.getSetterMethodName(propertyName), value.getClass());
                log.debug("findAndSetForeignKeyValue: method={}", method);
                method.invoke(pkInstance, value);
            } catch (Exception e) {
                log.debug("findAndSetForeignKeyValue: e.getClass()={}", e.getClass());
                throw e;
            }
        } else {
            Method method = pkClass.getDeclaredMethod(BeanUtil.getSetterMethodName(propertyName), foreignKey.getAttribute().getType());
            log.debug("findAndSetForeignKeyValue: method={}", method);
            method.invoke(pkInstance, value);
        }
    }


    private Object findAndGetPropertyValue(
            String propertyName,
            Object pkInstance) throws Exception {
//        Method method = pkClass.getDeclaredMethod(BeanUtil.getGetterMethodName(propertyName));
        Method method = pkInstance.getClass().getDeclaredMethod(BeanUtil.getGetterMethodName(propertyName));
        return method.invoke(pkInstance);
    }


    @Override
    public Object checkClass(Object pkValue) throws Exception {
        if (pkValue.getClass() == pkClass)
            return pkValue;

//        try {
        Object pkObject = pkClass.getConstructor().newInstance();
//        } catch (Exception e) {
//            log.debug("readValue: e={}", e.getMessage());
//            log.debug("readValue: e.getClass()={}", e.getClass());
//            log.debug("readValue: pkClass.getConstructor()={}", pkClass.getConstructor());
//            throw e;
//        }

        assignAttributes(pkObject, pkValue, idClassPropertyData);
        return pkObject;
    }


    private void assignAttributes(
            Object pkObject,
            Object oldPkValue,
            IdClassPropertyData idClassPropertyData) throws Exception {
        log.debug("assignAttributes: oldPkValue.getClass().getName()={}", oldPkValue.getClass().getName());
        Method[] methods = oldPkValue.getClass().getDeclaredMethods();
        for (Method method : methods) {
            log.debug("assignAttributes: method.getName()={}", method.getName());
        }

        for (EnhAttribute enhAttribute : idClassPropertyData.getEnhAttributes()) {
//            Method getMethod=oldPkValue.getClass().getDeclaredMethod(enhAttribute.getGetMethod(),enhAttribute.);
//            Object value=
            Method getMethod = oldPkValue.getClass().getDeclaredMethod(enhAttribute.getGetMethod());
            Object value = getMethod.invoke(oldPkValue);
            log.debug("assignAttributes: value={}", value);
            if (value == null)
                continue;

            log.debug("assignAttributes: value.getClass().getName()={}", value.getClass().getName());
            IdClassPropertyData nested = idClassPropertyData.getNested();
            if (nested != null && nested.getClassName().equals(value.getClass().getName())) {
                log.debug("assignAttributes: nested.getClassName()={}", nested.getClassName());
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
