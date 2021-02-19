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
	EnhEntity enhMappedSuperclassEntity = null;
	if (managedData.mappedSuperclass != null)
	    enhMappedSuperclassEntity = enhance(managedData.mappedSuperclass, parsedEntities);

	LOG.info("enhance: managedData.className=" + managedData.getClassName());
	EnhEntity enhEntity = new EnhEntity();
	enhEntity.setClassName(managedData.getClassName());
	List<EnhAttribute> enhAttributes = enhance(managedData);
	LOG.info("enhance: attributes created for " + managedData.getClassName());
	enhEntity.setEnhAttributes(enhAttributes);
	List<EnhEntity> embeddables = findEmbeddables(enhAttributes, parsedEntities);
	enhEntity.addEmbeddables(embeddables);
	LOG.info("enhance: completed '" + managedData.getClassName() + "'");

	enhEntity.setMappedSuperclass(enhMappedSuperclassEntity);
	parsedEntities.add(enhEntity);

	return enhEntity;
    }

    private List<EnhEntity> findEmbeddables(List<EnhAttribute> enhAttributes, Set<EnhEntity> parsedEntities) {
	List<EnhEntity> embeddables = new ArrayList<>();
	for (EnhAttribute enhAttribute : enhAttributes) {
	    LOG.info("findEmbeddables: enhAttribute.getName()=" + enhAttribute.getName()
		    + "; enhAttribute.isEmbedded()=" + enhAttribute.isEmbedded());
	    if (!enhAttribute.isEmbedded())
		continue;

	    EnhEntity embeddable = findInspectedEnhEmbeddables(enhAttribute.getClassName(), parsedEntities);
	    LOG.info("findEmbeddables: already parsed embeddable=" + embeddable);
	    if (embeddable == null) {
		embeddable = new EnhEntity();
		embeddable.setClassName(enhAttribute.getClassName());
		embeddable.setEnhAttributes(enhAttribute.getEmbeddedAttributes());
	    }

	    LOG.info("findEmbeddables: got embeddable=" + embeddable);
	    embeddables.add(embeddable);
	}

	return embeddables;
    }

    private EnhEntity findInspectedEnhEmbeddables(String className, Set<EnhEntity> parsedEntities) {
	for (EnhEntity enhEntity : parsedEntities) {
	    if (enhEntity.getClassName().equals(className))
		return enhEntity;
	}

	return null;
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
	for (AttributeData dataAttribute : managedData.getDataAttributes()) {
	    if (toEnhance(dataAttribute))
		return true;
	}

	if (!managedData.getMethodInfos().isEmpty())
	    return true;

	return false;
    }

    private boolean isClassModified(CtClass ctClass) throws NotFoundException {
	CtClass[] ctInterfaces = ctClass.getInterfaces();
//		LOG.info("isClassModified: ctInterfaces.length=" + ctInterfaces.length);
	for (CtClass ct : ctInterfaces) {
//			LOG.info("isClassModified: ct.getName()=" + ct.getName());
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

    private List<EnhAttribute> enhance(ManagedData managedData) throws Exception {
	CtClass ct = managedData.getCtClass();
	LOG.info("enhance: ct.getName()=" + ct.getName());
	LOG.info("enhance: ct.isFrozen()=" + ct.isFrozen() + "; isClassModified(ct)=" + isClassModified(ct));
//		if (ct.isFrozen() && !isClassModified(ct)) {
//			ct.defrost();
//		}

	LOG.info("enhance: isClassWritable(ct)=" + isClassWritable(ct));
	boolean modified = false;
	boolean canModify = canModify(ct);
	if (!enhancedDataEntities.contains(managedData))
	    if (toEnhance(managedData)) {
		LOG.info("Enhancing: " + ct.getName());
		addEntityDelegateField(ct);
		modified = true;
	    } else
		LOG.info("Enhancing: " + ct.getName() + " not necessary");

	for (BMTMethodInfo bmtMethodInfo : managedData.getMethodInfos()) {
	    enhanceConstructor(managedData, bmtMethodInfo);
	}

	boolean enhanceAttribute = false;
	List<EnhAttribute> enhAttributes = new ArrayList<>();
	List<AttributeData> dataAttributes = managedData.getDataAttributes();
	for (AttributeData dataAttribute : dataAttributes) {
	    Property property = dataAttribute.property;
	    LOG.info("Enhancing attribute '" + property.ctField.getName() + "'");
	    enhanceAttribute = toEnhance(dataAttribute);
	    if (property.setPropertyMethod.add && !enhancedDataEntities.contains(managedData)) {
		CtMethod ctMethod = createSetMethod(ct, property.ctField, enhanceAttribute);
		property.setPropertyMethod.enhance = false;
		property.setPropertyMethod.method = Optional.of(ctMethod);
		modified = true;
	    }

	    if (enhanceAttribute && !enhancedDataEntities.contains(managedData) && canModify(ct)) {
		if (property.getPropertyMethod.enhance)
		    if (isLazyOrEntityType(property.getPropertyMethod.method.get().getReturnType()))
			modifyGetMethod(property.getPropertyMethod.method.get(), property.ctField);

		if (property.setPropertyMethod.enhance)
		    modifySetMethod(property.setPropertyMethod.method.get(), property.ctField);
	    }

	    List<EnhAttribute> enhEmbeddedAttributes = null;
	    if (dataAttribute.embeddedData != null)
		enhEmbeddedAttributes = enhance(dataAttribute.embeddedData);

	    EnhAttribute enhAttribute = new EnhAttribute(property.ctField.getName(),
		    property.ctField.getType().getName(), property.ctField.getType().isPrimitive(),
		    property.getPropertyMethod.method.get().getName(),
		    property.setPropertyMethod.method.get().getName(), property.embedded, enhEmbeddedAttributes);
	    enhAttributes.add(enhAttribute);
	}

	LOG.info("enhance: modified=" + modified + "; canModify(ct)=" + canModify(ct) + "; ct.isModified()="
		+ ct.isModified());
//		if (modified) {
//			if (canModify) {
//				LOG.info("enhance: writing class " + ct.getName());
////				addEnhancedInterface(ct);
////				ct.toClass(getClass().getClassLoader(), getClass().getProtectionDomain());
//			}
//		}

	LOG.info("enhance: managedData=" + managedData);
	enhancedDataEntities.add(managedData);

	return enhAttributes;
    }

    private void enhanceConstructor(ManagedData managedData, BMTMethodInfo bmtMethodInfo) throws Exception {
	if (!canModify(managedData.getCtClass()))
	    return;

	for (BMTFieldInfo bmtFieldInfo : bmtMethodInfo.getBmtFieldInfos()) {
	    Optional<AttributeData> optional = managedData.findAttribute(bmtFieldInfo.name);
	    if (!optional.isPresent())
		throw new Exception("Field '" + bmtFieldInfo.name + "' not found");

	    if (bmtFieldInfo.implementation != null)
		// an implementation class. It can be a collection. NEW_EXPR_OP
		if (CollectionUtils.isCollectionName(bmtFieldInfo.implementation))
		    modifyConstructorWithCollectionCheck(bmtMethodInfo.ctConstructor, optional.get().property.ctField);
		else
		    modifyConstructorWithSimpleField(bmtMethodInfo.ctConstructor, optional.get().property.ctField);
	    else
		modifyConstructorWithSimpleField(bmtMethodInfo.ctConstructor, optional.get().property.ctField);
	}
    }

    private void addEntityDelegateField(CtClass ct) throws Exception {
//		LOG.info("addEntityDelegateField: canModify(ct)=" + canModify(ct));
	if (!canModify(ct))
	    return;

	LOG.info("addEntityDelegateField: ct.getName()=" + ct.getName());
	ClassPool pool = ClassPool.getDefault();
	Class<?> delegateClass = EntityDelegate.class;
	pool.importPackage(delegateClass.getPackage().getName());
	CtField f = CtField.make("private EntityDelegate entityDelegate = EntityDelegate.getInstance();", ct);
	LOG.info("Adding Entity Delegate");
	ct.addField(f);
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
	LOG.info("Modifying get method: mc=" + mc);
	ctMethod.insertBefore(mc);
    }

    private void modifySetMethod(CtMethod ctMethod, CtField ctField) throws Exception {
	String mc = "entityDelegate.set(" + ctField.getName() + ",\"" + ctField.getName() + "\", this);";
	LOG.info("Modifying set method: mc=" + mc);
	ctMethod.insertBefore(mc);
    }

    private void modifyConstructorWithCollectionCheck(CtConstructor ctConstructor, CtField ctField) throws Exception {
	String mc = "if(!" + ctField.getName() + ".isEmpty()) { entityDelegate.set(" + ctField.getName() + ",\""
		+ ctField.getName() + "\", this); }";
	LOG.info("Modifying constructor: mc=" + mc);
	ctConstructor.insertAfter(mc);
    }

    private void modifyConstructorWithSimpleField(CtConstructor ctConstructor, CtField ctField) throws Exception {
	String mc = "entityDelegate.set(" + ctField.getName() + ",\"" + ctField.getName() + "\", this);";
	LOG.info("Modifying constructor: mc=" + mc);
	ctConstructor.insertAfter(mc);
    }

    private String buildSetMethodName(CtField ctField, int counter) {
	StringBuilder sb = new StringBuilder();
	sb.append("set");
	sb.append(BeanUtil.capitalize(ctField.getName()));
	sb.append(Integer.toString(counter));
	return sb.toString();
    }

    private String createSetMethodString(CtField ctField, boolean delegate, int counter) throws Exception {
	StringBuilder sb = new StringBuilder();
	sb.append("public void ");
	sb.append(buildSetMethodName(ctField, counter));
	sb.append("(");
	sb.append(ctField.getType().getName());
	sb.append(" ");
	sb.append(ctField.getName());
	sb.append(") {");
	if (delegate) {
	    sb.append("entityDelegate.set(");
	    sb.append(ctField.getName());
	    sb.append(",\"");
	    sb.append(ctField.getName());
	    sb.append("\", this);");
	}

	sb.append(" this.");
	sb.append(ctField.getName());
	sb.append("=");
	sb.append(ctField.getName());
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
    private CtMethod createSetMethod(CtClass ctClass, CtField ctField, boolean delegate) throws Exception {
	int counter = 0;
	CtMethod ctMethod = null;
	for (int i = 0; i < 100; ++i) {
	    try {
		ctMethod = ctClass.getDeclaredMethod(buildSetMethodName(ctField, counter));
	    } catch (NotFoundException e) {
		break;
	    }

	    ++counter;
	}

	if (!canModify(ctClass))
	    return ctMethod;

	String setMethodString = createSetMethodString(ctField, delegate, counter);
	ctMethod = CtNewMethod.make(setMethodString, ctClass);
	ctClass.addMethod(ctMethod);
	LOG.info("createSetMethod: Created new method: " + setMethodString);
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
