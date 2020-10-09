package org.tinyjpa.metadata.enhancer.javassist;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.Entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.CollectionUtils;
import org.tinyjpa.metadata.BeanUtil;
import org.tinyjpa.metadata.EntityDelegate;
import org.tinyjpa.metadata.enhancer.EnhAttribute;
import org.tinyjpa.metadata.enhancer.EnhEntity;
import org.tinyjpa.metadata.enhancer.Enhanced;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
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

public class EntityEnhancer {
	private Logger LOG = LoggerFactory.getLogger(EntityEnhancer.class);

	private List<String> classNames;
	private List<ManagedData> enhancedDataEntities = new ArrayList<>();
//	private List<ManagedData> dataEntities;
	private ClassInspector classInspector = new ClassInspector();

	public EntityEnhancer() {
		super();
	}

	public EntityEnhancer(List<String> classNames) {
		super();
		this.classNames = classNames;
	}

//	public List<EnhEntity> enhance() throws Exception {
//		List<ManagedData> dataEntities = classInspector.inspect(classNames);
//		List<EnhEntity> enhEntities = new ArrayList<>();
//		for (ManagedData dataEntity : dataEntities) {
//			EnhEntity enhMappedSuperclassEntity = null;
//			LOG.info("enhance: dataEntity.mappedSuperclass=" + dataEntity.mappedSuperclass);
//			if (dataEntity.mappedSuperclass != null) {
//				LOG.info(
//						"enhance: dataEntity.mappedSuperclass.className=" + dataEntity.mappedSuperclass.getClassName());
//				enhMappedSuperclassEntity = enhance(dataEntity.mappedSuperclass, enhEntities);
//			}
//
//			EnhEntity enhEntity = enhance(dataEntity, enhEntities);
//			enhEntity.setMappedSuperclass(enhMappedSuperclassEntity);
//			enhEntities.add(enhEntity);
//		}
//
//		return enhEntities;
//	}

	public EnhEntity enhance(ManagedData managedData, List<EnhEntity> parsedEntities) throws Exception {
		LOG.info("enhance: managedData.className=" + managedData.getClassName());
		EnhEntity enhEntity = new EnhEntity();
		enhEntity.setClassName(managedData.getClassName());
		List<EnhAttribute> enhAttributes = enhance(managedData);
		LOG.info("enhance: attributes created for " + managedData.getClassName());
		enhEntity.setEnhAttributes(enhAttributes);
		List<EnhEntity> embeddables = findEmbeddables(enhAttributes, parsedEntities);
		enhEntity.addEmbeddables(embeddables);
		LOG.info("enhance: completed '" + managedData.getClassName() + "'");
		return enhEntity;
	}

	private List<EnhEntity> findEmbeddables(List<EnhAttribute> enhAttributes, List<EnhEntity> parsedEntities) {
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

	private EnhEntity findInspectedEnhEmbeddables(String className, List<EnhEntity> parsedEntities) {
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

	private void printInfo(CtClass ct) throws Exception {
		CtConstructor[] ctConstructors = ct.getConstructors();
		for (CtConstructor ctConstructor : ctConstructors) {

			ctConstructor.instrument(new ExprEditor() {
				@Override
				public void edit(NewExpr e) throws CannotCompileException {
					LOG.info("printInfo.constructor.edit(NewExpr): e.getClassName()=" + e.getClassName());
					try {
						LOG.info("printInfo.constructor.edit(NewExpr): e.getConstructor().getName()="
								+ e.getConstructor().getName());
					} catch (NotFoundException e1) {
					}
					LOG.info("printInfo.constructor.edit(NewExpr): e.getLineNumber()=" + e.getLineNumber());
					LOG.info("printInfo.constructor.edit(NewExpr): e.getSignature()=" + e.getSignature());
				}

				@Override
				public void edit(NewArray a) throws CannotCompileException {
					LOG.info("printInfo.constructor.edit(NewArray): a.getLineNumber()=" + a.getLineNumber());
				}

				@Override
				public void edit(MethodCall m) throws CannotCompileException {
					LOG.info("printInfo.constructor.edit(MethodCall): m.getLineNumber()=" + m.getLineNumber());
					try {
						LOG.info("printInfo.constructor.edit(MethodCall): m.getMethod().getName()="
								+ m.getMethod().getName());
					} catch (NotFoundException e) {
					}
				}

				@Override
				public void edit(ConstructorCall c) throws CannotCompileException {
					LOG.info("printInfo.constructor.edit(ConstructorCall): c.getLineNumber()=" + c.getLineNumber());
					LOG.info("printInfo.constructor.edit(ConstructorCall): c.getMethodName()=" + c.getMethodName());
					LOG.info("printInfo.constructor.edit(ConstructorCall): c.getClassName()=" + c.getClassName());
					LOG.info("printInfo.constructor.edit(ConstructorCall): c.getSignature()=" + c.getSignature());
				}

				@Override
				public void edit(FieldAccess f) throws CannotCompileException {
					LOG.info("printInfo.constructor.edit(FieldAccess): f.getFieldName()=" + f.getFieldName());
					LOG.info("printInfo.constructor.edit(FieldAccess): f.getLineNumber()=" + f.getLineNumber());
					LOG.info("printInfo.constructor.edit(FieldAccess): f.getSignature()=" + f.getSignature());
				}

				@Override
				public void edit(Instanceof i) throws CannotCompileException {
					try {
						LOG.info("printInfo.constructor.edit(Cast): i.getType().getName()=" + i.getType().getName());
					} catch (NotFoundException e) {
					}
				}

				@Override
				public void edit(Cast c) throws CannotCompileException {
					try {
						LOG.info("printInfo.constructor.edit(Cast): c.getType().getName()=" + c.getType().getName());
					} catch (NotFoundException e) {
					}
				}

				@Override
				public void edit(Handler h) throws CannotCompileException {
					LOG.info("printInfo.constructor.edit(Handler): c.getLineNumber()=" + h.getLineNumber());
				}

			});

		}
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

//		if (!enhancedDataEntities.contains(managedData)) {
//			printInfo(ct);
//		}

		LOG.info("enhance: ct.isFrozen()=" + ct.isFrozen() + "; isClassModified(ct)=" + isClassModified(ct));
//		if (ct.isFrozen() && !isClassModified(ct)) {
//			ct.defrost();
//		}

		LOG.info("enhance: isClassWritable(ct)=" + isClassWritable(ct));
		boolean modified = false;
		boolean canModify = canModify(ct);
		if (!enhancedDataEntities.contains(managedData)) {
			if (toEnhance(managedData)) {
				LOG.info("Enhancing: " + ct.getName());
				addEntityDelegateField(ct);
				modified = true;
			} else {
				LOG.info("Enhancing: " + ct.getName() + " not necessary");
			}
		}

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
				if (property.getPropertyMethod.enhance) {
					if (isLazyOrEntityType(property.getPropertyMethod.method.get().getReturnType()))
						modifyGetMethod(property.getPropertyMethod.method.get(), property.ctField);
				}

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
		if (managedData != null)
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

			if (bmtFieldInfo.implementation != null) {
				// an implementation class. It can be a collection. NEW_EXPR_OP
				if (CollectionUtils.isCollectionName(bmtFieldInfo.implementation)) {
					modifyConstructorWithCollectionCheck(bmtMethodInfo.ctConstructor, optional.get().property.ctField);
				} else {
					modifyConstructorWithSimpleField(bmtMethodInfo.ctConstructor, optional.get().property.ctField);
				}
			} else {
				modifyConstructorWithSimpleField(bmtMethodInfo.ctConstructor, optional.get().property.ctField);
			}
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

//		if (isEntityName(name))
//			return true;

		return CollectionUtils.isCollectionName(name);
	}

//	private boolean isEntityName(String name) {
//		return dataEntities.stream().filter(d -> d.getClassName().equals(name)).findFirst().isPresent();
//	}

}
