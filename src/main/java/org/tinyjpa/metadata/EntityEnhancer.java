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

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;

public class EntityEnhancer {
	private Logger LOG = LoggerFactory.getLogger(EntityEnhancer.class);

	public List<EnhEntity> enhance(List<String> classNames) throws Exception {
		List<EnhEntity> enhancedClasses = new ArrayList<>();
		for (String className : classNames) {
			enhance(className, enhancedClasses);
		}

		return enhancedClasses;
	}

	private void enhance(String className, List<EnhEntity> enhancedClasses) throws Exception {
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
		List<EnhAttribute> attrs = enhance(ct, properties, false);
		enhEntity.setEnhAttributes(attrs);
		if (!attrs.isEmpty() || enhEntity.getMappedSuperclass() != null) {
			enhancedClasses.add(enhEntity);
		}
	}

	private Optional<EnhEntity> findMappedSuperclass(CtClass ct, List<EnhEntity> enhancedClasses) throws Exception {
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
		List<EnhAttribute> attrs = enhance(superClass, properties, false);
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

	private List<EnhAttribute> enhance(CtClass ct, List<Property> properties, boolean embeddedId) throws Exception {
//		List<Property> properties = findAttributes(ct);
//		LOG.info("Found " + properties.size() + " attributes in '" + ct.getName() + "'");
		List<EnhAttribute> attributes = new ArrayList<>();
		// nothing to do if there are no persistent attributes
		if (properties.isEmpty())
			return attributes;

		if (countAttributesToEnhance(properties) == 0)
			return attributes;

		if (!embeddedId) {
			LOG.info("Enhancing: " + ct.getName());
			addEntityDelegateField(ct);
		}

		for (Property property : properties) {
			EnhAttribute enhAttribute = createAttributeFromProperty(property, embeddedId);
			attributes.add(enhAttribute);
		}

		ct.toClass(getClass().getClassLoader(), getClass().getProtectionDomain());
		return attributes;
	}

	private void addEntityDelegateField(CtClass ct) throws Exception {
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

	private EnhAttribute createAttributeFromProperty(Property property, boolean parentIsEmbeddedId) throws Exception {
		LOG.info("createAttributeFromProperty: property.ctField.getName()=" + property.ctField.getName()
				+ "; property.embedded=" + property.embedded);
		List<EnhAttribute> embeddedAttributes = null;
		if (property.embedded) {
			embeddedAttributes = enhance(property.ctField.getType(), property.embeddedProperties, property.id);
		}

		if (!property.id && !parentIsEmbeddedId) {
			if (property.enhanceGet)
				modifyGetMethod(property.getMethod, property.ctField);

			if (property.enhanceSet)
				modifySetMethod(property.setMethod, property.ctField);
		}

		EnhAttribute enhAttribute = new EnhAttribute(property.ctField.getName(), property.ctField.getType().getName(),
				property.ctField.getType().isPrimitive(), property.getMethod.getName(), property.setMethod.getName(),
				property.embedded, embeddedAttributes);
		return enhAttribute;
	}

	private void modifyGetMethod(CtMethod ctMethod, CtField ctField) throws Exception {
		String mc = ctField.getName() + " = (" + ctMethod.getReturnType().getName() + ") entityDelegate.get("
				+ ctField.getName() + ",\"" + ctField.getName() + "\", this);";
//		LOG.info("modifyGetMethod: mc=" + mc);
		ctMethod.insertBefore(mc);
	}

	private void modifySetMethod(CtMethod ctMethod, CtField ctField) throws Exception {
		String mc = "entityDelegate.set(" + ctField.getName() + ",\"" + ctField.getName() + "\", this);";
//		LOG.info("modifySetMethod: mc=" + mc);
		ctMethod.insertBefore(mc);
	}

	private List<Property> findAttributes(CtClass ctClass) throws Exception {
//		CtBehavior[] ctBehaviors = ctClass.getDeclaredBehaviors();
//		for(CtBehavior ctBehavior:ctBehaviors) {
//			LOG.info("findAttributes: ctField.getName()=" + ctBehavior.);
//		}

		CtField[] ctFields = ctClass.getDeclaredFields();
		List<Property> attrs = new ArrayList<>();
		for (CtField ctField : ctFields) {
			LOG.info("findAttributes: ctField.getName()=" + ctField.getName());
			LOG.info("findAttributes: ctField.getModifiers()=" + ctField.getModifiers());
			LOG.info("findAttributes: ctField.getType().getName()=" + ctField.getType().getName());
			LOG.info("findAttributes: ctField.getSignature()=" + ctField.getSignature());
			LOG.info("findAttributes: ctField.getFieldInfo()=" + ctField.getFieldInfo());
			LOG.info("findAttributes: ctField.getFieldInfo2()=" + ctField.getFieldInfo2());
			int modifier = ctField.getModifiers();
			if (!Modifier.isPrivate(modifier) && !Modifier.isProtected(modifier) && !Modifier.isPackage(modifier))
				continue;

			Object transientAnnotation = ctField.getAnnotation(Transient.class);
			if (transientAnnotation != null)
				continue;

			PropertyMethod getPropertyMethod = findGetMethod(ctClass, ctField);
			if (!getPropertyMethod.method.isPresent())
				continue;

			PropertyMethod setPropertyMethod = findSetMethod(ctClass, ctField);
			if (!setPropertyMethod.method.isPresent())
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

			Property property = new Property(id, getPropertyMethod.method.get(), setPropertyMethod.method.get(),
					ctField, embedded, embeddedProperties, getPropertyMethod.enhance, setPropertyMethod.enhance);
			attrs.add(property);
		}

		return attrs;
	}

	private CtMethod findIsGetMethod(CtClass ctClass, CtField ctField) throws Exception {
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

	private PropertyMethod findGetMethod(CtClass ctClass, CtField ctField) throws Exception {
//			LOG.info("findGetMethod: ctField.getName()=" + ctField.getName());
		CtMethod getMethod = findIsGetMethod(ctClass, ctField);
//			CtMethod getMethod = ctClass.getDeclaredMethod(buildMethodName("get", ctField.getName()));
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

//			LOG.info("findGetMethod: subtypeOf=true");
		return new PropertyMethod(Optional.of(getMethod), true);
	}

	private PropertyMethod findSetMethod(CtClass ctClass, CtField ctField) throws Exception {
		CtMethod setMethod = ctClass.getDeclaredMethod("set" + BeanUtil.capitalize(ctField.getName()));
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

	private class Property {
		private boolean id;
		private CtMethod getMethod;
		private CtMethod setMethod;
		private CtField ctField;
		private boolean embedded;
		private List<Property> embeddedProperties;
		private boolean enhanceGet;
		private boolean enhanceSet;

		public Property(boolean id, CtMethod getMethod, CtMethod setMethod, CtField ctField, boolean embedded,
				List<Property> embeddedProperties, boolean enhanceGet, boolean enhanceSet) {
			super();
			this.id = id;
			this.getMethod = getMethod;
			this.setMethod = setMethod;
			this.ctField = ctField;
			this.embedded = embedded;
			this.embeddedProperties = embeddedProperties;
			this.enhanceGet = enhanceGet;
			this.enhanceSet = enhanceSet;
		}
	}

	private class PropertyMethod {
		private Optional<CtMethod> method = Optional.empty();
		private boolean enhance = true;

		public PropertyMethod() {
		}

		public PropertyMethod(Optional<CtMethod> method, boolean enhance) {
			this.method = method;
			this.enhance = enhance;
		}

	}
}
