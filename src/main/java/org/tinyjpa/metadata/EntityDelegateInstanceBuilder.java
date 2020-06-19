package org.tinyjpa.metadata;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.Attribute;
import org.tinyjpa.jdbc.Entity;

public class EntityDelegateInstanceBuilder implements EntityInstanceBuilder {
	private Logger LOG = LoggerFactory.getLogger(EntityDelegateInstanceBuilder.class);

	@Override
	public Object build(Entity entity, List<Attribute> attributes, List<Object> values, Object idValue)
			throws Exception {
		Object entityInstance = entity.getClazz().newInstance();
		int i = 0;
		for (Attribute attribute : attributes) {
			LOG.info("build: attribute.getName()=" + attribute.getName());
			findAndSetAttributeValue(entity.getClazz(), entityInstance, entity.getAttributes(), attribute,
					values.get(i));
			++i;
		}

		entity.getId().getWriteMethod().invoke(entityInstance, idValue);

		return entityInstance;
	}

	@Override
	public Object setAttributeValue(Object parentInstance, Class<?> parentClass, Attribute attribute, Object value)
			throws Exception {
		Object parent = parentInstance;
		if (parent == null)
			parent = parentClass.newInstance();

		LOG.info("setAttributeValue: parent=" + parent + "; a.getWriteMethod()=" + attribute.getWriteMethod());
		try {
			EntityDelegate.getInstance().addIgnoreEntityInstance(parent);
			attribute.getWriteMethod().invoke(parent, value);
		} finally {
			EntityDelegate.getInstance().removeIgnoreEntityInstance(parent);
		}

		return parent;
	}

	@Override
	public Object getAttributeValue(Object parentInstance, Attribute attribute) throws Exception {
		LOG.info("getAttributeValue: parent=" + parentInstance + "; a.getReadMethod()=" + attribute.getReadMethod());
		try {
//			EntityDelegate.getInstance().addIgnoreEntityInstance(parent);
			return attribute.getReadMethod().invoke(parentInstance);
		} finally {
//			EntityDelegate.getInstance().removeIgnoreEntityInstance(parent);
		}

//		return parent;
	}

	private Object findAndSetAttributeValue(Class<?> parentClass, Object parentInstance, List<Attribute> attributes,
			Attribute attribute, Object value) throws Exception {
		LOG.info("findAndSetAttributeValue: value=" + value + "; value.getClass().getName()="
				+ value.getClass().getName());
		for (Attribute a : attributes) {
			if (a == attribute) {
				return setAttributeValue(parentInstance, parentClass, attribute, value);
			}
		}

		// search over embedded attributes
		for (Attribute a : attributes) {
			if (!a.isEmbedded())
				continue;

			Object aInstance = findAndSetAttributeValue(a.getType(), null, a.getEmbeddedAttributes(), attribute, value);
			if (aInstance != null) {
				return setAttributeValue(parentInstance, parentClass, a, aInstance);
			}
		}

		return null;
	}

}
