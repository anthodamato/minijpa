package org.tinyjpa.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;

public class EntityEnhancer {
	private Logger LOG = LoggerFactory.getLogger(EntityEnhancer.class);

	public List<EnhEntity> enhance(List<String> classNames) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, NotFoundException, CannotCompileException {
		List<EnhEntity> enhancedClasses = new ArrayList<>();
		for (String className : classNames) {
			enhance(className, enhancedClasses);
		}

		return enhancedClasses;
	}

	private void enhance(String className, List<EnhEntity> enhancedClasses) throws NotFoundException,
			CannotCompileException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		// already enhanced
		for (EnhEntity enhEntity : enhancedClasses) {
			if (enhEntity.getClassName().equals(className))
				return;
		}

		ClassPool pool = ClassPool.getDefault();
//		LOG.info("Enhancing: " + className);

		CtClass ct = pool.get(className);
		// mapped superclasses are enhanced finding the entity superclasses
		Object mappedSuperclassAnnotation = ct.getAnnotation(MappedSuperclass.class);
		if (mappedSuperclassAnnotation != null)
			return;

		// skip embeddable classes
		Object embeddableAnnotation = ct.getAnnotation(Embeddable.class);
		if (embeddableAnnotation != null)
			return;

		Object entityAnnotation = ct.getAnnotation(javax.persistence.Entity.class);
		if (entityAnnotation == null) {
			LOG.error("@Entity annotation not found: " + ct.getName());
			return;
		}

		EnhEntity enhEntity = new EnhEntity();
		enhEntity.setClassName(className);

		Optional<EnhEntity> optional = findMappedSuperclass(ct, enhancedClasses);
		if (optional.isPresent())
			enhEntity.setMappedSuperclass(optional.get());

		List<Property> properties = findAttributes(ct);
		LOG.info("Found " + properties.size() + " attributes in '" + ct.getName() + "'");
		List<EnhAttribute> attrs = enhance(ct, properties);
		enhEntity.setEnhAttributes(attrs);
		if (!attrs.isEmpty() || enhEntity.getMappedSuperclass() != null) {
			enhancedClasses.add(enhEntity);
		}
	}

	private Optional<EnhEntity> findMappedSuperclass(CtClass ct, List<EnhEntity> enhancedClasses)
			throws NotFoundException, ClassNotFoundException, InstantiationException, IllegalAccessException,
			CannotCompileException {
		CtClass superClass = ct.getSuperclass();
		if (superClass == null)
			return Optional.empty();

		if (superClass.getName().equals("java.lang.Object"))
			return Optional.empty();

		LOG.info("superClass.getName()=" + superClass.getName());
		Object mappedSuperclassAnnotation = superClass.getAnnotation(MappedSuperclass.class);
		if (mappedSuperclassAnnotation == null)
			return Optional.empty();

		LOG.info("mappedSuperclassAnnotation=" + mappedSuperclassAnnotation);
		// checks if the mapped superclass id already enhanced
		EnhEntity mappedSuperclassEnhEntity = findEnhancedMappedSuperclass(enhancedClasses, superClass.getName());
		LOG.info("mappedSuperclassEnhEntity=" + mappedSuperclassEnhEntity);
		if (mappedSuperclassEnhEntity != null)
			return Optional.of(mappedSuperclassEnhEntity);

		List<Property> properties = findAttributes(superClass);
		LOG.info("Found " + properties.size() + " attributes in '" + superClass.getName() + "'");
		List<EnhAttribute> attrs = enhance(superClass, properties);
		LOG.info("attrs.size()=" + attrs.size());
		if (attrs.isEmpty())
			return Optional.empty();

		EnhEntity mappedSuperclass = new EnhEntity();
		mappedSuperclass.setClassName(superClass.getName());
		mappedSuperclass.setEnhAttributes(attrs);
		return Optional.of(mappedSuperclass);
	}

	private EnhEntity findEnhancedMappedSuperclass(List<EnhEntity> enhancedClasses, String superclassName) {
		for (EnhEntity enhEntity : enhancedClasses) {
			EnhEntity mappedSuperclassEnhEntity = enhEntity.getMappedSuperclass();
			if (mappedSuperclassEnhEntity != null && mappedSuperclassEnhEntity.getClassName().equals(superclassName))
				return mappedSuperclassEnhEntity;
		}

		return null;
	}

	private List<EnhAttribute> enhance(CtClass ct, List<Property> properties) throws NotFoundException,
			CannotCompileException, ClassNotFoundException, InstantiationException, IllegalAccessException {
//		List<Property> properties = findAttributes(ct);
//		LOG.info("Found " + properties.size() + " attributes in '" + ct.getName() + "'");
		List<EnhAttribute> attributes = new ArrayList<>();
		// nothing to do if there are no persistent attributes
		if (properties.isEmpty())
			return attributes;

		if (countAttributesToEnhance(properties) == 0)
			return attributes;

		LOG.info("Enhancing: " + ct.getName());
		addEntityDelegateField(ct);

		for (Property property : properties) {
			EnhAttribute enhAttribute = createAttributeFromProperty(property, false);
			attributes.add(enhAttribute);
		}

		ct.toClass(getClass().getClassLoader(), getClass().getProtectionDomain());
		return attributes;
	}

	private void addEntityDelegateField(CtClass ct) throws CannotCompileException {
		LOG.info("addEntityDelegateField: ct.getName()=" + ct.getName());
		ClassPool pool = ClassPool.getDefault();
		Class<?> delegateClass = EntityDelegate.class;
		pool.importPackage(delegateClass.getPackage().getName());
		CtField f = CtField.make("private EntityDelegate entityDelegate = EntityDelegate.getInstance();", ct);
		ct.addField(f);
	}

	private long countAttributesToEnhance(List<Property> properties) {
		return properties.stream().filter(p -> !p.id).count();
	}

	private EnhAttribute createAttributeFromProperty(Property property, boolean parentIsEmbeddable)
			throws CannotCompileException, NotFoundException, ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		LOG.info("createAttributeFromProperty: property.ctField.getName()=" + property.ctField.getName()
				+ "; property.embedded=" + property.embedded);
		List<EnhAttribute> embeddedAttributes = null;
		if (property.embedded) {
			embeddedAttributes = enhance(property.ctField.getType(), property.embeddedProperties);
		}

		if (!property.id) {
			modifyGetMethod(property.getMethod, property.ctField);
			modifySetMethod(property.setMethod, property.ctField);
		}

		EnhAttribute enhAttribute = new EnhAttribute(property.ctField.getName(), property.ctField.getType().getName(),
				property.ctField.getType().isPrimitive(), property.getMethod.getName(), property.setMethod.getName(),
				property.embedded, embeddedAttributes);
		return enhAttribute;
	}

	private void modifyGetMethod(CtMethod ctMethod, CtField ctField) throws CannotCompileException, NotFoundException {
		String mc = ctField.getName() + " = (" + ctMethod.getReturnType().getName() + ") entityDelegate.get("
				+ ctField.getName() + ",\"" + ctField.getName() + "\", this);";
//		LOG.info("modifyGetMethod: mc=" + mc);
		ctMethod.insertBefore(mc);
	}

	private void modifySetMethod(CtMethod ctMethod, CtField ctField) throws CannotCompileException, NotFoundException {
		String mc = "entityDelegate.set(" + ctField.getName() + ",\"" + ctField.getName() + "\", this);";
//		LOG.info("modifySetMethod: mc=" + mc);
		ctMethod.insertBefore(mc);
	}

	private List<Property> findAttributes(CtClass ctClass)
			throws CannotCompileException, NotFoundException, ClassNotFoundException {
		CtField[] ctFields = ctClass.getDeclaredFields();
		List<Property> attrs = new ArrayList<>();
		for (CtField ctField : ctFields) {
			LOG.info("findAttributes: ctField.getName()=" + ctField.getName());
			LOG.info("findAttributes: ctField.getModifiers()=" + ctField.getModifiers());
			int modifier = ctField.getModifiers();
			if (!Modifier.isPrivate(modifier) && !Modifier.isProtected(modifier) && !Modifier.isPackage(modifier))
				continue;

			Object transientAnnotation = ctField.getAnnotation(Transient.class);
			if (transientAnnotation != null)
				continue;

			Optional<CtMethod> getMethod = findGetMethod(ctClass, ctField);
			if (!getMethod.isPresent())
				continue;

			Optional<CtMethod> setMethod = findSetMethod(ctClass, ctField);
			if (!setMethod.isPresent())
				continue;

			Object idAnnotation = ctField.getAnnotation(Id.class);
			Object embeddedIdAnnotation = ctField.getAnnotation(EmbeddedId.class);
			boolean id = idAnnotation != null || embeddedIdAnnotation != null;

			boolean embedded = false;
			List<Property> embeddedProperties = null;
			Object embeddedAnnotation = ctField.getAnnotation(Embedded.class);
			if (embeddedAnnotation != null || embeddedIdAnnotation != null) {
				embedded = true;
				embeddedProperties = findAttributes(ctField.getType());
				if (embeddedProperties.isEmpty()) {
					embeddedProperties = null;
					embedded = false;
				}
			}

			Property property = new Property(id, getMethod.get(), setMethod.get(), ctField, embedded,
					embeddedProperties);
			attrs.add(property);
		}

		return attrs;
	}

	private CtMethod findIsGetMethod(CtClass ctClass, CtField ctField) throws NotFoundException {
		try {
//			LOG.info("findIsGetMethod: ctField.getName()=" + ctField.getName());
//			LOG.info("findIsGetMethod: ctField.getType()=" + ctField.getType());
//			LOG.info("findIsGetMethod: ctField.getType().getName()=" + ctField.getType().getName());
//			LOG.info("findIsGetMethod: ctField.getType().isPrimitive()=" + ctField.getType().isPrimitive());

			String methodName = "get" + BeanUtil.capitalize(ctField.getName());
//			LOG.info("findIsGetMethod: methodName=" + methodName);
			return ctClass.getDeclaredMethod(methodName);
		} catch (NotFoundException e) {
			if (ctField.getType().getName().equals("java.lang.Boolean")
					|| ctField.getType().getName().equals("boolean")) {
				return ctClass.getDeclaredMethod("is" + BeanUtil.capitalize(ctField.getName()));
			}
		}

		return null;
	}

	private Optional<CtMethod> findGetMethod(CtClass ctClass, CtField ctField) {
		try {
//			LOG.info("findGetMethod: ctField.getName()=" + ctField.getName());
			CtMethod getMethod = findIsGetMethod(ctClass, ctField);
//			CtMethod getMethod = ctClass.getDeclaredMethod(buildMethodName("get", ctField.getName()));
			if (getMethod == null)
				return Optional.empty();

//			LOG.info("findGetMethod: getMethod.getName()=" + getMethod.getName());
			CtClass[] params = getMethod.getParameterTypes();
//			LOG.info("findGetMethod: params.length=" + params.length);
			if (params.length != 0)
				return Optional.empty();

//			LOG.info("findGetMethod: ctField.getType().getName()=" + ctField.getType().getName());
//			LOG.info("findGetMethod: getMethod.getReturnType().getName()=" + getMethod.getReturnType().getName());
			if (!getMethod.getReturnType().subtypeOf(ctField.getType()))
				return Optional.empty();

//			LOG.info("findGetMethod: subtypeOf=true");
			return Optional.of(getMethod);
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	private Optional<CtMethod> findSetMethod(CtClass ctClass, CtField ctField) {
		try {
			CtMethod setMethod = ctClass.getDeclaredMethod("set" + BeanUtil.capitalize(ctField.getName()));
//			LOG.info("findSetMethod: setMethod.getName()=" + setMethod.getName());
			CtClass[] params = setMethod.getParameterTypes();
//			LOG.info("findSetMethod: params.length=" + params.length);
			if (params.length != 1)
				return Optional.empty();

			if (!ctField.getType().subtypeOf(params[0]))
				return Optional.empty();

//			LOG.info("findSetMethod: setMethod.getReturnType().getName()=" + setMethod.getReturnType().getName());
			if (!setMethod.getReturnType().getName().equals("void")) // void type
				return Optional.empty();

//			LOG.info("findSetMethod: subtypeOf=true");
			return Optional.of(setMethod);
		} catch (NotFoundException e) {
			return Optional.empty();
		}
	}

	private class Property {
		private boolean id;
		private CtMethod getMethod;
		private CtMethod setMethod;
		private CtField ctField;
		private boolean embedded;
		private List<Property> embeddedProperties;

		public Property(boolean id, CtMethod getMethod, CtMethod setMethod, CtField ctField, boolean embedded,
				List<Property> embeddedProperties) {
			super();
			this.id = id;
			this.getMethod = getMethod;
			this.setMethod = setMethod;
			this.ctField = ctField;
			this.embedded = embedded;
			this.embeddedProperties = embeddedProperties;
		}

	}
}
