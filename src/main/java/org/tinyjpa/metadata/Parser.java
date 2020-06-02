package org.tinyjpa.metadata;

import java.beans.IntrospectionException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.Attribute;
import org.tinyjpa.jdbc.JdbcTypes;

public class Parser {
	private Logger LOG = LoggerFactory.getLogger(Parser.class);

//	public Entity parse(String className) throws ClassNotFoundException, IntrospectionException {
//		Class<?> c = Class.forName(className);
//		javax.persistence.Entity ec = c.getAnnotation(javax.persistence.Entity.class);
//		if (ec == null) {
//			LOG.warn("@Entity annotation not found");
//			return null;
//		}
//
//		List<Attribute> attributes = readAttributes(c);
//		Attribute id = null;
//		if (attributes.size() > 0 && attributes.get(0).isId())
//			id = attributes.get(0);
//
//		List<Attribute> subAttributes = null;
//		if (attributes.size() > 0)
//			subAttributes = attributes.subList(1, attributes.size());
//
//		String tableName = c.getSimpleName();
//		Table table = c.getAnnotation(Table.class);
//		if (table != null && table.name() != null && table.name().trim().length() > 0)
//			tableName = table.name();
//
//		return new Entity(c, tableName, id, Collections.unmodifiableList(subAttributes));
//	}

	public Entity parse(EnhEntity enhEntity) throws ClassNotFoundException, IntrospectionException {
		Class<?> c = Class.forName(enhEntity.getClassName());
		javax.persistence.Entity ec = c.getAnnotation(javax.persistence.Entity.class);
		if (ec == null) {
			LOG.warn("@Entity annotation not found");
			return null;
		}

		LOG.info("Reading attributes...");
		List<Attribute> attributes = readAttributes(enhEntity);
		if (enhEntity.getMappedSuperclass() != null) {
			List<Attribute> msAttributes = readAttributes(enhEntity.getMappedSuperclass());
			attributes.addAll(msAttributes);
		}

		LOG.info("Reading Id...");
		Optional<Attribute> optional = attributes.stream().filter(a -> a.isId()).findFirst();
		if (!optional.isPresent()) {
			LOG.warn("@Id annotation not found");
			return null;
		}

		Attribute id = optional.get();
		attributes.remove(id);

		String tableName = c.getSimpleName();
		Table table = c.getAnnotation(Table.class);
		if (table != null && table.name() != null && table.name().trim().length() > 0)
			tableName = table.name();

		return new Entity(c, tableName, id, Collections.unmodifiableList(attributes));
	}

	private List<Attribute> readAttributes(EnhEntity enhEntity) throws IntrospectionException {
		List<Attribute> attributes = new ArrayList<>();
		for (EnhAttribute enhAttribute : enhEntity.getEnhAttributes())
			try {
				String columnName = enhAttribute.getAttributeName();
				Class<?> c = Class.forName(enhEntity.getClassName());
				LOG.info("readAttributes: columnName=" + columnName);
				LOG.info("readAttributes: c.getClassLoader()=" + c.getClassLoader());
				Field field = c.getDeclaredField(enhAttribute.getAttributeName());

				Class<?> attributeClass = Class.forName(enhAttribute.getAttributeClassName());
				Method readMethod = c.getMethod(enhAttribute.getGetMethod());
				Method writeMethod = c.getMethod(enhAttribute.getSetMethod(), attributeClass);
				Column column = field.getAnnotation(Column.class);
				if (column == null) {
					column = readMethod.getAnnotation(Column.class);
				}

				if (column != null) {
					String cn = column.name();
					if (cn != null && cn.trim().length() > 0)
						columnName = cn;
				}

				Id id = field.getAnnotation(Id.class);
				if (id == null) {
					Attribute attribute = new Attribute(enhAttribute.getAttributeName(), columnName, attributeClass,
							readMethod, writeMethod, id != null, JdbcTypes.sqlTypeFromClass(attributeClass), null);
					attributes.add(attribute);
				} else {
					GeneratedValue generatedValue = field.getAnnotation(GeneratedValue.class);
					org.tinyjpa.metadata.GeneratedValue gv = null;
					if (generatedValue != null)
						gv = new org.tinyjpa.metadata.GeneratedValue(generatedValue.strategy(),
								generatedValue.generator());

					Attribute attribute = new Attribute(enhAttribute.getAttributeName(), columnName, attributeClass,
							readMethod, writeMethod, id != null, JdbcTypes.sqlTypeFromClass(attributeClass), gv);
					attributes.add(0, attribute);
				}
			} catch (Exception e) {
				LOG.error(e.getClass().getName());
				LOG.error(e.getMessage());
			}

		return attributes;
	}

//	private List<Attribute> readAttributes(Class<?> c) throws IntrospectionException {
//		BeanInfo beanInfo = Introspector.getBeanInfo(c);
//		List<Attribute> attributes = new ArrayList<>();
//		for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
//			if (pd.getPropertyType() == Class.class)
//				continue;
//
//			if (pd.getReadMethod() == null || pd.getWriteMethod() == null)
//				continue;
//
//			try {
//				String columnName = pd.getName();
//				LOG.info("readAttributes: columnName=" + columnName);
//				LOG.info("readAttributes: c.getClassLoader()=" + c.getClassLoader());
//				Field field = c.getDeclaredField(pd.getName());
//
//				// checks if transient
//				Transient transientAnnotation = field.getAnnotation(Transient.class);
//				if (transientAnnotation == null) {
//					Method readMethod = pd.getReadMethod();
//					transientAnnotation = readMethod.getAnnotation(Transient.class);
//				}
//
//				if (transientAnnotation != null)
//					continue;
//
//				Column column = field.getAnnotation(Column.class);
//				if (column == null) {
//					Method readMethod = pd.getReadMethod();
//					column = readMethod.getAnnotation(Column.class);
//				}
//
//				if (column != null) {
//					String cn = column.name();
//					if (cn != null && cn.trim().length() > 0)
//						columnName = cn;
//				}
//
//				Id id = field.getAnnotation(Id.class);
//				if (id == null) {
//					Attribute attribute = new Attribute(pd.getName(), columnName, pd.getPropertyType(),
//							pd.getReadMethod(), pd.getWriteMethod(), id != null,
//							JdbcTypes.sqlTypeFromClass(pd.getPropertyType()), null);
//					attributes.add(attribute);
//				} else {
//					GeneratedValue generatedValue = field.getAnnotation(GeneratedValue.class);
//					org.tinyjpa.metadata.GeneratedValue gv = null;
//					if (generatedValue != null)
//						gv = new org.tinyjpa.metadata.GeneratedValue(generatedValue.strategy(),
//								generatedValue.generator());
//
//					Attribute attribute = new Attribute(pd.getName(), columnName, pd.getPropertyType(),
//							pd.getReadMethod(), pd.getWriteMethod(), id != null,
//							JdbcTypes.sqlTypeFromClass(pd.getPropertyType()), gv);
//					attributes.add(0, attribute);
//				}
//			} catch (Exception e) {
//				LOG.error(e.getClass().getName());
//				LOG.error(e.getMessage());
//			}
//		}
//
//		return attributes;
//	}

}
