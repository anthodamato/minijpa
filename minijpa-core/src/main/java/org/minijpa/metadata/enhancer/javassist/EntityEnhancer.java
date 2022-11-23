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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.persistence.Entity;

import org.minijpa.jdbc.CollectionUtils;
import org.minijpa.metadata.BeanUtil;
import org.minijpa.metadata.EntityDelegate;
import org.minijpa.metadata.enhancer.EnhAttribute;
import org.minijpa.metadata.enhancer.EnhEntity;
import org.minijpa.metadata.enhancer.Enhanced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;

public class EntityEnhancer {

	private final Logger LOG = LoggerFactory.getLogger(EntityEnhancer.class);

	private final List<ManagedData> enhancedDataEntities = new ArrayList<>();

	public EntityEnhancer() {
		super();
	}

	public EnhEntity enhance(ManagedData managedData, Set<EnhEntity> parsedEntities) throws Exception {
		Optional<EnhEntity> optional = parsedEntities.stream()
				.filter(e -> e.getClassName().equals(managedData.getClassName())).findFirst();
		if (optional.isPresent())
			return optional.get();

		EnhEntity enhMappedSuperclassEntity = null;
		if (managedData.mappedSuperclass != null)
			enhMappedSuperclassEntity = enhance(managedData.mappedSuperclass, parsedEntities);

		EnhEntity enhEntity = new EnhEntity();
		enhEntity.setClassName(managedData.getClassName());

		CtClass ct = managedData.getCtClass();
		LOG.debug("enhance: ct.getName()={}", ct.getName());
		LOG.debug("enhance: ct.isFrozen()={}; isClassModified(ct)={}", ct.isFrozen(), isClassModified(ct));
		LOG.debug("enhance: isClassWritable(ct)={}", isClassWritable(ct));
		if (!enhancedDataEntities.contains(managedData))
			createEntityStatusFields(managedData, enhEntity);

		enhanceConstructor(managedData);

		List<EnhAttribute> enhAttributes = enhanceAttributes(managedData, parsedEntities);

//	LOG.debug("enhance: modified=" + modified + "; canModify(ct)=" + canModify(ct) + "; ct.isModified()="
//		+ ct.isModified());
		LOG.debug("enhance: managedData={}", managedData);
		enhancedDataEntities.add(managedData);

		enhEntity.setEnhAttributes(enhAttributes);
		enhEntity.setMappedSuperclass(enhMappedSuperclassEntity);
		// fills the join column methods
		fillJoinColumnMethods(managedData, enhEntity);

		parsedEntities.add(enhEntity);

		return enhEntity;
	}

	private void createEntityStatusFields(ManagedData managedData, EnhEntity enhEntity) throws Exception {
		CtClass ct = managedData.getCtClass();
		if (!toEnhance(managedData)) {
			LOG.debug("Enhancement of '{}' not needed", ct.getName());
			return;
		}

		LOG.debug("Enhancing {}", ct.getName());
		addEntityDelegateField(ct);
		// modification field
		addModificationField(ct, managedData.getModificationAttribute());
		CtMethod ctMethod = createGetMethod(ct, managedData.getModificationAttribute(), "java.util.List");
		enhEntity.setModificationAttributeGetMethod(ctMethod.getName());
		// lazy loaded attribute tracking
		if (managedData.getLazyLoadedAttribute().isPresent()) {
			addListField(ct, managedData.getLazyLoadedAttribute().get());
			ctMethod = createGetMethod(ct, managedData.getLazyLoadedAttribute().get(), "java.util.List");
			enhEntity.setLazyLoadedAttributeGetMethod(Optional.of(ctMethod.getName()));
		}
		// join column postponed update attribute
		if (managedData.getJoinColumnPostponedUpdateAttribute().isPresent()) {
			addListField(ct, managedData.getJoinColumnPostponedUpdateAttribute().get());
			ctMethod = createGetMethod(ct, managedData.getJoinColumnPostponedUpdateAttribute().get(), "java.util.List");
			enhEntity.setJoinColumnPostponedUpdateAttributeGetMethod(Optional.of(ctMethod.getName()));
		}
		// lock type field
		if (managedData.getLockTypeAttribute().isPresent()) {
			addLockTypeField(ct, managedData.getLockTypeAttribute().get());
			// get method
			ctMethod = createGetMethod(ct, managedData.getLockTypeAttribute().get(), "org.minijpa.jpa.db.LockType");
			enhEntity.setLockTypeAttributeGetMethod(Optional.of(ctMethod.getName()));
			// set method
			ctMethod = createSetMethod(ct, managedData.getLockTypeAttribute().get(), "org.minijpa.jpa.db.LockType");
			enhEntity.setLockTypeAttributeSetMethod(Optional.of(ctMethod.getName()));
		}
		// entity status field
		if (managedData.getEntityStatusAttribute().isPresent()) {
			addEntityStatusField(ct, managedData.getEntityStatusAttribute().get());
			// get method
			ctMethod = createGetMethod(ct, managedData.getEntityStatusAttribute().get(),
					"org.minijpa.jpa.db.EntityStatus");
			enhEntity.setEntityStatusAttributeGetMethod(Optional.of(ctMethod.getName()));
			// set method
			ctMethod = createSetMethod(ct, managedData.getEntityStatusAttribute().get(),
					"org.minijpa.jpa.db.EntityStatus");
			enhEntity.setEntityStatusAttributeSetMethod(Optional.of(ctMethod.getName()));
		}

		// creates the join column support fields
		List<AttributeData> attributeDataList = managedData.getAttributeDataList();
		for (AttributeData attributeData : attributeDataList) {
			Optional<RelationshipProperties> optional = attributeData.getProperty().getRelationshipProperties();
			if (optional.isPresent() && optional.get().getJoinColumnFieldName().isPresent()) {
				RelationshipProperties relationshipProperties = optional.get();
				String fieldName = relationshipProperties.getJoinColumnFieldName().get();
				addJoinColumnField(ct, fieldName);
				// get method
				ctMethod = createGetMethod(ct, fieldName, "java.lang.Object");
				relationshipProperties.setCtMethodGetter(Optional.of(ctMethod));
				// set method
				ctMethod = createSetMethod(ct, fieldName, "java.lang.Object");
				relationshipProperties.setCtMethodSetter(Optional.of(ctMethod));
			}
		}
	}

	private void fillJoinColumnMethods(ManagedData managedData, EnhEntity enhEntity) {
		List<AttributeData> attributeDataList = managedData.getAttributeDataList();
		for (AttributeData attributeData : attributeDataList) {
			Optional<RelationshipProperties> optional = attributeData.getProperty().getRelationshipProperties();
			if (optional.isPresent() && optional.get().getJoinColumnFieldName().isPresent()) {
				RelationshipProperties relationshipProperties = optional.get();
				String fieldName = relationshipProperties.getFieldName();
				Optional<EnhAttribute> o = enhEntity.getAttribute(fieldName);
				if (o.isEmpty())
					throw new IllegalStateException("Field name not found: " + fieldName);

				o.get().setJoinColumnGetMethod(Optional.of(relationshipProperties.getCtMethodGetter().get().getName()));
				o.get().setJoinColumnSetMethod(Optional.of(relationshipProperties.getCtMethodSetter().get().getName()));
			}
		}
	}

	private List<EnhAttribute> enhanceAttributes(ManagedData managedData, Set<EnhEntity> parsedEntities)
			throws Exception {
		CtClass ct = managedData.getCtClass();
		List<EnhAttribute> enhAttributes = new ArrayList<>();
		List<AttributeData> dataAttributes = managedData.getAttributeDataList();
		for (AttributeData attributeData : dataAttributes) {
			Property property = attributeData.getProperty();
			boolean enhanceAttribute = toEnhance(attributeData);
			LOG.debug("Enhancing attribute '{}' {}", property.getCtField().getName(), enhanceAttribute);
			if (property.getSetPropertyMethod().add && !enhancedDataEntities.contains(managedData)) {
				CtMethod ctMethod = createSetMethod(ct, property.getCtField(), enhanceAttribute, managedData);
				property.getSetPropertyMethod().enhance = false;
				property.getSetPropertyMethod().method = Optional.of(ctMethod);
			}

			if (enhanceAttribute && !enhancedDataEntities.contains(managedData) && canModify(ct)) {
				if (property.getGetPropertyMethod().enhance)
					if (isLazyOrEntityType(property.getGetPropertyMethod().method.get().getReturnType()))
						modifyGetMethod(property.getGetPropertyMethod().method.get(), property.getCtField());

				if (property.getSetPropertyMethod().enhance)
					modifySetMethod(property.getSetPropertyMethod().method.get(), property.getCtField(), managedData);
			}

			EnhEntity embeddedEnhEntity = null;
			List<EnhAttribute> enhEmbeddedAttributes = null;
			if (attributeData.getEmbeddedData() != null) {
				embeddedEnhEntity = enhance(attributeData.getEmbeddedData(), parsedEntities);
//		embeddedEnhEntity.setEmbeddedId(attributeData.isParentEmbeddedId());
			}

			EnhAttribute enhAttribute = new EnhAttribute(property.getCtField().getName(),
					property.getCtField().getType().getName(), property.getCtField().getType().isPrimitive(),
					property.getGetPropertyMethod().method.get().getName(),
					property.getSetPropertyMethod().method.get().getName(), property.isEmbedded(),
					enhEmbeddedAttributes, embeddedEnhEntity, attributeData.isParentEmbeddedId());
			enhAttributes.add(enhAttribute);
		}

		return enhAttributes;
	}

	private boolean toEnhance(AttributeData dataAttribute) {
		if (dataAttribute.getProperty().isId())
			return false;

		if (dataAttribute.isParentEmbeddedId())
			return false;

		if (!dataAttribute.getProperty().getGetPropertyMethod().enhance
				&& !dataAttribute.getProperty().getSetPropertyMethod().enhance)
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
				if (!optional.isPresent())
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

		LOG.debug("addEntityDelegateField: ct.getName()={}", ct.getName());
		ClassPool pool = ClassPool.getDefault();
		pool.importPackage(EntityDelegate.class.getPackage().getName());
		CtField f = CtField.make("private EntityDelegate entityDelegate = EntityDelegate.getInstance();", ct);
		LOG.debug("Adding Entity Delegate");
		ct.addField(f);
	}

	private void addModificationField(CtClass ct, String modificationFieldName) throws Exception {
		if (!canModify(ct))
			return;

		CtField f = CtField.make("private java.util.List " + modificationFieldName + " = new java.util.ArrayList();",
				ct);
		ct.addField(f);
		LOG.debug("Created '{}' Field", ct.getName());
	}

	private void addListField(CtClass ct, String fieldName) throws Exception {
		if (!canModify(ct))
			return;

		CtField f = CtField.make("private java.util.List " + fieldName + " = new java.util.ArrayList();", ct);
		ct.addField(f);
		LOG.debug("Created '{}' Field", fieldName);
	}

	private void addLockTypeField(CtClass ct, String fieldName) throws Exception {
		if (!canModify(ct))
			return;

		String f = "private org.minijpa.jpa.db.LockType " + fieldName + " = org.minijpa.jpa.db.LockType.NONE;";
		CtField ctField = CtField.make(f, ct);
		ct.addField(ctField);
		LOG.debug("Created '{}' Field: {}", ct.getName(), f);
	}

	private void addEntityStatusField(CtClass ct, String fieldName) throws Exception {
		if (!canModify(ct))
			return;

		String f = "private org.minijpa.jpa.db.EntityStatus " + fieldName + " = org.minijpa.jpa.db.EntityStatus.NEW;";
		CtField ctField = CtField.make(f, ct);
		ct.addField(ctField);
		LOG.debug("Created '{}' Field: {}", ct.getName(), f);
	}

	private void addJoinColumnField(CtClass ct, String fieldName) throws Exception {
		if (!canModify(ct))
			return;

		String f = "private Object " + fieldName + " = null;";
		CtField ctField = CtField.make(f, ct);
		ct.addField(ctField);
		LOG.debug("Created '{}' Field: {}", ct.getName(), f);
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
		LOG.debug("Modifying get method: mc={}", mc);
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
		LOG.debug("Modifying set method: mc={}", mc);
		ctMethod.insertBefore(mc);
	}

	private void modifyConstructorWithCollectionCheck(CtConstructor ctConstructor, CtField ctField,
			ManagedData managedData) throws Exception {
		String mc = "if(!" + ctField.getName() + ".isEmpty()) " + managedData.getModificationAttribute() + ".add(\""
				+ ctField.getName() + "\");";
		LOG.debug("Modifying constructor: mc={}", mc);
		ctConstructor.insertAfter(mc);
	}

	private void modifyConstructorWithSimpleField(CtConstructor ctConstructor, CtField ctField, ManagedData managedData)
			throws Exception {
		String mc = managedData.getModificationAttribute() + ".add(\"" + ctField.getName() + "\");";
		LOG.debug("Modifying constructor: mc={}", mc);
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

	private String createSetMethodString(CtField ctField, boolean delegate, int counter, ManagedData managedData)
			throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("public void ");
		sb.append(buildSetMethodName(ctField.getName() + Integer.toString(counter)));
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

	private String createSetMethodString(String fieldName, String fieldTypeName) throws Exception {
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
	 * @param ctClass
	 * @param ctField
	 * @param delegate
	 * @return
	 * @throws Exception
	 */
	private CtMethod createSetMethod(CtClass ctClass, CtField ctField, boolean delegate, ManagedData managedData)
			throws Exception {
		int counter = 0;
		CtMethod ctMethod = null;
		for (int i = 0; i < 100; ++i) {
			try {
				ctMethod = ctClass.getDeclaredMethod(buildSetMethodName(ctField.getName() + Integer.toString(counter)));
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
		LOG.debug("createSetMethod: Created new method: {}", setMethodString);
		return ctMethod;
	}

	private CtMethod createSetMethod(CtClass ctClass, String fieldName, String fieldTypeName) throws Exception {
		if (!canModify(ctClass)) {
			CtClass[] ctClasses = new CtClass[1];
			ctClasses[0] = ClassPool.getDefault().get(fieldTypeName);
			return ctClass.getMethod(buildSetMethodName(fieldName), Descriptor.ofMethod(CtClass.voidType, ctClasses));
		}

		String setMethodString = createSetMethodString(fieldName, fieldTypeName);
		CtMethod ctMethod = CtNewMethod.make(setMethodString, ctClass);
		ctClass.addMethod(ctMethod);
		LOG.debug("Created '{}' method: {}", ctClass.getName(), setMethodString);
		return ctMethod;
	}

	private CtMethod createGetMethod(CtClass ctClass, String fieldName, String fieldTypeName) throws Exception {
		String getMethodString = createGetMethodString(fieldName, fieldTypeName);
		if (!canModify(ctClass)) {
			return ctClass.getMethod(buildGetMethodName(fieldName),
					Descriptor.ofMethod(ClassPool.getDefault().get(fieldTypeName), new CtClass[0]));
		}

		CtMethod ctMethod = CtNewMethod.make(getMethodString, ctClass);
		ctClass.addMethod(ctMethod);
		LOG.debug("Created '{}' method: {}", ctClass.getName(), getMethodString);
		return ctMethod;
	}

	/**
	 * Returns true if the input type can be a lazy attribute.
	 *
	 * @param ctClass
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
