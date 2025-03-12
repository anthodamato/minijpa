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

import org.minijpa.jdbc.FetchParameter;
import org.minijpa.jdbc.ModelValueArray;
import org.minijpa.jdbc.QueryParameter;
import org.minijpa.jpa.db.*;
import org.minijpa.jpa.model.*;
import org.minijpa.jpa.model.relationship.JoinColumnAttribute;
import org.minijpa.sql.model.Column;
import org.minijpa.sql.model.FromTable;
import org.minijpa.sql.model.TableColumn;
import org.minijpa.sql.model.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class MetaEntityHelper {

    private static final Logger log = LoggerFactory.getLogger(MetaEntityHelper.class);

    public static AttributeFetchParameter toFetchParameter(MetaAttribute attribute) {
        return AttributeFetchParameter.build(attribute);
    }


    public static AttributeFetchParameter toFetchParameter(AbstractMetaAttribute attribute) {
        return AttributeFetchParameter.build(attribute);
    }

    public static List<FetchParameter> toFetchParameter(List<JoinColumnAttribute> attributes) {
        List<FetchParameter> list = new ArrayList<>();
        for (JoinColumnAttribute a : attributes) {
            FetchParameter columnNameValue = new AttributeFetchParameterImpl(a.getColumnName(),
                    a.getSqlType(),
                    a.getForeignKeyAttribute(),
                    a.getForeignKeyAttribute().getObjectConverter());
            list.add(columnNameValue);
        }

        return list;
    }

    public static List<QueryParameter> convertAbstractAVToQP(
            ModelValueArray<? extends AbstractAttribute> attributeValueArray) {
        List<QueryParameter> list = new ArrayList<>();
        for (int i = 0; i < attributeValueArray.size(); ++i) {
            AbstractAttribute a = attributeValueArray.getModel(i);
            QueryParameter queryParameter = new QueryParameter(
                    a.getColumnName(),
                    attributeValueArray.getValue(i),
                    a.getSqlType());
            list.add(queryParameter);
        }

        return list;
    }


    public static List<QueryParameter> convertAVToQP(
            ModelValueArray<AbstractMetaAttribute> modelValueArray) throws Exception {
        List<QueryParameter> list = new ArrayList<>();
        for (int i = 0; i < modelValueArray.size(); ++i) {
            log.debug("Convert Attribute Value to Query Parameter -> Attribute {}", modelValueArray.getModel(i));
            if (modelValueArray.getModel(i) instanceof MetaAttribute) {
                list.add(modelValueArray.getModel(i).queryParameter(modelValueArray.getValue(i)));
            } else {
                list.addAll(
                        ((RelationshipMetaAttribute) modelValueArray.getModel(i)).queryParameters(
                                modelValueArray.getValue(i)));
            }
        }

        return list;
    }


    public static List<QueryParameter> createJoinColumnAVSToQP(
            List<JoinColumnAttribute> joinColumnAttributes,
            Pk owningId, Object joinTableForeignKey) throws Exception {
        ModelValueArray<AbstractMetaAttribute> modelValueArray = new ModelValueArray<>();
        owningId.expand(joinTableForeignKey, modelValueArray);
        List<QueryParameter> queryParameters = new ArrayList<>();
        for (int i = 0; i < modelValueArray.size(); ++i) {
            MetaAttribute attribute = (MetaAttribute) modelValueArray.getModel(i);
            int index = AttributeUtil.indexOfJoinColumnAttribute(joinColumnAttributes, attribute);
            MetaAttribute metaAttribute = joinColumnAttributes.get(index).getForeignKeyAttribute();
            QueryParameter qp = new QueryParameter(joinColumnAttributes.get(index).getColumnName(),
                    modelValueArray.getValue(i), metaAttribute.getSqlType(),
                    metaAttribute.getObjectConverter());
            queryParameters.add(qp);
        }

        return queryParameters;
    }

    public static List<FetchParameter> convertAllAttributes(MetaEntity entity) {
        List<MetaAttribute> expandedAttributes = entity.expandAllAttributes();
        List<FetchParameter> fetchColumnNameValues = expandedAttributes.stream()
                .map(a -> (FetchParameter) a)
                .collect(Collectors.toList());
        fetchColumnNameValues.addAll(toFetchParameter(entity.expandJoinColumnAttributes()));
        return fetchColumnNameValues;
    }

    public static TableColumn toValue(AbstractMetaAttribute attribute, FromTable fromTable) {
        return new TableColumn(fromTable, new Column(attribute.getColumnName()));
    }

    public static List<Value> toValues(MetaEntity entity, FromTable fromTable) {
        List<Value> values = new ArrayList<>();
        List<MetaAttribute> expandedAttributes = entity.expandAllAttributes();
        for (AbstractMetaAttribute attribute : expandedAttributes) {
            TableColumn tableColumn = new TableColumn(fromTable, new Column(attribute.getColumnName()));
            values.add(tableColumn);
        }

        for (JoinColumnAttribute joinColumnAttribute : entity.expandJoinColumnAttributes()) {
            TableColumn tableColumn = new TableColumn(fromTable,
                    new Column(joinColumnAttribute.getColumnName()));
            values.add(tableColumn);
        }

        return values;
    }

    public static List<TableColumn> toValues(
            List<? extends AbstractMetaAttribute> attributes,
            FromTable fromTable) {
        List<TableColumn> tableColumns = new ArrayList<>();
        for (AbstractMetaAttribute metaAttribute : attributes) {
            TableColumn tableColumn = new TableColumn(fromTable,
                    new Column(metaAttribute.getColumnName()));
            tableColumns.add(tableColumn);
        }

        return tableColumns;
    }


    public static List<TableColumn> attributesToTableColumns(
            List<? extends AbstractAttribute> attributes,
            FromTable fromTable) {
        return attributes.stream().map(a -> new TableColumn(fromTable, new Column(a.getColumnName())))
                .collect(Collectors.toList());
    }

    public static boolean hasOptimisticLock(MetaEntity entity, Object entityInstance)
            throws IllegalAccessException, InvocationTargetException {
        LockType lockType = (LockType) entity.getLockTypeAttributeReadMethod()
                .invoke(entityInstance);
        if (lockType == LockType.OPTIMISTIC || lockType == LockType.OPTIMISTIC_FORCE_INCREMENT) {
            return true;
        }

        return entity.getVersionMetaAttribute() != null;
    }

    public static void updateVersionAttributeValue(MetaEntity entity, Object entityInstance)
            throws Exception {
        if (!hasOptimisticLock(entity, entityInstance))
            return;

        Object versionValue = entity.nextVersionValue(entityInstance);
        entity.getVersionMetaAttribute().getWriteMethod().invoke(entityInstance, versionValue);
    }

    public static void createVersionAttributeArrayEntry(
            MetaEntity entity,
            Object entityInstance,
            ModelValueArray<AbstractMetaAttribute> modelValueArray) throws Exception {
        if (!hasOptimisticLock(entity, entityInstance)) {
            return;
        }

        Object versionValue = entity.nextVersionValue(entityInstance);
        modelValueArray.add(entity.getVersionMetaAttribute(), versionValue);
    }


    public static LockType getLockType(MetaEntity entity, Object entityInstance) throws Exception {
        return (LockType) entity.getLockTypeAttributeReadMethod().invoke(entityInstance);
    }

    public static void setLockType(MetaEntity entity, Object entityInstance, LockType lockType)
            throws Exception {
        entity.getLockTypeAttributeWriteMethod().invoke(entityInstance, lockType);
    }

    public static Optional<QueryParameter> generateVersionParameter(MetaEntity metaEntity) {
        VersionMetaAttribute versionMetaAttribute = metaEntity.getVersionMetaAttribute();
        if (versionMetaAttribute == null)
            return Optional.empty();

        Object value = versionMetaAttribute.getFirstValue();
        QueryParameter queryParameter = versionMetaAttribute.queryParameter(value);
        return Optional.of(queryParameter);
    }

    public static EntityStatus getEntityStatus(MetaEntity entity, Object entityInstance)
            throws Exception {
        return (EntityStatus) entity.getEntityStatusAttributeReadMethod().invoke(entityInstance);
    }

    public static void setEntityStatus(MetaEntity entity, Object entityInstance,
                                       EntityStatus entityStatus)
            throws Exception {
        entity.getEntityStatusAttributeWriteMethod().invoke(entityInstance, entityStatus);
    }

    public static void setForeignKeyValue(
            RelationshipMetaAttribute attribute,
            Object entityInstance,
            Object value) throws IllegalAccessException, InvocationTargetException {
        attribute.getJoinColumnWriteMethod().invoke(entityInstance, value);
    }

    public static Object getForeignKeyValue(
            RelationshipMetaAttribute attribute,
            Object entityInstance) throws IllegalAccessException, InvocationTargetException {
        return attribute.getJoinColumnReadMethod().invoke(entityInstance);
    }

    public static boolean isFlushed(
            MetaEntity entity,
            Object entityInstance) throws Exception {
        EntityStatus entityStatus = getEntityStatus(entity, entityInstance);
        return entityStatus == EntityStatus.FLUSHED
                || entityStatus == EntityStatus.FLUSHED_LOADED_FROM_DB;
    }

    public static boolean isDetached(MetaEntity entity, Object entityInstance) throws Exception {
        EntityStatus entityStatus = getEntityStatus(entity, entityInstance);
        return entityStatus == EntityStatus.DETACHED;
    }


    public static ModelValueArray<AbstractMetaAttribute> getModifications(
            MetaEntity entity,
            Object entityInstance)
            throws IllegalAccessException, InvocationTargetException {
        ModelValueArray<AbstractMetaAttribute> modelValueArray = new ModelValueArray<>();
        Method m = entity.getModificationAttributeReadMethod();
        List list = (List) m.invoke(entityInstance);
        log.debug("Entity Modifications -> Count {}", list.size());
        if (list.isEmpty()) {
            return modelValueArray;
        }

        for (Object p : list) {
            String v = (String) p;
            log.debug("Entity Modifications -> attribute '{}'", v);
            AbstractMetaAttribute attribute = entity.getAttribute(v);
            log.debug("Entity Modifications -> attribute {}", attribute);
            if (attribute == null) {
                Optional<MetaEntity> embeddable = entity.getEmbeddable(v);
                if (embeddable.isPresent()) {
                    Object embeddedInstance = embeddable.get().getValue(entityInstance);
                    ModelValueArray<AbstractMetaAttribute> mva = getModifications(embeddable.get(),
                            embeddedInstance);
                    modelValueArray.add(mva);
                }
            } else {
                Object value = attribute.getReadMethod().invoke(entityInstance);
                modelValueArray.add(attribute, value);
            }
        }

        return modelValueArray;
    }

    public static List getJoinColumnPostponedUpdateAttributeList(MetaEntity entity,
                                                                 Object parentInstance)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method m = entity.getJoinColumnPostponedUpdateAttributeReadMethod();
        return (List) m.invoke(parentInstance);
    }


    public static Object writeMetaAttributeValue(
            Object parentInstance,
            Class<?> parentClass,
            AbstractMetaAttribute attribute,
            Object value,
            MetaEntity entity) throws Exception {
        Object parent = parentInstance;
        if (parent == null)
            parent = parentClass.getDeclaredConstructor().newInstance();

        log.debug("Writing Attribute Value -> {}", value);
        if (value != null) {
            log.debug("Writing Meta Attribute Value -> Value Class {}", value.getClass());
        }

        log.debug("Writing Attribute Value -> to Object {}", parent);
        log.debug("Writing Attribute Value -> Write Method {}", attribute.getWriteMethod());

        attribute.getWriteMethod().invoke(parent, value);
        entity.removeModificationAttribute(parent, attribute.getName());
        return parent;
    }


    public static Object writeEmbeddableValue(
            Object parentInstance,
            Class<?> parentClass,
            MetaEntity embeddable,
            Object value,
            MetaEntity entity) throws Exception {
        Object parent = parentInstance;
        if (parent == null) {
            parent = parentClass.getDeclaredConstructor().newInstance();
        }

        log.debug("Writing Embeddable Value -> {}", value);
        log.debug("Writing Embedded Value -> to Object {}", parent);
        log.debug("Writing Embedded Value -> Write Method {}", embeddable.getWriteMethod());

        embeddable.getWriteMethod().invoke(parent, value);
        entity.removeModificationAttribute(parent, embeddable.getName());
        return parent;
    }


    public static Object writeAttributeValue(
            MetaEntity entity,
            Object parentInstance,
            AbstractMetaAttribute attribute,
            Object value) throws Exception {
        log.debug("Writing Attribute Value -> {}", value);
        log.debug("Writing Attribute Value -> to Object {}", parentInstance);
        log.debug("Writing Attribute Value -> Attribute '{}'", attribute.getName());
        return findAndSetAttributeValue(entity.getEntityClass(), parentInstance, attribute, value,
                entity);
    }


    private static Object findAndSetAttributeValue(
            Class<?> parentClass,
            Object parentInstance,
            AbstractMetaAttribute attribute,
            Object value,
            MetaEntity entity) throws Exception {
        log.debug("Find And Set Attribute Value -> Value {}", value);
        log.debug("Find And Set Attribute Value -> Attribute {}", attribute);
        log.debug("Find And Set Attribute Value -> Parent Instance {}", parentInstance);
        log.debug("Find And Set Attribute Value -> Parent Class={}", parentClass);
        log.debug("Find And Set Attribute Value -> Entity {}", entity);

        // search over all attributes
        for (AbstractMetaAttribute a : entity.getAttributes()) {
            if (a == attribute) {
                return MetaEntityHelper.writeMetaAttributeValue(parentInstance, parentClass, a, value,
                        entity);
            }
        }

        for (MetaEntity embeddable : entity.getEmbeddables()) {
            Object parent = embeddable.getValue(parentInstance);
            Object aInstance = findAndSetAttributeValue(embeddable.getEntityClass(), parent, attribute,
                    value,
                    embeddable);
            if (aInstance != null) {
                return MetaEntityHelper.writeEmbeddableValue(parentInstance, parentClass, embeddable,
                        aInstance,
                        entity);
            }
        }

        return null;
    }


    // TODO the MetaEntity.getLazyLoadedAttributeReadMethod() method could be empty.
    // If there ara not lazy attributes it's empty
    public static boolean isLazyAttributeLoaded(
            MetaEntity entity,
            AbstractMetaAttribute a,
            Object entityInstance)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        // TODO condition added to avoid that 'entity.getLazyLoadedAttributeReadMethod().get()'
        // throws an exception 'No value present'
        if (!a.isLazy()) {
            return true;
        }

        Method m = entity.getLazyLoadedAttributeReadMethod();
        List list = (List) m.invoke(entityInstance);
        return list.contains(a.getName());
    }

    public static void lazyAttributeLoaded(
            MetaEntity entity,
            AbstractMetaAttribute a,
            Object entityInstance,
            boolean loaded)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method m = entity.getLazyLoadedAttributeReadMethod();
        List list = (List) m.invoke(entityInstance);
        if (loaded) {
            if (!list.contains(a.getName())) {
                list.add(a.getName());
            }
        } else {
            list.remove(a.getName());
        }
    }


    public static void addElementToCollectionAttribute(
            Object entityInstance,
            MetaEntity metaEntity,
            RelationshipMetaAttribute metaAttribute,
            Object element) throws Exception {
        log.debug("Adding Element To Collection Attribute -> Entity Instance {}", entityInstance);
        log.debug("Adding Element To Collection Attribute -> Attribute {}", metaAttribute);
        Object collectionInstance = metaAttribute.getValue(entityInstance);
        log.debug("Adding Element To Collection Attribute -> Collection Instance {}", collectionInstance);
        if (collectionInstance == null) {
            // it has to create a collection instance for the attribute
            collectionInstance = CollectionUtils.createInstance(null,
                    metaAttribute.getCollectionImplementationClass());
            MetaEntityHelper.writeMetaAttributeValue(entityInstance, entityInstance.getClass(),
                    metaAttribute, collectionInstance,
                    metaEntity);
        }

        if (collectionInstance instanceof Collection) {
            ((Collection) collectionInstance).add(element);
        }
    }
}
