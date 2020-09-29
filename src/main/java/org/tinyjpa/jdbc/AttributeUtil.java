package org.tinyjpa.jdbc;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.JdbcRunner.AttributeValues;

public class AttributeUtil {
	private static final Logger LOG = LoggerFactory.getLogger(AttributeUtil.class);

	public static Object createPK(MetaEntity entity, AttributeValues attributeValues) throws Exception {
		MetaAttribute id = entity.getId();
		if (id.isEmbedded()) {
			Object pkObject = id.getType().newInstance();
			createPK(entity, attributeValues, id.getEmbeddedAttributes(), id, pkObject);
			return pkObject;
		}

		int index = indexOf(attributeValues.attributes, id.getName());
		return attributeValues.values.get(index);
	}

	public static void createPK(MetaEntity entity, AttributeValues attributeValues, List<MetaAttribute> attributes,
			MetaAttribute id, Object pkObject) throws Exception {
		for (MetaAttribute a : attributes) {
			if (a.isEmbedded())
				createPK(entity, attributeValues, a.getEmbeddedAttributes(), id, pkObject);
			else {
				int index = indexOf(attributeValues.attributes, a.getName());
				Object value = attributeValues.values.get(index);
				a.getWriteMethod().invoke(pkObject, value);
			}
		}
	}

	public static int indexOf(List<MetaAttribute> attributes, String name) {
		for (int i = 0; i < attributes.size(); ++i) {
			MetaAttribute a = attributes.get(i);
			LOG.info("indexOf: a.getName()=" + a.getName());
			if (a.getName().equals(name))
				return i;
		}

		return -1;
	}

	public static int indexOfJoinColumnAttribute(List<JoinColumnAttribute> joinColumnAttributes, MetaAttribute a) {
		for (int i = 0; i < joinColumnAttributes.size(); ++i) {
			if (joinColumnAttributes.get(i).getForeignKeyAttribute() == a)
				return i;
		}

		return -1;
	}

	public static Object getIdValue(MetaEntity entity, Object entityInstance) throws Exception {
		MetaAttribute id = entity.getId();
		return id.getReadMethod().invoke(entityInstance);
	}

	public static Object getIdValue(MetaAttribute id, Object entityInstance) throws Exception {
		return id.getReadMethod().invoke(entityInstance);
	}

}
