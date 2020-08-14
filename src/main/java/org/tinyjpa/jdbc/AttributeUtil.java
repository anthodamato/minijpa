package org.tinyjpa.jdbc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.JdbcRunner.AttributeValues;

public class AttributeUtil {
	private static final Logger LOG = LoggerFactory.getLogger(AttributeUtil.class);

	public static Object createPK(Entity entity, AttributeValues attributeValues) throws Exception {
		Attribute id = entity.getId();
		if (id.isEmbedded()) {
			Object pkObject = id.getType().newInstance();
			createPK(entity, attributeValues, id.getEmbeddedAttributes(), id, pkObject);
			return pkObject;
		}

		int index = indexOf(attributeValues.attributes, id.getName());
		return attributeValues.values.get(index);
	}

	public static void createPK(Entity entity, AttributeValues attributeValues, List<Attribute> attributes,
			Attribute id, Object pkObject) throws Exception {
		for (Attribute a : attributes) {
			if (a.isEmbedded())
				createPK(entity, attributeValues, a.getEmbeddedAttributes(), id, pkObject);
			else {
				int index = indexOf(attributeValues.attributes, a.getName());
				Object value = attributeValues.values.get(index);
				a.getWriteMethod().invoke(pkObject, value);
			}
		}
	}

	public static int indexOf(List<Attribute> attributes, String name) {
		for (int i = 0; i < attributes.size(); ++i) {
			Attribute a = attributes.get(i);
			LOG.info("indexOf: a.getName()=" + a.getName());
			if (a.getName().equals(name))
				return i;
		}

		return -1;
	}

	public static int indexOfJoinColumnAttribute(List<JoinColumnAttribute> joinColumnAttributes, Attribute a) {
		for (int i = 0; i < joinColumnAttributes.size(); ++i) {
			if (joinColumnAttributes.get(i).getForeignKeyAttribute() == a)
				return i;
		}

		return -1;
	}

	public static Object getIdValue(Entity entity, Object entityInstance) throws Exception {
		Attribute id = entity.getId();
		return id.getReadMethod().invoke(entityInstance);
	}

	public static Object getIdValue(Attribute id, Object entityInstance) throws Exception {
		return id.getReadMethod().invoke(entityInstance);
	}

	/**
	 * Given an attribute class declared as interface, this method returns an
	 * implementation class to use in the lazy attributes.
	 * 
	 * @param attributeClass
	 * @return
	 */
	public static Class<?> findImplementationClass(Class<?> attributeClass) {
		LOG.info("findImplementationClass: attributeClass.getName()=" + attributeClass.getName());
		if (attributeClass == Collection.class || attributeClass == Set.class)
			return HashSet.class;

		if (attributeClass == List.class)
			return ArrayList.class;

		if (attributeClass == Map.class)
			return HashMap.class;

		return null;
	}

	private static boolean implementsInterface(Class<?> attributeClass, Class<?> interfaceClass) {
		if (attributeClass == interfaceClass)
			return true;

		Class<?>[] ics = attributeClass.getInterfaces();
		for (Class<?> ic : ics) {
			LOG.info("implementsInterface: ic=" + ic);
		}

		for (Class<?> ic : ics) {
			if (ic == interfaceClass)
				return true;
		}

		Class<?> sc = attributeClass.getSuperclass();
		if (sc == null)
			return false;

		return implementsInterface(sc, interfaceClass);
	}

	public static boolean isCollectionClass(Class<?> attributeClass) {
		LOG.info("isCollectionClass: attributeClass=" + attributeClass);
		if (implementsInterface(attributeClass, Collection.class))
			return true;

		if (implementsInterface(attributeClass, Map.class))
			return true;

		return false;
	}

	public static boolean isCollectionEmpty(Object instance) {
		Class<?> c = instance.getClass();
		if (implementsInterface(c, Collection.class)) {
			return ((Collection<?>) instance).isEmpty();
		} else if (implementsInterface(c, Map.class)) {
			return ((Map<?, ?>) instance).isEmpty();
		}

		return true;
	}

	public static List<Object> getCollectionAsList(Object instance) {
		List<Object> list = new ArrayList<>();
		Class<?> c = instance.getClass();
		if (implementsInterface(c, Collection.class)) {
			list.addAll((Collection<?>) instance);
		} else if (implementsInterface(c, Map.class)) {
			Map<?, ?> map = (Map<?, ?>) instance;
			list.addAll(map.values());
		}

		return list;
	}
}
