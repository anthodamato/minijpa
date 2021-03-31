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

public class EntityEnhancer {

    private final Logger LOG = LoggerFactory.getLogger(EntityEnhancer.class);

    private final List<ManagedData> enhancedDataEntities = new ArrayList<>();

    public EntityEnhancer() {
	super();
    }

    public EnhEntity enhance(ManagedData managedData, Set<EnhEntity> parsedEntities) throws Exception {
	Optional<EnhEntity> optional = parsedEntities.stream().filter(e -> e.getClassName().equals(managedData.getClassName())).findFirst();
	if (optional.isPresent())
	    return optional.get();

	EnhEntity enhMappedSuperclassEntity = null;
	if (managedData.mappedSuperclass != null)
	    enhMappedSuperclassEntity = enhance(managedData.mappedSuperclass, parsedEntities);

	EnhEntity enhEntity = new EnhEntity();
	enhEntity.setClassName(managedData.getClassName());

	CtClass ct = managedData.getCtClass();
	LOG.debug("enhance: ct.getName()=" + ct.getName());
	LOG.debug("enhance: ct.isFrozen()=" + ct.isFrozen() + "; isClassModified(ct)=" + isClassModified(ct));
	LOG.debug("enhance: isClassWritable(ct)=" + isClassWritable(ct));
	boolean modified = false;
	boolean canModify = canModify(ct);
	if (!enhancedDataEntities.contains(managedData))
	    if (toEnhance(managedData)) {
		LOG.debug("Enhancing " + ct.getName());
		addEntityDelegateField(ct);
		// modification field
		addModificationField(ct, managedData.getModificationAttribute());
		CtMethod ctMethod = createGetMethod(ct, managedData.getModificationAttribute(), "java.util.List");
		enhEntity.setModificationAttributeGetMethod(ctMethod.getName());
		// lazy loaded attribute tracking
		if (managedData.getLazyLoadedAttribute().isPresent()) {
		    addLazyLoadedField(ct, managedData.getLazyLoadedAttribute().get());
		    ctMethod = createGetMethod(ct, managedData.getLazyLoadedAttribute().get(), "java.util.List");
		    enhEntity.setLazyLoadedAttributeGetMethod(Optional.of(ctMethod.getName()));
		}
		// lock type field
		if (managedData.getLockTypeAttribute().isPresent()) {
		    addLockTypeField(ct, managedData.getLockTypeAttribute().get());
		    // get method
		    ctMethod = createGetMethod(ct, managedData.getLockTypeAttribute().get(), "org.minijpa.jdbc.LockType");
		    enhEntity.setLockTypeAttributeGetMethod(Optional.of(ctMethod.getName()));
		    // set method
		    ctMethod = createSetMethod(ct, managedData.getLockTypeAttribute().get(), "org.minijpa.jdbc.LockType");
		    enhEntity.setLockTypeAttributeSetMethod(Optional.of(ctMethod.getName()));
		}

		modified = true;
	    } else
		LOG.debug("Enhancement of '" + ct.getName() + "' not needed");

	enhanceConstructor(managedData);

	boolean enhanceAttribute = false;
	List<EnhAttribute> enhAttributes = new ArrayList<>();
	List<AttributeData> dataAttributes = managedData.getAttributeDataList();
	for (AttributeData attributeData : dataAttributes) {
	    Property property = attributeData.property;
	    LOG.debug("Enhancing attribute '" + property.ctField.getName() + "'");
	    enhanceAttribute = toEnhance(attributeData);
	    if (property.setPropertyMethod.add && !enhancedDataEntities.contains(managedData)) {
		CtMethod ctMethod = createSetMethod(ct, property.ctField, enhanceAttribute, managedData);
		property.setPropertyMethod.enhance = false;
		property.setPropertyMethod.method = Optional.of(ctMethod);
		modified = true;
	    }

	    if (enhanceAttribute && !enhancedDataEntities.contains(managedData) && canModify(ct)) {
		if (property.getPropertyMethod.enhance)
		    if (isLazyOrEntityType(property.getPropertyMethod.method.get().getReturnType()))
			modifyGetMethod(property.getPropertyMethod.method.get(), property.ctField);

		if (property.setPropertyMethod.enhance)
		    modifySetMethod(property.setPropertyMethod.method.get(), property.ctField, managedData);
	    }

	    EnhEntity embeddedEnhEntity = null;
	    List<EnhAttribute> enhEmbeddedAttributes = null;
	    if (attributeData.embeddedData != null) {
		embeddedEnhEntity = enhance(attributeData.embeddedData, parsedEntities);
	    }

	    EnhAttribute enhAttribute = new EnhAttribute(property.ctField.getName(),
		    property.ctField.getType().getName(), property.ctField.getType().isPrimitive(),
		    property.getPropertyMethod.method.get().getName(),
		    property.setPropertyMethod.method.get().getName(), property.embedded, enhEmbeddedAttributes,
		    embeddedEnhEntity);
	    enhAttributes.add(enhAttribute);
	}

	LOG.debug("enhance: modified=" + modified + "; canModify(ct)=" + canModify(ct) + "; ct.isModified()="
		+ ct.isModified());

	LOG.debug("enhance: managedData=" + managedData);
	enhancedDataEntities.add(managedData);

	enhEntity.setEnhAttributes(enhAttributes);

	enhEntity.setMappedSuperclass(enhMappedSuperclassEntity);
	parsedEntities.add(enhEntity);

	return enhEntity;
    }

    private boolean toEnhance(AttributeData dataAttribute) {
	if (dataAttribute.property.id)
	    return false;

	if (dataAttribute.parentIsEmbeddedId)
	    return false;

	if (!dataAttribute.property.getPropertyMethod.enhance && !dataAttribute.property.setPropertyMethod.enhance)
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
			modifyConstructorWithCollectionCheck(bmtMethodInfo.ctConstructor, optional.get().property.ctField, managedData);
		    else
			modifyConstructorWithSimpleField(bmtMethodInfo.ctConstructor, optional.get().property.ctField, managedData);
		else
		    modifyConstructorWithSimpleField(bmtMethodInfo.ctConstructor, optional.get().property.ctField, managedData);
	    }
	}
    }

    private void addEntityDelegateField(CtClass ct) throws Exception {
	if (!canModify(ct))
	    return;

	LOG.debug("addEntityDelegateField: ct.getName()=" + ct.getName());
	ClassPool pool = ClassPool.getDefault();
	pool.importPackage(EntityDelegate.class.getPackage().getName());
	CtField f = CtField.make("private EntityDelegate entityDelegate = EntityDelegate.getInstance();", ct);
	LOG.debug("Adding Entity Delegate");
	ct.addField(f);
    }

    private void addModificationField(CtClass ct, String modificationFieldName) throws Exception {
	if (!canModify(ct))
	    return;

	CtField f = CtField.make("private java.util.List " + modificationFieldName + " = new java.util.ArrayList();", ct);
	ct.addField(f);
	LOG.debug("Created '" + ct.getName() + "' Modification Field");
    }

    private void addLazyLoadedField(CtClass ct, String fieldName) throws Exception {
	if (!canModify(ct))
	    return;

	CtField f = CtField.make("private java.util.List " + fieldName + " = new java.util.ArrayList();", ct);
	ct.addField(f);
	LOG.debug("Created '" + ct.getName() + "' Lazy Loaded Field");
    }

    private void addLockTypeField(CtClass ct, String fieldName) throws Exception {
	if (!canModify(ct))
	    return;

	String f = "private org.minijpa.jdbc.LockType " + fieldName + " = org.minijpa.jdbc.LockType.NONE;";
	CtField ctField = CtField.make(f, ct);
	ct.addField(ctField);
	LOG.debug("Created '" + ct.getName() + "' Field: " + f);
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
	LOG.debug("Modifying get method: mc=" + mc);
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
	LOG.debug("Modifying set method: mc=" + mc);
	ctMethod.insertBefore(mc);
    }

    private void modifyConstructorWithCollectionCheck(CtConstructor ctConstructor, CtField ctField,
	    ManagedData managedData) throws Exception {
	String mc = "if(!" + ctField.getName() + ".isEmpty()) " + managedData.getModificationAttribute() + ".add(\"" + ctField.getName() + "\");";
	LOG.debug("Modifying constructor: mc=" + mc);
	ctConstructor.insertAfter(mc);
    }

    private void modifyConstructorWithSimpleField(CtConstructor ctConstructor, CtField ctField,
	    ManagedData managedData) throws Exception {
	String mc = managedData.getModificationAttribute() + ".add(\"" + ctField.getName() + "\");";
	LOG.debug("Modifying constructor: mc=" + mc);
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

    private String createSetMethodString(CtField ctField, boolean delegate, int counter,
	    ManagedData managedData) throws Exception {
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
	LOG.debug("createSetMethod: Created new method: " + setMethodString);
	return ctMethod;
    }

    private CtMethod createSetMethod(CtClass ctClass, String fieldName, String fieldTypeName)
	    throws Exception {
	if (!canModify(ctClass))
	    return null;

	String setMethodString = createSetMethodString(fieldName, fieldTypeName);
	CtMethod ctMethod = CtNewMethod.make(setMethodString, ctClass);
	ctClass.addMethod(ctMethod);
	LOG.debug("Created '" + ctClass.getName() + "' method: " + setMethodString);
	return ctMethod;
    }

    private CtMethod createGetMethod(CtClass ctClass, String fieldName, String fieldTypeName) throws Exception {
	if (!canModify(ctClass))
	    return null;

	String getMethodString = createGetMethodString(fieldName, fieldTypeName);
	CtMethod ctMethod = CtNewMethod.make(getMethodString, ctClass);
	ctClass.addMethod(ctMethod);
	LOG.debug("Created '" + ctClass.getName() + "' method: " + getMethodString);
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
