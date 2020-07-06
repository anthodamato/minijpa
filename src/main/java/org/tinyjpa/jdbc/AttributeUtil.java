package org.tinyjpa.jdbc;

import java.util.List;

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

	public static Object getIdValue(Entity entity, Object entityInstance) throws Exception {
		Attribute id = entity.getId();
		return id.getReadMethod().invoke(entityInstance);
	}

}
