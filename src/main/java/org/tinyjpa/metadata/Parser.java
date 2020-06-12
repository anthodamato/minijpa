package org.tinyjpa.metadata;

import java.beans.IntrospectionException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.Attribute;
import org.tinyjpa.jdbc.Entity;
import org.tinyjpa.jdbc.JdbcTypes;

public class Parser {
	private Logger LOG = LoggerFactory.getLogger(Parser.class);

	public Entity parse(EnhEntity enhEntity) throws ClassNotFoundException, IntrospectionException,
			NoSuchFieldException, SecurityException, NoSuchMethodException {
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

	private List<Attribute> readAttributes(EnhEntity enhEntity) throws IntrospectionException, ClassNotFoundException,
			NoSuchFieldException, SecurityException, NoSuchMethodException {
		List<Attribute> attributes = new ArrayList<>();
		for (EnhAttribute enhAttribute : enhEntity.getEnhAttributes()) {
			Attribute attribute = readAttribute(enhEntity.getClassName(), enhAttribute);
			attributes.add(attribute);
		}

		return attributes;
	}

	private List<Attribute> readAttributes(List<EnhAttribute> enhAttributes, String parentClassName)
			throws IntrospectionException, ClassNotFoundException, NoSuchFieldException, SecurityException,
			NoSuchMethodException {
		List<Attribute> attributes = new ArrayList<>();
		for (EnhAttribute enhAttribute : enhAttributes) {
			Attribute attribute = readAttribute(parentClassName, enhAttribute);
			attributes.add(attribute);
		}

		return attributes;
	}

	private Attribute readAttribute(String parentClassName, EnhAttribute enhAttribute) throws IntrospectionException,
			ClassNotFoundException, NoSuchFieldException, SecurityException, NoSuchMethodException {
		String columnName = enhAttribute.getName();
		Class<?> c = Class.forName(parentClassName);
		LOG.info("readAttribute: columnName=" + columnName);
//		LOG.info("readAttributes: c.getClassLoader()=" + c.getClassLoader());
		Field field = c.getDeclaredField(enhAttribute.getName());
		Class<?> attributeClass = null;
		if (enhAttribute.isPrimitiveType())
			attributeClass = JavaTypes.getClass(enhAttribute.getClassName());
		else
			attributeClass = Class.forName(enhAttribute.getClassName());

		Method readMethod = c.getMethod(enhAttribute.getGetMethod());
		Method writeMethod = c.getMethod(enhAttribute.getSetMethod(), attributeClass);
		Column column = field.getAnnotation(Column.class);
//			if (column == null) {
//				column = readMethod.getAnnotation(Column.class);
//			}

		if (column != null) {
			String cn = column.name();
			if (cn != null && cn.trim().length() > 0)
				columnName = cn;
		}

		Id idAnnotation = field.getAnnotation(Id.class);
		Attribute attribute = null;
		if (idAnnotation == null) {
			List<Attribute> embeddedAttributes = null;
			boolean embedded = enhAttribute.isEmbedded();
			if (embedded) {
				embeddedAttributes = readAttributes(enhAttribute.getEmbeddedAttributes(), enhAttribute.getClassName());
				LOG.info("readAttribute: embeddedAttributes.size()=" + embeddedAttributes.size());
				if (embeddedAttributes.isEmpty()) {
					embedded = false;
					embeddedAttributes = null;
				}
			}

			boolean id = field.getAnnotation(EmbeddedId.class) != null;
//			LOG.info("readAttribute: enhAttribute.getName()=" + enhAttribute.getName());
//			LOG.info("readAttribute: embedded=" + embedded);
			attribute = new Attribute(enhAttribute.getName(), columnName, attributeClass, readMethod, writeMethod, id,
					JdbcTypes.sqlTypeFromClass(attributeClass), null, embedded, embeddedAttributes);
			LOG.info("readAttribute: attribute: " + attribute);
		} else {
			GeneratedValue generatedValue = field.getAnnotation(GeneratedValue.class);
			org.tinyjpa.metadata.GeneratedValue gv = null;
			if (generatedValue != null)
				gv = new org.tinyjpa.metadata.GeneratedValue(generatedValue.strategy(), generatedValue.generator());

			attribute = new Attribute(enhAttribute.getName(), columnName, attributeClass, readMethod, writeMethod,
					idAnnotation != null, JdbcTypes.sqlTypeFromClass(attributeClass), gv, false, null);
		}

		return attribute;
	}
}
