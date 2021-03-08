package org.minijpa.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.minijpa.jdbc.AttributeValue;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.db.EntityInstanceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityDelegateInstanceBuilder implements EntityInstanceBuilder {

    private Logger LOG = LoggerFactory.getLogger(EntityDelegateInstanceBuilder.class);

    @Override
    public Object build(MetaEntity entity, Object idValue)
	    throws Exception {
	Object entityInstance = entity.getEntityClass().getDeclaredConstructor().newInstance();
	entity.getId().getWriteMethod().invoke(entityInstance, idValue);
	return entityInstance;
    }

    @Override
    public void setAttributeValues(MetaEntity entity, Object entityInstance, List<MetaAttribute> attributes,
	    List<Object> values) throws Exception {
	for (int i = 0; i < attributes.size(); ++i) {
	    MetaAttribute attribute = attributes.get(i);
	    LOG.info("setAttributeValues: 1 attribute.getName()=" + attribute.getName() + "; values.get(i)=" + values.get(i));
	}

	for (int i = 0; i < attributes.size(); ++i) {
	    MetaAttribute attribute = attributes.get(i);
	    LOG.info("setAttributeValues: attribute.getName()=" + attribute.getName() + "; values.get(i)=" + values.get(i));
	    findAndSetAttributeValue(entity.getEntityClass(), entityInstance, entity.getAttributes(), attribute,
		    values.get(i));
	}
    }

    @Override
    public Object setAttributeValue(Object parentInstance, Class<?> parentClass, MetaAttribute attribute, Object value)
	    throws Exception {
	Object parent = parentInstance;
	if (parent == null) {
	    parent = parentClass.getDeclaredConstructor().newInstance();
	}

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
	LOG.info("findAndSetAttributeValue: value=" + value + "; attribute=" + attribute);

	for (MetaAttribute a : attributes) {
	    if (a == attribute) {
		return setAttributeValue(parentInstance, parentClass, attribute, value);
	    }
	}

	// search over embedded attributes
	for (MetaAttribute a : attributes) {
	    if (!a.isEmbedded())
		continue;

	    Object aInstance = findAndSetAttributeValue(a.getType(), null, a.getChildren(), attribute, value);
	    if (aInstance != null) {
		return setAttributeValue(parentInstance, parentClass, a, aInstance);
	    }
	}

	return null;
    }

    private List<AttributeValue> unpackEmbedded(MetaAttribute metaAttribute, Object value) {
	List<AttributeValue> attrValues = new ArrayList<>();
	Optional<Map<String, Object>> optional = EntityDelegate.getInstance().getChanges(value);
	if (optional.isEmpty())
	    return attrValues;

	for (Map.Entry<String, Object> e : optional.get().entrySet()) {
	    MetaAttribute a = metaAttribute.findChildByName(e.getKey());
	    attrValues.add(new AttributeValue(a, e.getValue()));
	    if (a.isEmbedded())
		attrValues.addAll(unpackEmbedded(a, e.getValue()));
	}

	return attrValues;
    }

    @Override
    public Optional<List<AttributeValue>> getChanges(MetaEntity entity, Object entityInstance) {
	Optional<Map<String, Object>> optional = EntityDelegate.getInstance().getChanges(entityInstance);
	if (optional.isEmpty())
	    return Optional.empty();

	List<AttributeValue> attributeValues = new ArrayList<>();
	for (Map.Entry<String, Object> e : optional.get().entrySet()) {
	    MetaAttribute metaAttribute = entity.getAttribute(e.getKey());
	    if (metaAttribute.isEmbedded()) {
		attributeValues.addAll(unpackEmbedded(metaAttribute, e.getValue()));
	    } else
		attributeValues.add(new AttributeValue(metaAttribute, e.getValue()));
	}

	return Optional.of(attributeValues);
    }

    @Override
    public void removeChanges(Object entityInstance) {
	EntityDelegate.getInstance().removeChanges(entityInstance);
    }

}
