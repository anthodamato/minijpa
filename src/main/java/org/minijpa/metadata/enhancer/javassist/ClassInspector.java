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
import java.util.logging.Level;
import java.util.stream.Collectors;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.minijpa.metadata.BeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.expr.Cast;
import javassist.expr.ConstructorCall;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.Handler;
import javassist.expr.Instanceof;
import javassist.expr.MethodCall;
import javassist.expr.NewArray;
import javassist.expr.NewExpr;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import org.minijpa.metadata.RelationshipHelper;

public class ClassInspector {

    private final Logger LOG = LoggerFactory.getLogger(ClassInspector.class);
    private final List<ManagedData> inspectedClasses = new ArrayList<>();
    private final String modificationAttributePrefix = "mds";
    private final String lazyLoadedAttributePrefix = "lla";
    private final String lockTypeAttributePrefix = "lta";
    private final String entityStatusAttributePrefix = "sts";
    private final String joinColumnPostponedUpdateAttributePrefix = "jcpu";

    public ManagedData inspect(String className) throws Exception {
	// already inspected
	for (ManagedData managedData : inspectedClasses) {
	    LOG.debug("inspect: managedData=" + managedData + "; managedData.getClassName()="
		    + managedData.getClassName() + "; attrs=" + managedData.getAttributeDataList().stream()
		    .map(a -> a.property.ctField.getName()).collect(Collectors.toList()));
	    if (managedData.getClassName().equals(className))
		return managedData;
	}

	LOG.debug("inspect: Thread.currentThread()=" + Thread.currentThread());
	LOG.debug("Inspecting " + className);
	ClassPool pool = ClassPool.getDefault();
	CtClass ct = null;
	try {
	    ct = pool.get(className);
	} catch (NotFoundException e) {
	    throw new IllegalArgumentException("Class '" + className + "' not found");
	}

	// mapped superclasses are enhanced finding the entity superclasses
	Object mappedSuperclassAnnotation = ct.getAnnotation(MappedSuperclass.class);
	if (mappedSuperclassAnnotation != null)
	    throw new IllegalArgumentException("Found @MappedSuperclass annotation. Class '" + className + "'");

	// skip embeddable classes
	Object embeddableAnnotation = ct.getAnnotation(Embeddable.class);
	if (embeddableAnnotation != null)
	    throw new IllegalArgumentException("Found @Embeddable annotation. Class '" + className + "'");

	Object entityAnnotation = ct.getAnnotation(Entity.class);
	if (entityAnnotation == null) {
	    throw new IllegalArgumentException("@Entity annotation not found. Class '" + className + "'");
	}

	ManagedData managedData = new ManagedData();
	managedData.setClassName(className);

	Optional<ManagedData> optional = findMappedSuperclass(ct);
	if (optional.isPresent())
	    managedData.mappedSuperclass = optional.get();

	List<Property> properties = findProperties(ct);
	LOG.debug("Found " + properties.size() + " attributes in '" + ct.getName() + "'");

	// modification attribute
	Optional<String> modificationAttribute = findAvailableAttribute(modificationAttributePrefix, properties, ct);
	removeAttributeFromProperties(modificationAttribute.get(), properties);
	// lock type attribute
	Optional<String> lockTypeAttribute = findAvailableAttribute(lockTypeAttributePrefix, properties, ct);
	removeAttributeFromProperties(lockTypeAttribute.get(), properties);
	// entity status attribute
	Optional<String> entityStatusAttribute = findAvailableAttribute(entityStatusAttributePrefix, properties, ct);
	removeAttributeFromProperties(entityStatusAttribute.get(), properties);

	// lazy loaded attribute tracker	
	Optional<String> lazyLoadedAttribute = createLazyLoadedAttribute(properties, ct);
//	Optional<Property> optionalLazy = properties.stream()
//		.filter(p -> p.getRelationshipProperties().isPresent() && p.getRelationshipProperties().get().isLazy())
//		.findFirst();
//	if (optionalLazy.isPresent()) {
//	    lazyLoadedAttribute = findAvailableAttribute(lazyLoadedAttributePrefix, properties, ct);
//	    removeAttributeFromProperties(lazyLoadedAttribute.get(), properties);
//	}

	// join column postponed update attribute
//	createJoinColumnPostponedUpdateAttributeOnDest(properties, ct);
	Optional<String> joinColumnPostponedUpdateAttribute = createJoinColumnPostponedUpdateAttribute(properties, ct);
	LOG.debug("inspect: joinColumnPostponedUpdateAttribute.isPresent()=" + joinColumnPostponedUpdateAttribute.isPresent());
	// join column fields
	findJoinColumnAttributeFields(properties, ct);

	List<AttributeData> attrs = createDataAttributes(properties, false);
	managedData.addAttributeDatas(attrs);
	managedData.setCtClass(ct);
	managedData.setModificationAttribute(modificationAttribute.get());
	managedData.setLockTypeAttribute(lockTypeAttribute);
	managedData.setEntityStatusAttribute(entityStatusAttribute);
	managedData.setLazyLoadedAttribute(lazyLoadedAttribute);
	managedData.setJoinColumnPostponedUpdateAttribute(joinColumnPostponedUpdateAttribute);

	// looks for embeddables
	LOG.debug("Inspects embeddables...");
	List<ManagedData> embeddables = new ArrayList<>();
	createEmbeddables(attrs, embeddables);
	managedData.getEmbeddables().addAll(embeddables);
	LOG.debug("Found " + embeddables.size() + " embeddables in '" + ct.getName() + "'");

	List<BMTMethodInfo> methodInfos = inspectConstructorsAndMethods(ct);
	addPrimitiveAttributesToInitialization(properties, methodInfos);
	managedData.getMethodInfos().addAll(methodInfos);

	inspectedClasses.add(managedData);
	return managedData;
    }

    private Optional<String> createLazyLoadedAttribute(List<Property> properties, CtClass ct) {
	Optional<String> lazyLoadedAttribute = Optional.empty();
	Optional<Property> optionalLazy = properties.stream()
		.filter(p -> p.getRelationshipProperties().isPresent() && p.getRelationshipProperties().get().isLazy())
		.findFirst();
	if (optionalLazy.isPresent()) {
	    lazyLoadedAttribute = findAvailableAttribute(lazyLoadedAttributePrefix, properties, ct);
	    removeAttributeFromProperties(lazyLoadedAttribute.get(), properties);
	}

	return lazyLoadedAttribute;
    }

    private Optional<String> createJoinColumnPostponedUpdateAttribute(List<Property> properties, CtClass ct) {
	Optional<String> optionalName = Optional.empty();
//	Optional<Property> optionalLazy = properties.stream()
//		.filter(p -> p.getRelationshipProperties().isPresent()
//		&& p.getRelationshipProperties().get().hasJoinColumn())
//		.findFirst();
//	if (optionalLazy.isPresent()) {
	optionalName = findAvailableAttribute(joinColumnPostponedUpdateAttributePrefix, properties, ct);
	removeAttributeFromProperties(optionalName.get(), properties);
//	}

	return optionalName;
    }

    private void createJoinColumnPostponedUpdateAttributeOnDest(
	    List<Property> properties,
	    CtClass ct) throws Exception {
	List<Property> list = properties.stream()
		.filter(p -> p.getRelationshipProperties().isPresent()
		&& p.getRelationshipProperties().get().hasJoinColumn())
		.collect(Collectors.toList());
	for (Property p : list) {
	    Optional<ManagedData> o = Optional.empty();
	    for (ManagedData managedData : inspectedClasses) {
		if (managedData.getClassName().equals(p.ctField.getType().getName())) {
		    o = Optional.of(managedData);
		    break;
		}
	    }

	    LOG.debug("createJoinColumnPostponedUpdateAttributeOnDest: p.getCtField().getName()=" + p.getCtField().getName());
	    LOG.debug("createJoinColumnPostponedUpdateAttributeOnDest: o.isEmpty()=" + o.isEmpty());
	    if (o.isEmpty()) {
		ManagedData managedData = inspect(p.ctField.getType().getName());
		o = Optional.of(managedData);
	    }

	    if (o.get().getJoinColumnPostponedUpdateAttribute().isEmpty()) {
		Optional<String> optionalName = findAvailableAttribute(joinColumnPostponedUpdateAttributePrefix,
			properties, o.get().getCtClass());
		LOG.debug("createJoinColumnPostponedUpdateAttributeOnDest: optionalName.get()=" + optionalName.get());
		o.get().setJoinColumnPostponedUpdateAttribute(optionalName);
	    }
	}
    }

    private void addPrimitiveAttributesToInitialization(
	    List<Property> properties,
	    List<BMTMethodInfo> methodInfos) throws NotFoundException {
	for (Property property : properties) {
	    if (property.getCtField().getType().isPrimitive()) {
		for (BMTMethodInfo bMTMethodInfo : methodInfos) {
		    Optional<BMTFieldInfo> optional = bMTMethodInfo.getBmtFieldInfos().stream().filter(f -> f.name.equals(property.getCtField().getName())).findFirst();
		    if (optional.isEmpty()) {
			bMTMethodInfo.getBmtFieldInfos().add(new BMTFieldInfo(BMTFieldInfo.PRIMITIVE, property.getCtField().getName(), null));
		    }
		}
	    }
	}
    }

    private void findJoinColumnAttributeFields(List<Property> properties, CtClass ct) {
	List<String> fieldNames = new ArrayList<>();
	for (Property property : properties) {
	    Optional<RelationshipProperties> optional = property.getRelationshipProperties();
	    if (optional.isPresent()) {
		RelationshipProperties relationshipProperties = optional.get();
		if (relationshipProperties.hasJoinColumn() && relationshipProperties.isLazy()) {
		    String prefix = property.ctField.getName() + "_jcv";
		    Optional<String> o = findAvailableAttribute(prefix, properties, ct);
		    relationshipProperties.setJoinColumnFieldName(o);
		    fieldNames.add(o.get());
		}
	    }
	}

	for (String fn : fieldNames) {
	    removeAttributeFromProperties(fn, properties);
	}
    }

    private Optional<String> findAvailableAttribute(String attributePrefix, List<Property> properties,
	    CtClass ctClass) {
	for (int i = 0; i < 100; ++i) {
	    String name = attributePrefix + Integer.toString(i);
	    Optional<Property> optional = properties.stream().filter(p -> p.getCtField().getName().equals(name)).findFirst();
	    if (optional.isEmpty())
		return Optional.of(name);

	    // the class has been already written, the attribute already created
	    if (ctClass.isFrozen())
		return Optional.of(name);
	}

	return Optional.empty();
    }

    /**
     * If the class is written it has to remove the attribute property.
     *
     * @param attributeName
     * @param properties
     */
    private void removeAttributeFromProperties(String attributeName, List<Property> properties) {
	Optional<Property> optionalMA = properties.stream().filter(p -> p.getCtField().getName().equals(attributeName)).findFirst();
	if (optionalMA.isPresent())
	    properties.remove(optionalMA.get());
    }

    private Optional<ManagedData> findMappedSuperclass(CtClass ct)
	    throws Exception {
	CtClass superClass = ct.getSuperclass();
	if (superClass == null)
	    return Optional.empty();

	if (superClass.getName().equals("java.lang.Object"))
	    return Optional.empty();

	LOG.debug("findMappedSuperclass: superClass.getName()=" + superClass.getName());
	Object mappedSuperclassAnnotation = superClass.getAnnotation(MappedSuperclass.class);
	if (mappedSuperclassAnnotation == null)
	    return Optional.empty();

	// checks if the mapped superclass id already inspected
	ManagedData mappedSuperclassEnhEntity = findInspectedMappedSuperclass(superClass.getName());
	LOG.debug("findMappedSuperclass: mappedSuperclassEnhEntity=" + mappedSuperclassEnhEntity);
	if (mappedSuperclassEnhEntity != null)
	    return Optional.of(mappedSuperclassEnhEntity);

	List<Property> properties = findProperties(superClass);
	Optional<String> modificationAttribute = findAvailableAttribute(modificationAttributePrefix, properties, superClass);
	removeAttributeFromProperties(modificationAttribute.get(), properties);

	// lazy loaded attribute tracker
	Optional<String> lazyLoadedAttribute = createLazyLoadedAttribute(properties, ct);
//	Optional<Property> optionalLazy = properties.stream()
//		.filter(p -> p.getRelationshipProperties().isPresent() && p.getRelationshipProperties().get().isLazy())
//		.findFirst();
//	if (optionalLazy.isPresent()) {
//	    lazyLoadedAttribute = findAvailableAttribute(lazyLoadedAttributePrefix, properties, ct);
//	    removeAttributeFromProperties(lazyLoadedAttribute.get(), properties);
//	}

	// join column postponed update attribute
//	createJoinColumnPostponedUpdateAttributeOnDest(properties, ct);
	Optional<String> joinColumnPostponedUpdateAttribute = createJoinColumnPostponedUpdateAttribute(properties, ct);

	LOG.debug("Found " + properties.size() + " attributes in '" + superClass.getName() + "'");
	List<AttributeData> attrs = createDataAttributes(properties, false);
	LOG.debug("findMappedSuperclass: attrs.size()=" + attrs.size());
	if (attrs.isEmpty())
	    return Optional.empty();

	ManagedData mappedSuperclass = new ManagedData();
	mappedSuperclass.setClassName(superClass.getName());
	mappedSuperclass.addAttributeDatas(attrs);
	mappedSuperclass.setCtClass(superClass);
	mappedSuperclass.setModificationAttribute(modificationAttribute.get());
	mappedSuperclass.setLazyLoadedAttribute(lazyLoadedAttribute);
	mappedSuperclass.setJoinColumnPostponedUpdateAttribute(joinColumnPostponedUpdateAttribute);

	List<ManagedData> embeddables = new ArrayList<>();
	createEmbeddables(attrs, embeddables);
	mappedSuperclass.getEmbeddables().addAll(embeddables);

	return Optional.of(mappedSuperclass);
    }

    private ManagedData findInspectedMappedSuperclass(String superclassName) {
	for (ManagedData enhEntity : inspectedClasses) {
	    ManagedData mappedSuperclassEnhEntity = enhEntity.mappedSuperclass;
	    if (mappedSuperclassEnhEntity != null && mappedSuperclassEnhEntity.getClassName().equals(superclassName))
		return mappedSuperclassEnhEntity;
	}

	return null;
    }

    private void createEmbeddables(List<AttributeData> dataAttributes, List<ManagedData> embeddables) {
	for (AttributeData dataAttribute : dataAttributes) {
	    if (!dataAttribute.property.embedded)
		continue;

	    ManagedData managedData = findInspectedEmbeddable(inspectedClasses,
		    dataAttribute.property.ctField.getName());
	    if (managedData != null)
		embeddables.add(managedData);
	}
    }

    private ManagedData findInspectedEmbeddable(List<ManagedData> inspectedClasses, String className) {
	for (ManagedData managedData : inspectedClasses) {
	    for (ManagedData embeddable : managedData.getEmbeddables()) {
		if (embeddable.getClassName().equals(className))
		    return embeddable;

		if (!embeddable.getEmbeddables().isEmpty()) {
		    ManagedData entity = findInspectedEmbeddable(embeddable.getEmbeddables(), className);
		    if (entity != null)
			return entity;
		}
	    }
	}

	return null;
    }

    private List<AttributeData> createDataAttributes(List<Property> properties, boolean embeddedId) throws Exception {
	List<AttributeData> attributes = new ArrayList<>();
	// nothing to do if there are no persistent attributes
	if (properties.isEmpty())
	    return attributes;

	if (countAttributesToEnhance(properties) == 0)
	    return attributes;

	for (Property property : properties) {
	    AttributeData dataAttribute = createAttributeFromProperty(property, embeddedId);
	    attributes.add(dataAttribute);
	}

	return attributes;
    }

    private List<BMTMethodInfo> inspectConstructorsAndMethods(CtClass ct) throws Exception {
	List<BMTMethodInfo> methodInfos = new ArrayList<>();
	if (ct.isFrozen())
	    return methodInfos;

	ExprEditorExt exprEditorExt = new ExprEditorExt(ct.getName());
	CtConstructor[] ctConstructors = ct.getConstructors();
	for (CtConstructor ctConstructor : ctConstructors) {
	    ctConstructor.instrument(exprEditorExt);
	    List<BMTFieldInfo> fieldInfos = exprEditorExt.getFieldInfos();
	    LOG.debug("inspectConstructorsAndMethods: fieldInfos.size()=" + fieldInfos.size());
	    BMTMethodInfo methodInfo = new BMTMethodInfo();
	    methodInfo.ctConstructor = ctConstructor;
	    methodInfo.addFieldInfos(fieldInfos);
	    methodInfos.add(methodInfo);

	    exprEditorExt.clear();
	}

	return methodInfos;
    }

    private long countAttributesToEnhance(List<Property> properties) {
	return properties.stream().filter(p -> !p.id).count();
    }

    private AttributeData createAttributeFromProperty(Property property, boolean parentIsEmbeddedId) throws Exception {
	LOG.debug("createAttributeFromProperty: property.ctField.getName()=" + property.ctField.getName()
		+ "; property.embedded=" + property.embedded + "; property.ctField.getType().getName()="
		+ property.ctField.getType().getName());
	ManagedData embeddedData = null;
	if (property.embedded) {
	    Optional<String> modificationAttribute = findAvailableAttribute(modificationAttributePrefix,
		    property.embeddedProperties, property.ctField.getType());
	    removeAttributeFromProperties(modificationAttribute.get(), property.embeddedProperties);

	    // lazy loaded attribute tracker
	    Optional<String> lazyLoadedAttribute = createLazyLoadedAttribute(property.embeddedProperties, property.ctField.getType());
//	    Optional<Property> optionalLazy = property.embeddedProperties.stream()
//		    .filter(p -> p.getRelationshipProperties().isPresent() && p.getRelationshipProperties().get().isLazy())
//		    .findFirst();
//	    if (optionalLazy.isPresent()) {
//		lazyLoadedAttribute = findAvailableAttribute(lazyLoadedAttributePrefix, property.embeddedProperties, property.ctField.getType());
//		removeAttributeFromProperties(lazyLoadedAttribute.get(), property.embeddedProperties);
//	    }

	    // join column postponed update attribute
//	    createJoinColumnPostponedUpdateAttributeOnDest(property.embeddedProperties, property.ctField.getType());
	    Optional<String> joinColumnPostponedUpdateAttribute = createJoinColumnPostponedUpdateAttribute(property.embeddedProperties, property.ctField.getType());

	    embeddedData = new ManagedData(ManagedData.EMBEDDABLE);
	    embeddedData.addAttributeDatas(createDataAttributes(property.embeddedProperties, property.id));
	    embeddedData.setCtClass(property.ctField.getType());
	    embeddedData.setClassName(property.ctField.getType().getName());
	    embeddedData.setModificationAttribute(modificationAttribute.get());
	    embeddedData.setLazyLoadedAttribute(lazyLoadedAttribute);
	    embeddedData.setJoinColumnPostponedUpdateAttribute(joinColumnPostponedUpdateAttribute);
	}

	AttributeData attributeData = new AttributeData(property, parentIsEmbeddedId, embeddedData);
	return attributeData;
    }

    private List<Property> findProperties(CtClass ctClass) throws Exception {
//		CtBehavior[] ctBehaviors = ctClass.getDeclaredBehaviors();
//		for(CtBehavior ctBehavior:ctBehaviors) {
//			LOG.info("findAttributes: ctField.getName()=" + ctBehavior.);
//		}

	CtField[] ctFields = ctClass.getDeclaredFields();
	List<Property> attrs = new ArrayList<>();
	for (CtField ctField : ctFields) {
	    Optional<Property> optional = readProperty(ctField, ctClass);
	    if (optional.isPresent())
		attrs.add(optional.get());
	}

	return attrs;
    }

    private Optional<Property> readProperty(CtField ctField, CtClass ctClass) throws Exception {
	LOG.debug("readAttribute: ctField.getName()=" + ctField.getName());
	LOG.debug("readAttribute: ctField.getModifiers()=" + ctField.getModifiers());
	LOG.debug("readAttribute: ctField.getType().getName()=" + ctField.getType().getName());
//		LOG.info("readAttribute: ctField.getSignature()=" + ctField.getSignature());
//		LOG.info("readAttribute: ctField.getFieldInfo()=" + ctField.getFieldInfo());
//		LOG.info("readAttribute: ctField.getFieldInfo2()=" + ctField.getFieldInfo2());
	int modifier = ctField.getModifiers();
	if (!Modifier.isPrivate(modifier) && !Modifier.isProtected(modifier) && !Modifier.isPackage(modifier))
	    return Optional.empty();

	Object transientAnnotation = ctField.getAnnotation(Transient.class);
	if (transientAnnotation != null)
	    return Optional.empty();

	Object idAnnotation = ctField.getAnnotation(Id.class);
	Object embeddedIdAnnotation = ctField.getAnnotation(EmbeddedId.class);

	boolean embedded = false;
	List<Property> embeddedProperties = null;
	Object embeddedAnnotation = ctField.getAnnotation(Embedded.class);
	if (embeddedAnnotation != null || embeddedIdAnnotation != null) {
	    Object embeddableAnnotation = ctField.getType().getAnnotation(Embeddable.class);
	    if (embeddableAnnotation == null)
		throw new Exception("@Embeddable annotation missing on '" + ctField.getType().getName() + "'");

	    embedded = true;
	    embeddedProperties = findProperties(ctField.getType());
	    if (embeddedIdAnnotation != null) {
		for (Property p : embeddedProperties) {
		    p.setEmbeddedIdParent(true);
		}
	    }

	    if (embeddedProperties.isEmpty()) {
		embeddedProperties = null;
		embedded = false;
	    }
	}

	boolean id = idAnnotation != null || embeddedIdAnnotation != null;

	PropertyMethod getPropertyMethod = findGetMethod(ctClass, ctField);
	if (!getPropertyMethod.method.isPresent())
	    return Optional.empty();

	PropertyMethod setPropertyMethod = findSetMethod(ctClass, ctField);
	if (!setPropertyMethod.method.isPresent())
	    setPropertyMethod.add = true;

	Optional<RelationshipProperties> optional = findRelationshipProperties(ctField);
	Property property = new Property(id, getPropertyMethod, setPropertyMethod, ctField, embedded,
		embeddedProperties, optional);
	return Optional.of(property);
    }

    private Optional<RelationshipProperties> findRelationshipProperties(CtField ctField)
	    throws ClassNotFoundException, NotFoundException {
	OneToOne oneToOne = (OneToOne) ctField.getAnnotation(OneToOne.class);
	if (oneToOne != null) {
	    boolean lazy = false;
	    if (oneToOne.fetch() != null && oneToOne.fetch() == FetchType.LAZY)
		lazy = true;

	    Optional<String> mappedBy = RelationshipHelper.getMappedBy(oneToOne);
	    return Optional.of(new RelationshipProperties(ctField.getName(), ctField.getType(), lazy, mappedBy.isEmpty()));
	}

	OneToMany oneToMany = (OneToMany) ctField.getAnnotation(OneToMany.class);
	if (oneToMany != null) {
	    boolean lazy = false;
	    if (oneToMany.fetch() != null && oneToMany.fetch() == FetchType.LAZY)
		lazy = true;

	    return Optional.of(new RelationshipProperties(ctField.getName(), ctField.getType(), lazy, false));
	}

	ManyToOne manyToOne = (ManyToOne) ctField.getAnnotation(ManyToOne.class);
	if (manyToOne != null) {
	    boolean lazy = false;
	    if (manyToOne.fetch() != null && manyToOne.fetch() == FetchType.LAZY)
		lazy = true;

	    Optional<String> mappedBy = RelationshipHelper.getMappedBy(manyToOne);
	    return Optional.of(new RelationshipProperties(ctField.getName(), ctField.getType(), lazy, mappedBy.isEmpty()));
	}

	ManyToMany manyToMany = (ManyToMany) ctField.getAnnotation(ManyToMany.class);
	if (manyToMany != null) {
	    boolean lazy = false;
	    if (manyToMany.fetch() != null && manyToMany.fetch() == FetchType.LAZY)
		lazy = true;

	    return Optional.of(new RelationshipProperties(ctField.getName(), ctField.getType(), lazy, false));
	}

	return Optional.empty();
    }

    private CtMethod findIsGetMethod(CtClass ctClass, CtField ctField) throws NotFoundException {
	try {
	    String methodName = "get" + BeanUtil.capitalize(ctField.getName());
	    return ctClass.getDeclaredMethod(methodName);
	} catch (NotFoundException e) {
	    if (ctField.getType().getName().equals("java.lang.Boolean")
		    || ctField.getType().getName().equals("boolean"))
		return ctClass.getDeclaredMethod("is" + BeanUtil.capitalize(ctField.getName()));
	}

	return null;
    }

    private PropertyMethod findGetMethod(CtClass ctClass, CtField ctField) throws Exception {
	CtMethod getMethod = null;
	try {
	    getMethod = findIsGetMethod(ctClass, ctField);
	} catch (NotFoundException e) {
	    return new PropertyMethod();
	}

	if (getMethod == null)
	    return new PropertyMethod();

	CtClass[] params = getMethod.getParameterTypes();
	if (params.length != 0)
	    return new PropertyMethod();

	if (!getMethod.getReturnType().subtypeOf(ctField.getType()))
	    return new PropertyMethod();

	return new PropertyMethod(Optional.of(getMethod), true);
    }

    private PropertyMethod findSetMethod(CtClass ctClass, CtField ctField) throws Exception {
	CtMethod setMethod = null;
	try {
	    setMethod = ctClass.getDeclaredMethod("set" + BeanUtil.capitalize(ctField.getName()));
	} catch (NotFoundException e) {
	    return new PropertyMethod();
	}

	CtClass[] params = setMethod.getParameterTypes();
	if (params.length != 1)
	    return new PropertyMethod();

	if (!ctField.getType().subtypeOf(params[0]))
	    return new PropertyMethod();

	if (!setMethod.getReturnType().getName().equals(Void.TYPE.getName())) // void type
	    return new PropertyMethod();

	return new PropertyMethod(Optional.of(setMethod), true);
    }

    private class ExprEditorExt extends ExprEditor {

	private final String className;
	public final int NO_OP = -1;
	public final int CONSTRUCTOR_CALL_OP = 0;
	public final int NEW_EXPR_OP = 1;
	public final int METHOD_CALL_OP = 2;
	public final int NEW_ARRAY_OP = 3;
	public final int INSTANCEOF_OP = 4;
	public final int CAST_OP = 5;
	public final int HANDLER_OP = 6;
	private int latestOpType = NO_OP;
	private String newExprClassName;
	private final List<BMTFieldInfo> fieldInfos = new ArrayList<>();

	public ExprEditorExt(String className) {
	    this.className = className;
	}

	public List<BMTFieldInfo> getFieldInfos() {
	    return fieldInfos;
	}

	public void clear() {
	    latestOpType = NO_OP;
	    newExprClassName = null;
	    fieldInfos.clear();
	}

	@Override
	public void edit(NewExpr e) throws CannotCompileException {
	    latestOpType = NEW_EXPR_OP;
	    newExprClassName = e.getClassName();
	}

	@Override
	public void edit(NewArray a) throws CannotCompileException {
	    latestOpType = NEW_ARRAY_OP;
	}

	@Override
	public void edit(MethodCall m) throws CannotCompileException {
	    latestOpType = METHOD_CALL_OP;
	}

	@Override
	public void edit(ConstructorCall c) throws CannotCompileException {
	    latestOpType = CONSTRUCTOR_CALL_OP;
	}

	@Override
	public void edit(FieldAccess f) throws CannotCompileException {
	    LOG.debug("ExprEditorExt: f.getFieldName()=" + f.getFieldName());
	    LOG.debug("ExprEditorExt: f.getSignature()=" + f.getSignature());
	    LOG.debug("ExprEditorExt: f.getClassName()=" + f.getClassName());
	    LOG.debug("ExprEditorExt: newExprClassName=" + newExprClassName);
	    try {
		CtField ctField = f.getField();
		LOG.debug("ExprEditorExt: ctField.getName()=" + ctField.getName());
		CtClass ctClass = ctField.getType();
		LOG.debug("ExprEditorExt: ctClass.getName()=" + ctClass.getName());
		boolean isEnum = ctClass.isEnum();
		LOG.debug("ExprEditorExt: isEnum=" + isEnum);
	    } catch (NotFoundException ex) {
		java.util.logging.Logger.getLogger(ClassInspector.class.getName()).log(Level.SEVERE, null, ex);
	    }

	    LOG.debug("ExprEditorExt: fieldInfos.size()=" + fieldInfos.size());
	    LOG.debug("ExprEditorExt: latestOpType=" + latestOpType);
	    if (f.getClassName().equals(className)) {
		BMTFieldInfo fieldInfo = new BMTFieldInfo(BMTFieldInfo.ASSIGNMENT, f.getFieldName(), newExprClassName);
		fieldInfos.add(fieldInfo);
	    }

	    latestOpType = NO_OP;
	    newExprClassName = null;
	    LOG.debug("ExprEditorExt: edit **********************");
	}

	@Override
	public void edit(Instanceof i) throws CannotCompileException {
	    latestOpType = INSTANCEOF_OP;
	}

	@Override
	public void edit(Cast c) throws CannotCompileException {
	    latestOpType = CAST_OP;
	}

	@Override
	public void edit(Handler h) throws CannotCompileException {
	    latestOpType = HANDLER_OP;
	}

    }

}
