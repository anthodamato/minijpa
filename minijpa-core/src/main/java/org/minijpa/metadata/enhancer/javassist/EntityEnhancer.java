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
package org.minijpa.metadata.enhancer.javassist;

import java.util.*;

import javax.persistence.Entity;

import javassist.*;
import org.minijpa.jpa.db.CollectionUtils;
import org.minijpa.metadata.BeanUtil;
import org.minijpa.metadata.EntityDelegate;
import org.minijpa.metadata.JavaTypes;
import org.minijpa.metadata.enhancer.EnhAttribute;
import org.minijpa.metadata.enhancer.EnhEntity;
import org.minijpa.metadata.enhancer.Enhanced;
import org.minijpa.metadata.enhancer.IdClassPropertyData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javassist.bytecode.Descriptor;

public class EntityEnhancer {

    private final Logger log = LoggerFactory.getLogger(EntityEnhancer.class);

    private final List<ManagedData> enhancedDataEntities = new ArrayList<>();

    public EntityEnhancer() {
    }

    public EnhEntity enhance(ManagedData managedData, Set<EnhEntity> parsedEntities) throws Exception {
        Optional<EnhEntity> optional = parsedEntities.stream()
                .filter(e -> e.getClassName().equals(managedData.getClassName())).findFirst();
        if (optional.isPresent())
            return optional.get();

        EnhEntity enhMappedSuperclassEntity = null;
        if (managedData.mappedSuperclass != null) {
            enhMappedSuperclassEntity = enhance(managedData.mappedSuperclass, parsedEntities);
            if (managedData.mappedSuperclass.getPrimaryKeyClass() != null)
                enhMappedSuperclassEntity.setIdClassPropertyData(enhancePrimaryKeyClass(managedData.mappedSuperclass.getPrimaryKeyClass()));
        }

        EnhEntity enhEntity = new EnhEntity();
        enhEntity.setClassName(managedData.getClassName());

        CtClass ct = managedData.getCtClass();
        log.debug("enhance: ct.getName()={}", ct.getName());
        log.debug("enhance: ct.isFrozen()={}; isClassModified(ct)={}", ct.isFrozen(), isClassModified(ct));
        log.debug("enhance: isClassWritable(ct)={}", isClassWritable(ct));
        if (!enhancedDataEntities.contains(managedData))
            createEntityStatusFields(managedData, enhEntity);

        enhanceConstructor(managedData);

        List<EnhAttribute> enhAttributes = enhanceAttributes(managedData, parsedEntities);

//	LOG.debug("enhance: modified=" + modified + "; canModify(ct)=" + canModify(ct) + "; ct.isModified()="
//		+ ct.isModified());
        log.debug("enhance: managedData={}", managedData);
        enhancedDataEntities.add(managedData);

        enhEntity.setEnhAttributes(enhAttributes);
        enhEntity.setMappedSuperclass(enhMappedSuperclassEntity);
        // fills the join column methods
        fillJoinColumnMethods(managedData, enhEntity);

//        enhEntity.setClassType(managedData.getCtClass().toClass());
//        managedData.getCtClass().defrost();

        parsedEntities.add(enhEntity);

        if (managedData.getPrimaryKeyClass() != null)
            enhEntity.setIdClassPropertyData(enhancePrimaryKeyClass(managedData.getPrimaryKeyClass()));

        return enhEntity;
    }


    public IdClassPropertyData enhancePrimaryKeyClass(
            ManagedData managedData) throws Exception {
        IdClassPropertyData idClassPropertyData = new IdClassPropertyData();
        idClassPropertyData.setClassName(managedData.getClassName());

        CtClass ct = managedData.getCtClass();
        log.debug("enhancePrimaryKeyClass: managedData={}", managedData);
        enhancedDataEntities.add(managedData);

        idClassPropertyData.setIdClassManagedData(managedData);
        idClassPropertyData.setIdCtClass(ct);

        return idClassPropertyData;
    }


    private void createEntityStatusFields(ManagedData managedData, EnhEntity enhEntity) throws Exception {
        CtClass ct = managedData.getCtClass();
        if (!toEnhance(managedData)) {
            log.debug("Enhancement of '{}' not needed", ct.getName());
            return;
        }

        log.debug("Enhancing {}", ct.getName());
        addEntityDelegateField(ct);
        // modification field
        addModificationField(ct, managedData.getModificationAttribute());
        CtMethod ctMethod = createGetMethod(ct, managedData.getModificationAttribute(), "java.util.List");
        enhEntity.setModificationAttributeGetMethod(ctMethod.getName());
        // lazy loaded attribute tracking
        if (managedData.getLazyLoadedAttribute() != null) {
            addListField(ct, managedData.getLazyLoadedAttribute());
            ctMethod = createGetMethod(ct, managedData.getLazyLoadedAttribute(), "java.util.List");
            enhEntity.setLazyLoadedAttributeGetMethod(ctMethod.getName());
        }

        // join column postponed update attribute
        if (managedData.getJoinColumnPostponedUpdateAttribute() != null) {
            addListField(ct, managedData.getJoinColumnPostponedUpdateAttribute());
            ctMethod = createGetMethod(ct, managedData.getJoinColumnPostponedUpdateAttribute(), "java.util.List");
            enhEntity.setJoinColumnPostponedUpdateAttributeGetMethod(ctMethod.getName());
        }
        // lock type field
        if (managedData.getLockTypeAttribute() != null) {
            addLockTypeField(ct, managedData.getLockTypeAttribute());
            // get method
            ctMethod = createGetMethod(ct, managedData.getLockTypeAttribute(), "org.minijpa.jpa.db.LockType");
            enhEntity.setLockTypeAttributeGetMethod(ctMethod.getName());
            // set method
            ctMethod = createSetMethod(ct, managedData.getLockTypeAttribute(), "org.minijpa.jpa.db.LockType");
            enhEntity.setLockTypeAttributeSetMethod(ctMethod.getName());
        }
        // entity status field
        if (managedData.getEntityStatusAttribute() != null) {
            addEntityStatusField(ct, managedData.getEntityStatusAttribute());
            // get method
            ctMethod = createGetMethod(ct, managedData.getEntityStatusAttribute(),
                    "org.minijpa.jpa.db.EntityStatus");
            enhEntity.setEntityStatusAttributeGetMethod(ctMethod.getName());
            // set method
            ctMethod = createSetMethod(ct, managedData.getEntityStatusAttribute(),
                    "org.minijpa.jpa.db.EntityStatus");
            enhEntity.setEntityStatusAttributeSetMethod(ctMethod.getName());
        }

        // creates the join column support fields
        List<AttributeData> attributeDataList = managedData.getAttributeDataList();
        for (AttributeData attributeData : attributeDataList) {
            RelationshipProperties relationshipProperties = attributeData.getProperty().getRelationshipProperties();
            if (relationshipProperties != null && relationshipProperties.getJoinColumnFieldName() != null) {
                String fieldName = relationshipProperties.getJoinColumnFieldName();
                addJoinColumnField(ct, fieldName);
                // get method
                ctMethod = createGetMethod(ct, fieldName, "java.lang.Object");
                relationshipProperties.setCtMethodGetter(ctMethod);
                // set method
                ctMethod = createSetMethod(ct, fieldName, "java.lang.Object");
                relationshipProperties.setCtMethodSetter(ctMethod);
            }
        }
    }

    private void fillJoinColumnMethods(ManagedData managedData, EnhEntity enhEntity) {
        List<AttributeData> attributeDataList = managedData.getAttributeDataList();
        for (AttributeData attributeData : attributeDataList) {
            RelationshipProperties relationshipProperties = attributeData.getProperty().getRelationshipProperties();
            if (relationshipProperties != null && relationshipProperties.getJoinColumnFieldName() != null) {
                String fieldName = relationshipProperties.getFieldName();
                Optional<EnhAttribute> o = enhEntity.getAttribute(fieldName);
                if (o.isEmpty())
                    throw new IllegalStateException("Field name not found: " + fieldName);

                o.get().setJoinColumnGetMethod(relationshipProperties.getCtMethodGetter().getName());
                o.get().setJoinColumnSetMethod(relationshipProperties.getCtMethodSetter().getName());
            }
        }
    }

    private List<EnhAttribute> enhanceAttributes(
            ManagedData managedData,
            Set<EnhEntity> parsedEntities)
            throws Exception {
        CtClass ct = managedData.getCtClass();
        List<EnhAttribute> enhAttributes = new ArrayList<>();
        List<AttributeData> dataAttributes = managedData.getAttributeDataList();
        log.debug("enhanceAttributes: dataAttributes.size()={}", dataAttributes.size());
        for (AttributeData attributeData : dataAttributes) {
            Property property = attributeData.getProperty();
            log.debug("Enhancing attribute property.getRelationshipProperties()={}", property.getRelationshipProperties());
            log.debug("Enhancing attribute property.getGetPropertyMethod()={}", property.getGetPropertyMethod());
            boolean enhanceAttribute = toEnhance(attributeData);
            log.debug("Enhancing attribute '{}' {}", property.getCtField().getName(), enhanceAttribute);
            if (property.getSetPropertyMethod().add && !enhancedDataEntities.contains(managedData)) {
                CtMethod ctMethod = createSetMethod(ct, property.getCtField(), enhanceAttribute, managedData);
                property.getSetPropertyMethod().enhance = false;
                property.getSetPropertyMethod().method = ctMethod;
            }

            if (enhanceAttribute && !enhancedDataEntities.contains(managedData) && canModify(ct)) {
                if (property.getGetPropertyMethod().enhance)
                    if (isLazyOrEntityType(property.getGetPropertyMethod().method.getReturnType()))
                        modifyGetMethod(property.getGetPropertyMethod().method, property.getCtField());

                if (property.getSetPropertyMethod().enhance)
                    modifySetMethod(property.getSetPropertyMethod().method, property.getCtField(), managedData);
            }

            EnhEntity embeddedEnhEntity = null;
            List<EnhAttribute> enhEmbeddedAttributes = null;
            if (attributeData.getEmbeddedData() != null) {
                embeddedEnhEntity = enhance(attributeData.getEmbeddedData(), parsedEntities);
//		embeddedEnhEntity.setEmbeddedId(attributeData.isParentEmbeddedId());
            }

            EnhAttribute enhAttribute = new EnhAttribute(property.getCtField().getName(),
                    property.getCtField().getType().getName(), property.getCtField().getType().isPrimitive(),
                    property.getGetPropertyMethod().method.getName(),
                    property.getSetPropertyMethod().method.getName(), property.isEmbedded(),
                    enhEmbeddedAttributes, embeddedEnhEntity, attributeData.isParentEmbeddedId());
            enhAttributes.add(enhAttribute);
        }

        return enhAttributes;
    }


    public List<EnhAttribute> enhanceIdClassAttributesGetSet(
            ManagedData managedData,
            CtClass ct,
            Map<String, CtClass> idCtClasses) throws Exception {
        List<EnhAttribute> enhAttributes = new ArrayList<>();
        List<AttributeData> dataAttributes = managedData.getAttributeDataList();
        log.debug("enhanceAttributesGetSet: dataAttributes.size()={}", dataAttributes.size());
        CtField[] fields = ct.getDeclaredFields();
        for (CtField ctField : fields) {
            log.debug("enhanceAttributesGetSet: ctField.getName()={}", ctField.getName());
        }

        for (AttributeData attributeData : dataAttributes) {
            Property property = attributeData.getProperty();
            if (!JavaTypes.isPkType(property.getCtField().getType().getName()) &&
                    !idCtClasses.containsKey(property.getCtField().getType().getName()))
                continue;

            if (property.getSetPropertyMethod().create) {
                if (idCtClasses.containsKey(property.getCtField().getType().getName())) {
                    CtMethod method = new CtMethod(CtClass.voidType, buildSetMethodName(property.getCtField().getName()),
                            new CtClass[]{idCtClasses.get(property.getCtField().getType().getName())}, ct);
                    ct.addMethod(method);
                    String body = "{ this." + property.getCtField().getName() + "=$1; }";
                    method.setBody(body);
                    ct.setModifiers(ct.getModifiers() & ~Modifier.ABSTRACT);
                    property.getSetPropertyMethod().enhance = false;
                    property.getSetPropertyMethod().method = method;
                    log.debug("enhanceAttributesGetSet: set method={}.{}", ct.getName(), method.getName());
                    log.debug("enhanceAttributesGetSet: set idCtClasses.get(property.getCtField().getType().getName()).getName()={}", idCtClasses.get(property.getCtField().getType().getName()).getName());

                    log.debug("enhanceAttributesGetSet: set property.getCtField().getName()={}", property.getCtField().getName());
                    CtField ctField = ct.getDeclaredField(property.getCtField().getName());
                    log.debug("enhanceAttributesGetSet: set ctField={}", ctField);
                    if (ctField != null)
                        ctField.setType(idCtClasses.get(property.getCtField().getType().getName()));
                } else {
                    CtMethod ctMethod = createSetMethod(ct, property.getCtField().getName(), property.getCtField().getType().getName());
                    property.getSetPropertyMethod().enhance = false;
                    property.getSetPropertyMethod().method = ctMethod;
                    log.debug("enhanceAttributesGetSet: enhanced method={}.{}", ct.getName(), ctMethod.getName());
                }
            }

            log.debug("enhanceAttributesGetSet: property.getGetPropertyMethod()={}", property.getGetPropertyMethod());
            if (property.getGetPropertyMethod().create) {
                if (idCtClasses.containsKey(property.getCtField().getType().getName())) {
                    CtMethod method = new CtMethod(idCtClasses.get(property.getCtField().getType().getName()),
                            buildGetMethodName(property.getCtField().getName()),
                            new CtClass[]{}, ct);
                    ct.addMethod(method);
                    String body = "{ return " + property.getCtField().getName() + "; }";
                    method.setBody(body);
                    ct.setModifiers(ct.getModifiers() & ~Modifier.ABSTRACT);
                    property.getGetPropertyMethod().enhance = false;
                    property.getGetPropertyMethod().method = method;
                    log.debug("enhanceAttributesGetSet: get method={}.{}", ct.getName(), method.getName());
                    log.debug("enhanceAttributesGetSet: get idCtClasses.get(property.getCtField().getType().getName()).getName()={}", idCtClasses.get(property.getCtField().getType().getName()).getName());
                } else {
                    CtMethod ctMethod = createGetMethod(ct, property.getCtField().getName(), property.getCtField().getType().getName());
                    property.getGetPropertyMethod().enhance = false;
                    property.getGetPropertyMethod().method = ctMethod;
                    log.debug("enhanceAttributesGetSet: enhanced method={}.{}", ct.getName(), ctMethod.getName());
                }
            }

            log.debug("enhanceAttributesGetSet: property.getGetPropertyMethod().method={}", property.getGetPropertyMethod().method);
            log.debug("enhanceAttributesGetSet: property.getSetPropertyMethod().method={}", property.getSetPropertyMethod().method);
            EnhAttribute enhAttribute = new EnhAttribute(property.getCtField().getName(),
                    property.getCtField().getType().getName(), property.getCtField().getType().isPrimitive(),
                    property.getGetPropertyMethod().method.getName(),
                    property.getSetPropertyMethod().method.getName(), property.isEmbedded(),
                    null, null, attributeData.isParentEmbeddedId());
            enhAttributes.add(enhAttribute);
        }

        return enhAttributes;
    }


    private boolean toEnhance(AttributeData attributeData) {
        if (attributeData.getProperty().isId()
                && attributeData.getProperty().getRelationshipProperties() != null
                && !attributeData.getProperty().getRelationshipProperties().hasJoinColumn())
            return false;

        if (attributeData.isParentEmbeddedId())
            return false;

        if (!attributeData.getProperty().getGetPropertyMethod().enhance
                && !attributeData.getProperty().getSetPropertyMethod().enhance)
            return false;

        return true;
    }


    private boolean toEnhance(ManagedData managedData) throws Exception {
        for (AttributeData attributeData : managedData.getAttributeDataList()) {
            if (toEnhance(attributeData))
                return true;
        }

        if (!managedData.getMethodInfos().isEmpty())
            return true;

        return false;
    }

    private boolean isClassModified(CtClass ctClass) throws NotFoundException {
        CtClass[] ctInterfaces = ctClass.getInterfaces();
        for (CtClass ct : ctInterfaces) {
            if (ct.getName().equals(Enhanced.class.getName()))
                return true;
        }

        return false;
    }

    private boolean isClassWritable(CtClass ctClass) {
        return !ctClass.isFrozen();
    }

    private boolean canModify(CtClass ctClass) throws NotFoundException {
//		return !isClassModified(ctClass) && isClassWritable(ctClass);
        return isClassWritable(ctClass);
    }

    private void enhanceConstructor(ManagedData managedData) throws Exception {
        for (BMTMethodInfo bmtMethodInfo : managedData.getMethodInfos()) {
            if (!canModify(managedData.getCtClass()))
                return;

            for (BMTFieldInfo bmtFieldInfo : bmtMethodInfo.getBmtFieldInfos()) {
                Optional<AttributeData> optional = managedData.findAttribute(bmtFieldInfo.name);
                if (optional.isEmpty())
                    throw new Exception("Field '" + bmtFieldInfo.name + "' not found");

                if (bmtFieldInfo.implementation != null)
                    // an implementation class. It can be a collection. NEW_EXPR_OP
                    if (CollectionUtils.isCollectionName(bmtFieldInfo.implementation))
                        modifyConstructorWithCollectionCheck(bmtMethodInfo.getCtConstructor(),
                                optional.get().getProperty().getCtField(), managedData);
                    else
                        modifyConstructorWithSimpleField(bmtMethodInfo.getCtConstructor(),
                                optional.get().getProperty().getCtField(), managedData);
                else
                    modifyConstructorWithSimpleField(bmtMethodInfo.getCtConstructor(),
                            optional.get().getProperty().getCtField(), managedData);
            }
        }
    }

    private void addEntityDelegateField(CtClass ct) throws Exception {
        if (!canModify(ct))
            return;

        log.debug("addEntityDelegateField: ct.getName()={}", ct.getName());
        ClassPool pool = ClassPool.getDefault();
        pool.importPackage(EntityDelegate.class.getPackage().getName());
        CtField f = CtField.make("private EntityDelegate entityDelegate = EntityDelegate.getInstance();", ct);
        log.debug("Adding Entity Delegate");
        ct.addField(f);
    }

    private void addModificationField(CtClass ct, String modificationFieldName) throws Exception {
        if (!canModify(ct))
            return;

        CtField f = CtField.make("private java.util.List " + modificationFieldName + " = new java.util.ArrayList();",
                ct);
        ct.addField(f);
        log.debug("Created '{}' Field", ct.getName());
    }

    private void addListField(CtClass ct, String fieldName) throws Exception {
        if (!canModify(ct))
            return;

        CtField f = CtField.make("private java.util.List " + fieldName + " = new java.util.ArrayList();", ct);
        ct.addField(f);
        log.debug("Created '{}' Field", fieldName);
    }

    private void addLockTypeField(CtClass ct, String fieldName) throws Exception {
        if (!canModify(ct))
            return;

        String f = "private org.minijpa.jpa.db.LockType " + fieldName + " = org.minijpa.jpa.db.LockType.NONE;";
        CtField ctField = CtField.make(f, ct);
        ct.addField(ctField);
        log.debug("Created '{}' Field: {}", ct.getName(), f);
    }

    private void addEntityStatusField(CtClass ct, String fieldName) throws Exception {
        if (!canModify(ct))
            return;

        String f = "private org.minijpa.jpa.db.EntityStatus " + fieldName + " = org.minijpa.jpa.db.EntityStatus.NEW;";
        CtField ctField = CtField.make(f, ct);
        ct.addField(ctField);
        log.debug("Created '{}' Field: {}", ct.getName(), f);
    }

    private void addJoinColumnField(CtClass ct, String fieldName) throws Exception {
        if (!canModify(ct))
            return;

        String f = "private Object " + fieldName + " = null;";
        CtField ctField = CtField.make(f, ct);
        ct.addField(ctField);
        log.debug("Created '{}' Field: {}", ct.getName(), f);
    }

    private void addEnhancedInterface(CtClass ct) throws Exception {
        ClassPool pool = ClassPool.getDefault();
        Class<?> enhancedClass = Enhanced.class;
        pool.importPackage(enhancedClass.getPackage().getName());
        CtClass ctClass = pool.get(enhancedClass.getName());
        ct.addInterface(ctClass);
    }

    private void modifyGetMethod(CtMethod ctMethod, CtField ctField) throws Exception {
        String mc = ctField.getName() + " = (" + ctMethod.getReturnType().getName() + ") entityDelegate.get("
                + ctField.getName() + ",\"" + ctField.getName() + "\", this);";
        log.debug("Modifying get method: mc={}", mc);
        ctMethod.insertBefore(mc);
    }

    private void modifySetMethod(CtMethod ctMethod, CtField ctField, ManagedData managedData) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("if(!");
        sb.append(managedData.getModificationAttribute());
        sb.append(".contains(\"");
        sb.append(ctField.getName());
        sb.append("\")) ");
        sb.append(managedData.getModificationAttribute());
        sb.append(".add(\"");
        sb.append(ctField.getName());
        sb.append("\");");
        String mc = sb.toString();
        log.debug("Modifying set method: mc={}", mc);
        ctMethod.insertBefore(mc);
    }

    private void modifyConstructorWithCollectionCheck(
            CtConstructor ctConstructor,
            CtField ctField,
            ManagedData managedData) throws Exception {
        String mc = "if(!" + ctField.getName() + ".isEmpty()) " + managedData.getModificationAttribute() + ".add(\""
                + ctField.getName() + "\");";
        log.debug("Modifying constructor: mc={}", mc);
        ctConstructor.insertAfter(mc);
    }

    private void modifyConstructorWithSimpleField(
            CtConstructor ctConstructor,
            CtField ctField,
            ManagedData managedData)
            throws Exception {
        String mc = managedData.getModificationAttribute() + ".add(\"" + ctField.getName() + "\");";
        log.debug("Modifying constructor: mc={}", mc);
        ctConstructor.insertAfter(mc);
    }

    private String buildSetMethodName(String fieldName) {
        StringBuilder sb = new StringBuilder();
        sb.append("set");
        sb.append(BeanUtil.capitalize(fieldName));
        return sb.toString();
    }

    private String buildGetMethodName(String fieldName) {
        StringBuilder sb = new StringBuilder();
        sb.append("get");
        sb.append(BeanUtil.capitalize(fieldName));
        return sb.toString();
    }

    private String createSetMethodString(
            CtField ctField,
            boolean delegate,
            int counter,
            ManagedData managedData)
            throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("public void ");
        sb.append(buildSetMethodName(ctField.getName() + counter));
        sb.append("(");
        sb.append(ctField.getType().getName());
        sb.append(" ");
        sb.append(ctField.getName());
        sb.append(") {");
        if (delegate) {
            sb.append("if(!");
            sb.append(managedData.getModificationAttribute());
            sb.append(".contains(\"");
            sb.append(ctField.getName());
            sb.append("\")) ");
            sb.append(managedData.getModificationAttribute());
            sb.append(".add(\"");
            sb.append(ctField.getName());
            sb.append("\");");
        }

        sb.append(" this.");
        sb.append(ctField.getName());
        sb.append("=");
        sb.append(ctField.getName());
        sb.append("; }");
        return sb.toString();
    }

    private String createSetMethodString(String fieldName, String fieldTypeName) {
        StringBuilder sb = new StringBuilder();
        sb.append("public void ");
        sb.append(buildSetMethodName(fieldName));
        sb.append("(");
        sb.append(fieldTypeName);
        sb.append(" ");
        sb.append(fieldName);
        sb.append(") {");
        sb.append(" this.");
        sb.append(fieldName);
        sb.append("=");
        sb.append(fieldName);
        sb.append("; }");
        return sb.toString();
    }

    private String createGetMethodString(String fieldName, String fieldTypeName) {
        StringBuilder sb = new StringBuilder();
        sb.append("public ");
        sb.append(fieldTypeName);
        sb.append(" ");
        sb.append(buildGetMethodName(fieldName));
        sb.append("() { return ");
        sb.append(fieldName);
        sb.append("; }");
        return sb.toString();
    }

    /**
     * TODO check this method
     *
     * @param ctClass  class
     * @param ctField  field
     * @param delegate delegate
     * @return created method
     * @throws Exception error during method building
     */
    private CtMethod createSetMethod(
            CtClass ctClass,
            CtField ctField,
            boolean delegate,
            ManagedData managedData)
            throws Exception {
        int counter = 0;
        CtMethod ctMethod = null;
        for (int i = 0; i < 100; ++i) {
            try {
                ctMethod = ctClass.getDeclaredMethod(buildSetMethodName(ctField.getName() + counter));
            } catch (NotFoundException e) {
                break;
            }

            ++counter;
        }

        if (!canModify(ctClass))
            return ctMethod;

        String setMethodString = createSetMethodString(ctField, delegate, counter, managedData);
        ctMethod = CtNewMethod.make(setMethodString, ctClass);
        ctClass.addMethod(ctMethod);
        log.debug("createSetMethod: Created new method: {}", setMethodString);
        return ctMethod;
    }


    private CtMethod createSetMethod(
            CtClass ctClass,
            String fieldName,
            String fieldTypeName) throws Exception {
        if (!canModify(ctClass)) {
            CtClass[] ctClasses = new CtClass[1];
            ctClasses[0] = ClassPool.getDefault().get(fieldTypeName);
            return ctClass.getMethod(buildSetMethodName(fieldName), Descriptor.ofMethod(CtClass.voidType, ctClasses));
        }

        String setMethodString = createSetMethodString(fieldName, fieldTypeName);
        CtMethod ctMethod = CtNewMethod.make(setMethodString, ctClass);
        ctClass.addMethod(ctMethod);
        log.debug("Created '{}' method: {}", ctClass.getName(), setMethodString);
        return ctMethod;
    }


    private CtMethod createGetMethod(
            CtClass ctClass,
            String fieldName,
            String fieldTypeName) throws Exception {
        String getMethodString = createGetMethodString(fieldName, fieldTypeName);
        if (!canModify(ctClass)) {
            return ctClass.getMethod(buildGetMethodName(fieldName),
                    Descriptor.ofMethod(ClassPool.getDefault().get(fieldTypeName), new CtClass[0]));
        }

        CtMethod ctMethod = CtNewMethod.make(getMethodString, ctClass);
        ctClass.addMethod(ctMethod);
        log.debug("Created '{}' method: {}", ctClass.getName(), getMethodString);
        return ctMethod;
    }

    /**
     * Returns true if the input type can be a lazy attribute.
     *
     * @param ctClass class
     * @return true if the input type can be a lazy attribute
     */
    private boolean isLazyOrEntityType(CtClass ctClass) throws Exception {
        String name = ctClass.getName();

        Object entity = ctClass.getAnnotation(Entity.class);
        if (entity != null)
            return true;

        return CollectionUtils.isCollectionName(name);
    }

}
