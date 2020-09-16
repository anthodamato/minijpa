package org.tinyjpa.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
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

	private List<String> classNames;
	private List<DataEntity> enhancedDataEntities = new ArrayList<>();
	private List<DataEntity> dataEntities;

	public EntityEnhancer(List<String> classNames) {
		super();
		this.classNames = classNames;
	}

	public List<EnhEntity> enhance() throws Exception {
		dataEntities = inspect();
		List<EnhEntity> enhEntities = new ArrayList<>();
		for (DataEntity dataEntity : dataEntities) {
			EnhEntity enhMappedSuperclassEntity = null;
			LOG.info("enhance: dataEntity.mappedSuperclass=" + dataEntity.mappedSuperclass);
			if (dataEntity.mappedSuperclass != null) {
				LOG.info("enhance: dataEntity.mappedSuperclass.className=" + dataEntity.mappedSuperclass.className);
				enhMappedSuperclassEntity = enhance(dataEntity.mappedSuperclass, enhEntities);
			}

			EnhEntity enhEntity = enhance(dataEntity, enhEntities);
			enhEntity.setMappedSuperclass(enhMappedSuperclassEntity);
			enhEntities.add(enhEntity);
		}

		return enhEntities;
	}

	private EnhEntity enhance(DataEntity dataEntity, List<EnhEntity> parsedEntities) throws Exception {
		LOG.info("enhance: dataEntity.className=" + dataEntity.className);
		EnhEntity enhEntity = new EnhEntity();
		enhEntity.setClassName(dataEntity.className);
		List<EnhAttribute> enhAttributes = enhance(dataEntity.ct, dataEntity.dataAttributes, dataEntity);
		LOG.info("enhance: attributes created for " + dataEntity.className);
		enhEntity.setEnhAttributes(enhAttributes);
		List<EnhEntity> embeddables = findEmbeddables(enhAttributes, parsedEntities);
		enhEntity.addEmbeddables(embeddables);
		return enhEntity;
	}

	private List<EnhEntity> findEmbeddables(List<EnhAttribute> enhAttributes, List<EnhEntity> parsedEntities) {
		List<EnhEntity> embeddables = new ArrayList<>();
		for (EnhAttribute enhAttribute : enhAttributes) {
			if (!enhAttribute.isEmbedded())
				continue;

			EnhEntity embeddable = findInspectedEnhEmbeddables(enhAttribute.getClassName(), parsedEntities);
			if (embeddable == null) {
				embeddable = new EnhEntity();
				embeddable.setClassName(enhAttribute.getClassName());
				embeddable.setEnhAttributes(enhAttribute.getEmbeddedAttributes());
			}

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

	private boolean toEnhance(List<DataAttribute> dataAttributes) throws Exception {
		for (DataAttribute dataAttribute : dataAttributes) {
			if (toEnhance(dataAttribute))
				return true;
		}

		return false;
	}

	private List<EnhAttribute> enhance(CtClass ct, List<DataAttribute> dataAttributes, DataEntity dataEntity)
			throws Exception {
		LOG.info("enhance: ct.getName()=" + ct.getName());
		if (!enhancedDataEntities.contains(dataEntity)) {
			if (toEnhance(dataAttributes)) {
				LOG.info("Enhancing: " + ct.getName());
				addEntityDelegateField(ct);
			} else {
				LOG.info("Enhancing: " + ct.getName() + " not necessary");
			}
		}

		List<EnhAttribute> enhAttributes = new ArrayList<>();
		for (DataAttribute dataAttribute : dataAttributes) {
			Property property = dataAttribute.property;
			LOG.info("enhance: Enhancing attribute '" + property.ctField.getName() + "'");
			if (toEnhance(dataAttribute) && !enhancedDataEntities.contains(dataEntity)) {
				if (property.enhanceGet) {
					if (isLazyOrEntityType(property.getMethod.getReturnType()))
						modifyGetMethod(property.getMethod, property.ctField);
				}

				if (property.enhanceSet)
					modifySetMethod(property.setMethod, property.ctField);
			}

			List<EnhAttribute> enhEmbeddedAttributes = null;
			if (dataAttribute.embeddedAttributes != null)
				enhEmbeddedAttributes = enhance(dataAttribute.embeddedCtClass, dataAttribute.embeddedAttributes, null);

			EnhAttribute enhAttribute = new EnhAttribute(property.ctField.getName(),
					property.ctField.getType().getName(), property.ctField.getType().isPrimitive(),
					property.getMethod.getName(), property.setMethod.getName(), property.embedded,
					enhEmbeddedAttributes);
			enhAttributes.add(enhAttribute);
		}

		if (!enhancedDataEntities.contains(dataEntity)) {
			ct.toClass(getClass().getClassLoader(), getClass().getProtectionDomain());
		}

		if (dataEntity != null)
			enhancedDataEntities.add(dataEntity);

		return enhAttributes;
	}

	private boolean toEnhance(DataAttribute dataAttribute) {
		if (dataAttribute.property.id)
			return false;

		if (dataAttribute.parentIsEmbeddedId)
			return false;

		if (!dataAttribute.property.enhanceGet && !dataAttribute.property.enhanceSet)
			return false;

		return true;
	}

	private List<DataEntity> inspect() throws Exception {
		List<DataEntity> dataEntities = new ArrayList<>();
		for (String className : classNames) {
			inspect(className, dataEntities);
		}

		return dataEntities;
	}

	private void inspect(String className, List<DataEntity> inspectedClasses) throws Exception {
		// already enhanced
		for (DataEntity enhEntity : inspectedClasses) {
			if (enhEntity.className.equals(className))
				return;
		}

		ClassPool pool = ClassPool.getDefault();
		CtClass ct = pool.get(className);
		// mapped superclasses are enhanced finding the entity superclasses
		Object mappedSuperclassAnnotation = ct.getAnnotation(MappedSuperclass.class);
		if (mappedSuperclassAnnotation != null)
			return;

		// skip embeddable classes
		Object embeddableAnnotation = ct.getAnnotation(Embeddable.class);
		if (embeddableAnnotation != null)
			return;

		Object entityAnnotation = ct.getAnnotation(Entity.class);
		if (entityAnnotation == null) {
			LOG.error("@Entity annotation not found: " + ct.getName());
			return;
		}

		DataEntity dataEntity = new DataEntity();
		dataEntity.className = className;

		Optional<DataEntity> optional = findMappedSuperclass(ct, inspectedClasses);
		if (optional.isPresent())
			dataEntity.mappedSuperclass = optional.get();

		List<Property> properties = findAttributes(ct);
		LOG.info("Found " + properties.size() + " attributes in '" + ct.getName() + "'");
		List<DataAttribute> attrs = createDataAttributes(properties, false);
		dataEntity.dataAttributes = attrs;
		dataEntity.ct = ct;

		// looks for embeddables
		List<DataEntity> embeddables = new ArrayList<>();
		createEmbeddables(attrs, embeddables, inspectedClasses);
		dataEntity.embeddables.addAll(embeddables);

		if (!attrs.isEmpty() || dataEntity.mappedSuperclass != null) {
			inspectedClasses.add(dataEntity);
		}
	}

	private Optional<DataEntity> findMappedSuperclass(CtClass ct, List<DataEntity> inspectedClasses) throws Exception {
		CtClass superClass = ct.getSuperclass();
		if (superClass == null)
			return Optional.empty();

		if (superClass.getName().equals("java.lang.Object"))
			return Optional.empty();

		LOG.info("superClass.getName()=" + superClass.getName());
		Object mappedSuperclassAnnotation = superClass.getAnnotation(MappedSuperclass.class);
		if (mappedSuperclassAnnotation == null)
			return Optional.empty();

		// checks if the mapped superclass id already inspected
		DataEntity mappedSuperclassEnhEntity = findInspectedMappedSuperclass(inspectedClasses, superClass.getName());
		LOG.info("mappedSuperclassEnhEntity=" + mappedSuperclassEnhEntity);
		if (mappedSuperclassEnhEntity != null)
			return Optional.of(mappedSuperclassEnhEntity);

		List<Property> properties = findAttributes(superClass);
		LOG.info("Found " + properties.size() + " attributes in '" + superClass.getName() + "'");
		List<DataAttribute> attrs = createDataAttributes(properties, false);
		LOG.info("attrs.size()=" + attrs.size());
		if (attrs.isEmpty())
			return Optional.empty();

		DataEntity mappedSuperclass = new DataEntity();
		mappedSuperclass.className = superClass.getName();
		mappedSuperclass.dataAttributes = attrs;
		mappedSuperclass.ct = superClass;

		List<DataEntity> embeddables = new ArrayList<>();
		createEmbeddables(attrs, embeddables, inspectedClasses);
		mappedSuperclass.embeddables.addAll(embeddables);

		return Optional.of(mappedSuperclass);
	}

	private DataEntity findInspectedMappedSuperclass(List<DataEntity> enhancedClasses, String superclassName) {
		for (DataEntity enhEntity : enhancedClasses) {
			DataEntity mappedSuperclassEnhEntity = enhEntity.mappedSuperclass;
			if (mappedSuperclassEnhEntity != null && mappedSuperclassEnhEntity.className.equals(superclassName))
				return mappedSuperclassEnhEntity;
		}

		return null;
	}

	private void createEmbeddables(List<DataAttribute> dataAttributes, List<DataEntity> embeddables,
			List<DataEntity> inspectedClasses) {
		for (DataAttribute dataAttribute : dataAttributes) {
			if (!dataAttribute.property.embedded)
				continue;

			DataEntity dataEntity = findInspectedEmbeddable(inspectedClasses, dataAttribute.property.ctField.getName());
			if (dataEntity == null) {
				dataEntity = new DataEntity();
				dataEntity.className = dataAttribute.property.ctField.getName();
				dataEntity.dataAttributes = dataAttribute.embeddedAttributes;
				dataEntity.ct = dataAttribute.embeddedCtClass;
			}

			embeddables.add(dataEntity);
			createEmbeddables(dataAttribute.embeddedAttributes, embeddables, inspectedClasses);
		}
	}

	private DataEntity findInspectedEmbeddable(List<DataEntity> inspectedClasses, String className) {
		for (DataEntity dataEntity : inspectedClasses) {
			for (DataEntity embeddable : dataEntity.embeddables) {
				if (embeddable.className.equals(className))
					return embeddable;

				if (!embeddable.embeddables.isEmpty()) {
					DataEntity entity = findInspectedEmbeddable(embeddable.embeddables, className);
					if (entity != null)
						return entity;
				}
			}
		}

		return null;
	}

	private List<DataAttribute> createDataAttributes(List<Property> properties, boolean embeddedId) throws Exception {
		List<DataAttribute> attributes = new ArrayList<>();
		// nothing to do if there are no persistent attributes
		if (properties.isEmpty())
			return attributes;

		if (countAttributesToEnhance(properties) == 0)
			return attributes;

		for (Property property : properties) {
			DataAttribute dataAttribute = createAttributeFromProperty(property, embeddedId);
			attributes.add(dataAttribute);
		}

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

	private DataAttribute createAttributeFromProperty(Property property, boolean parentIsEmbeddedId) throws Exception {
		LOG.info("createAttributeFromProperty: property.ctField.getName()=" + property.ctField.getName()
				+ "; property.embedded=" + property.embedded);
		List<DataAttribute> embeddedAttributes = null;
		CtClass embeddedCtClass = null;
		if (property.embedded) {
			embeddedAttributes = createDataAttributes(property.embeddedProperties, property.id);
			embeddedCtClass = property.ctField.getType();
		}

		DataAttribute dataAttribute = new DataAttribute(property, embeddedAttributes, parentIsEmbeddedId,
				embeddedCtClass);
		return dataAttribute;
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
			Optional<Property> optional = readAttribute(ctField, ctClass);
			if (optional.isPresent())
				attrs.add(optional.get());
		}

		return attrs;
	}

	private Optional<Property> readAttribute(CtField ctField, CtClass ctClass) throws Exception {
		LOG.info("readAttribute: ctField.getName()=" + ctField.getName());
		LOG.info("readAttribute: ctField.getModifiers()=" + ctField.getModifiers());
		LOG.info("readAttribute: ctField.getType().getName()=" + ctField.getType().getName());
		LOG.info("readAttribute: ctField.getSignature()=" + ctField.getSignature());
		LOG.info("readAttribute: ctField.getFieldInfo()=" + ctField.getFieldInfo());
		LOG.info("readAttribute: ctField.getFieldInfo2()=" + ctField.getFieldInfo2());
		int modifier = ctField.getModifiers();
		if (!Modifier.isPrivate(modifier) && !Modifier.isProtected(modifier) && !Modifier.isPackage(modifier))
			return Optional.empty();

		Object transientAnnotation = ctField.getAnnotation(Transient.class);
		if (transientAnnotation != null)
			return Optional.empty();

		PropertyMethod getPropertyMethod = findGetMethod(ctClass, ctField);
		if (!getPropertyMethod.method.isPresent())
			return Optional.empty();

		PropertyMethod setPropertyMethod = findSetMethod(ctClass, ctField);
		if (!setPropertyMethod.method.isPresent())
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
		Property property = new Property(id, getPropertyMethod.method.get(), setPropertyMethod.method.get(), ctField,
				embedded, embeddedProperties, getPropertyMethod.enhance, setPropertyMethod.enhance);
		return Optional.of(property);
	}

	private CtMethod findIsGetMethod(CtClass ctClass, CtField ctField) throws NotFoundException {
		try {
			String methodName = "get" + BeanUtil.capitalize(ctField.getName());
			return ctClass.getDeclaredMethod(methodName);
		} catch (NotFoundException e) {
			if (ctField.getType().getName().equals("java.lang.Boolean")
					|| ctField.getType().getName().equals("boolean")) {
				return ctClass.getDeclaredMethod("is" + BeanUtil.capitalize(ctField.getName()));
			}
		}

		return null;
	}

	/**
	 * Returns true if the input type can be a lazy attribute.
	 * 
	 * @param ctClass
	 * @return true if the input type can be a lazy attribute
	 */
	private boolean isLazyOrEntityType(CtClass ctClass) {
		String name = ctClass.getName();
		if (isEntityName(name))
			return true;

		if (name.equals("java.util.Collection"))
			return true;

		if (name.equals("java.util.Map") || name.equals("java.util.HashMap"))
			return true;

		if (name.equals("java.util.List"))
			return true;

		if (name.equals("java.util.Set") || name.equals("java.util.HashSet"))
			return true;

		return false;
	}

	private boolean isEntityName(String name) {
		return dataEntities.stream().filter(d -> d.className.equals(name)).findFirst().isPresent();
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

	private class DataAttribute {
		private Property property;
		private List<DataAttribute> embeddedAttributes;
		private boolean parentIsEmbeddedId = false;
		// only for embedded attributes
		private CtClass embeddedCtClass;

		public DataAttribute(Property property, List<DataAttribute> embeddedProperties, boolean parentIsEmbeddedId,
				CtClass embeddedCtClass) {
			super();
			this.property = property;
			this.embeddedAttributes = embeddedProperties;
			this.parentIsEmbeddedId = parentIsEmbeddedId;
			this.embeddedCtClass = embeddedCtClass;
		}
	}

	private class DataEntity {
		private String className;
		private CtClass ct;
		private List<DataAttribute> dataAttributes = new ArrayList<>();
		private DataEntity mappedSuperclass;
		private List<DataEntity> embeddables = new ArrayList<>();
	}
}
