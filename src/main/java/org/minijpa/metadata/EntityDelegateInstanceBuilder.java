package org.minijpa.metadata;

import java.util.List;
import java.util.Optional;

import org.minijpa.jdbc.AttributeValue;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.db.EntityInstanceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityDelegateInstanceBuilder implements EntityInstanceBuilder {
	private Logger LOG = LoggerFactory.getLogger(EntityDelegateInstanceBuilder.class);
	private boolean log = false;

	@Override
	public Object build(MetaEntity entity, List<MetaAttribute> attributes, List<Object> values, Object idValue)
			throws Exception {
		Object entityInstance = entity.getEntityClass().newInstance();
		entity.getId().getWriteMethod().invoke(entityInstance, idValue);
		setAttributeValues(entity, entityInstance, attributes, values);
		return entityInstance;
	}

	@Override
	public void setAttributeValues(MetaEntity entity, Object entityInstance, List<MetaAttribute> attributes,
			List<Object> values) throws Exception {
		for (int i = 0; i < attributes.size(); ++i) {
			MetaAttribute attribute = attributes.get(i);
			if (log) {
				LOG.info("setAttributeValues: attribute.getName()=" + attribute.getName());
				LOG.info("setAttributeValues: values.get(i)=" + values.get(i));
			}

			findAndSetAttributeValue(entity.getEntityClass(), entityInstance, entity.getAttributes(), attribute,
					values.get(i));
		}
	}

	@Override
	public Object setAttributeValue(Object parentInstance, Class<?> parentClass, MetaAttribute attribute, Object value)
			throws Exception {
		Object parent = parentInstance;
		if (parent == null)
			parent = parentClass.newInstance();

		if (log)
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
	public Object getAttributeValue(Object parentInstance, MetaAttribute attribute) throws Exception {
		if (log)
			LOG.info(
					"getAttributeValue: parent=" + parentInstance + "; a.getReadMethod()=" + attribute.getReadMethod());

		try {
//			EntityDelegate.getInstance().addIgnoreEntityInstance(parent);
			return attribute.getReadMethod().invoke(parentInstance);
		} finally {
//			EntityDelegate.getInstance().removeIgnoreEntityInstance(parent);
		}

//		return parent;
	}

	private Object findAndSetAttributeValue(Class<?> parentClass, Object parentInstance, List<MetaAttribute> attributes,
			MetaAttribute attribute, Object value) throws Exception {
		if (log)
			LOG.info("findAndSetAttributeValue: value=" + value + "; value.getClass().getName()="
					+ value.getClass().getName());

		for (MetaAttribute a : attributes) {
			if (a == attribute) {
				return setAttributeValue(parentInstance, parentClass, attribute, value);
			}
		}

		// search over embedded attributes
		for (MetaAttribute a : attributes) {
			if (!a.isEmbedded())
				continue;

			Object aInstance = findAndSetAttributeValue(a.getType(), null, a.getEmbeddedAttributes(), attribute, value);
			if (aInstance != null) {
				return setAttributeValue(parentInstance, parentClass, a, aInstance);
			}
		}

		return null;
	}

	@Override
	public Optional<List<AttributeValue>> getChanges(MetaEntity entity, Object entityInstance) {
		return EntityDelegate.getInstance().getChanges(entity, entityInstance);
	}

	@Override
	public void removeChanges(Object entityInstance) {
		EntityDelegate.getInstance().removeChanges(entityInstance);
	}

}
