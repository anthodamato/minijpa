package org.minijpa.metadata.enhancer.javassist;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

public class ClassInspector {

    private Logger LOG = LoggerFactory.getLogger(ClassInspector.class);
    private boolean log = false;

    public ManagedData inspect(String className, List<ManagedData> inspectedClasses) throws Exception {
	// already inspected
	for (ManagedData managedData : inspectedClasses) {
	    if (log)
		LOG.info("inspect: managedData=" + managedData + "; managedData.getClassName()="
			+ managedData.getClassName() + "; attrs=" + managedData.getDataAttributes().stream()
			.map(a -> a.property.ctField.getName()).collect(Collectors.toList()));
	    if (managedData.getClassName().equals(className))
		return managedData;
	}

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
	    return null;

	// skip embeddable classes
	Object embeddableAnnotation = ct.getAnnotation(Embeddable.class);
	if (embeddableAnnotation != null)
	    return null;

	Object entityAnnotation = ct.getAnnotation(Entity.class);
	if (entityAnnotation == null) {
	    LOG.error("@Entity annotation not found: " + ct.getName());
	    return null;
	}

	ManagedData managedData = new ManagedData();
	managedData.setClassName(className);

	Optional<ManagedData> optional = findMappedSuperclass(ct, inspectedClasses);
	if (optional.isPresent())
	    managedData.mappedSuperclass = optional.get();

	List<Property> properties = findAttributes(ct);
	if (log)
	    LOG.info("Found " + properties.size() + " attributes in '" + ct.getName() + "'");

	List<AttributeData> attrs = createDataAttributes(properties, false);
	managedData.addAttributeDatas(attrs);
	managedData.setCtClass(ct);

	// looks for embeddables
	LOG.info("Inspects embeddables...");
	List<ManagedData> embeddables = new ArrayList<>();
	createEmbeddables(attrs, embeddables, inspectedClasses);
	managedData.getEmbeddables().addAll(embeddables);
	LOG.info("Found " + embeddables.size() + " embeddables in '" + ct.getName() + "'");

	if (!attrs.isEmpty() || managedData.mappedSuperclass != null) {
	    List<BMTMethodInfo> methodInfos = inspectConstructorsAndMethods(ct);
	    if (!methodInfos.isEmpty())
		managedData.getMethodInfos().addAll(methodInfos);

	    inspectedClasses.add(managedData);
	    return managedData;
	}

	return null;
    }

    private Optional<ManagedData> findMappedSuperclass(CtClass ct, List<ManagedData> inspectedClasses)
	    throws Exception {
	CtClass superClass = ct.getSuperclass();
	if (superClass == null)
	    return Optional.empty();

	if (superClass.getName().equals("java.lang.Object"))
	    return Optional.empty();

	if (log)
	    LOG.info("superClass.getName()=" + superClass.getName());
	Object mappedSuperclassAnnotation = superClass.getAnnotation(MappedSuperclass.class);
	if (mappedSuperclassAnnotation == null)
	    return Optional.empty();

	// checks if the mapped superclass id already inspected
	ManagedData mappedSuperclassEnhEntity = findInspectedMappedSuperclass(inspectedClasses, superClass.getName());
	if (log)
	    LOG.info("mappedSuperclassEnhEntity=" + mappedSuperclassEnhEntity);
	if (mappedSuperclassEnhEntity != null)
	    return Optional.of(mappedSuperclassEnhEntity);

	List<Property> properties = findAttributes(superClass);
	LOG.info("Found " + properties.size() + " attributes in '" + superClass.getName() + "'");
	List<AttributeData> attrs = createDataAttributes(properties, false);
	if (log)
	    LOG.info("attrs.size()=" + attrs.size());
	if (attrs.isEmpty())
	    return Optional.empty();

	ManagedData mappedSuperclass = new ManagedData();
	mappedSuperclass.setClassName(superClass.getName());
	mappedSuperclass.addAttributeDatas(attrs);
	mappedSuperclass.setCtClass(superClass);

	List<ManagedData> embeddables = new ArrayList<>();
	createEmbeddables(attrs, embeddables, inspectedClasses);
	mappedSuperclass.getEmbeddables().addAll(embeddables);

	return Optional.of(mappedSuperclass);
    }

    private ManagedData findInspectedMappedSuperclass(List<ManagedData> enhancedClasses, String superclassName) {
	for (ManagedData enhEntity : enhancedClasses) {
	    ManagedData mappedSuperclassEnhEntity = enhEntity.mappedSuperclass;
	    if (mappedSuperclassEnhEntity != null && mappedSuperclassEnhEntity.getClassName().equals(superclassName))
		return mappedSuperclassEnhEntity;
	}

	return null;
    }

    private void createEmbeddables(List<AttributeData> dataAttributes, List<ManagedData> embeddables,
	    List<ManagedData> inspectedClasses) {
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

	ExprEditorExt exprEditorExt = new ExprEditorExt();
	CtConstructor[] ctConstructors = ct.getConstructors();
	for (CtConstructor ctConstructor : ctConstructors) {
	    ctConstructor.instrument(exprEditorExt);
	    List<BMTFieldInfo> fieldInfos = exprEditorExt.getFieldInfos();
	    if (log)
		LOG.info("inspectConstructorsAndMethods: fieldInfos.size()=" + fieldInfos.size());
	    if (!fieldInfos.isEmpty()) {
		BMTMethodInfo methodInfo = new BMTMethodInfo();
		methodInfo.ctConstructor = ctConstructor;
		methodInfo.addFieldInfos(fieldInfos);
		methodInfos.add(methodInfo);
	    }

	    exprEditorExt.clear();
	}

	return methodInfos;
    }

    private long countAttributesToEnhance(List<Property> properties) {
	return properties.stream().filter(p -> !p.id).count();
    }

    private AttributeData createAttributeFromProperty(Property property, boolean parentIsEmbeddedId) throws Exception {
	if (log)
	    LOG.info("createAttributeFromProperty: property.ctField.getName()=" + property.ctField.getName()
		    + "; property.embedded=" + property.embedded + "; property.ctField.getType().getName()="
		    + property.ctField.getType().getName());
//		List<AttributeData> embeddedAttributes = null;
//		CtClass embeddedCtClass = null;
	ManagedData embeddedData = null;
	if (property.embedded) {
	    embeddedData = new ManagedData(ManagedData.EMBEDDABLE);
	    embeddedData.addAttributeDatas(createDataAttributes(property.embeddedProperties, property.id));
	    embeddedData.setCtClass(property.ctField.getType());
	    embeddedData.setClassName(property.ctField.getType().getName());
//			embeddedAttributes = createDataAttributes(property.embeddedProperties, property.id);
//			embeddedCtClass = property.ctField.getType();
	}

	AttributeData dataAttribute = new AttributeData(property, parentIsEmbeddedId, embeddedData);
	return dataAttribute;
    }

    private List<Property> findAttributes(CtClass ctClass) throws Exception {
//		CtBehavior[] ctBehaviors = ctClass.getDeclaredBehaviors();
//		for(CtBehavior ctBehavior:ctBehaviors) {
//			LOG.info("findAttributes: ctField.getName()=" + ctBehavior.);
//		}

	CtField[] ctFields = ctClass.getDeclaredFields();
	List<Property> attrs = new ArrayList<>();
	for (CtField ctField : ctFields) {
	    Optional<Property> optional = readAttribute(ctField, ctClass);
	    if (optional.isPresent())
		attrs.add(optional.get());
	}

	return attrs;
    }

    private Optional<Property> readAttribute(CtField ctField, CtClass ctClass) throws Exception {
	if (log)
	    LOG.info("readAttribute: ctField.getName()=" + ctField.getName());
	if (log)
	    LOG.info("readAttribute: ctField.getModifiers()=" + ctField.getModifiers());
	if (log)
	    LOG.info("readAttribute: ctField.getType().getName()=" + ctField.getType().getName());
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
	    embeddedProperties = findAttributes(ctField.getType());
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

	Property property = new Property(id, getPropertyMethod, setPropertyMethod, ctField, embedded,
		embeddedProperties);
	return Optional.of(property);
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

//			LOG.info("findGetMethod: getMethod.getName()=" + getMethod.getName());
	CtClass[] params = getMethod.getParameterTypes();
//			LOG.info("findGetMethod: params.length=" + params.length);
	if (params.length != 0)
	    return new PropertyMethod();

//			LOG.info("findGetMethod: ctField.getType().getName()=" + ctField.getType().getName());
//			LOG.info("findGetMethod: getMethod.getReturnType().getName()=" + getMethod.getReturnType().getName());
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

//			LOG.info("findSetMethod: setMethod.getName()=" + setMethod.getName());
	CtClass[] params = setMethod.getParameterTypes();
//			LOG.info("findSetMethod: params.length=" + params.length);
	if (params.length != 1)
	    return new PropertyMethod();

	if (!ctField.getType().subtypeOf(params[0]))
	    return new PropertyMethod();

//			LOG.info("findSetMethod: setMethod.getReturnType().getName()=" + setMethod.getReturnType().getName());
	if (!setMethod.getReturnType().getName().equals(Void.TYPE.getName())) // void type
	    return new PropertyMethod();

//			LOG.info("findSetMethod: subtypeOf=true");
	return new PropertyMethod(Optional.of(setMethod), true);
    }

    private class ExprEditorExt extends ExprEditor {

	public final int NO_OP = -1;
	public final int CONSTRUCTOR_CALL_OP = 0;
	public final int NEW_EXPR_OP = 1;
	public final int METHOD_CALL_OP = 2;
	private int latestOpType = NO_OP;
	private String name;
	private List<BMTFieldInfo> fieldInfos = new ArrayList<>();

	public List<BMTFieldInfo> getFieldInfos() {
	    return fieldInfos;
	}

	public void clear() {
	    latestOpType = NO_OP;
	    name = null;
	    fieldInfos.clear();
	}

//		@Override
//		public boolean doit(CtClass clazz, MethodInfo minfo) throws CannotCompileException {
//			return false;
//		}
	@Override
	public void edit(NewExpr e) throws CannotCompileException {
	    latestOpType = NEW_EXPR_OP;
	    name = e.getClassName();
	}

	@Override
	public void edit(NewArray a) throws CannotCompileException {
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
	    LOG.info("ExprEditorExt: f.getFieldName()=" + f.getFieldName());
	    LOG.info("ExprEditorExt: fieldInfos.size()=" + fieldInfos.size());
	    if (latestOpType == CONSTRUCTOR_CALL_OP) {
		BMTFieldInfo fieldInfo = new BMTFieldInfo(BMTFieldInfo.ASSIGNMENT, f.getFieldName(), null);
		fieldInfos.add(fieldInfo);
	    } else if (latestOpType == NEW_EXPR_OP) {
		BMTFieldInfo fieldInfo = new BMTFieldInfo(BMTFieldInfo.ASSIGNMENT, f.getFieldName(), name);
		fieldInfos.add(fieldInfo);
	    } else if (latestOpType == METHOD_CALL_OP) {
		BMTFieldInfo fieldInfo = new BMTFieldInfo(BMTFieldInfo.ASSIGNMENT, f.getFieldName(), null);
		fieldInfos.add(fieldInfo);
	    }

	    latestOpType = NO_OP;
	}

	@Override
	public void edit(Instanceof i) throws CannotCompileException {
	    latestOpType = NO_OP;
	}

	@Override
	public void edit(Cast c) throws CannotCompileException {
	    latestOpType = NO_OP;
	}

	@Override
	public void edit(Handler h) throws CannotCompileException {
	    latestOpType = NO_OP;
	}

    }

}
