package org.minijpa.metadata;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.minijpa.jdbc.AttributeValueArray;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.db.EntityInstanceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityDelegateInstanceBuilder implements EntityInstanceBuilder {

    private final Logger LOG = LoggerFactory.getLogger(EntityDelegateInstanceBuilder.class);

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
//	for (int i = 0; i < attributes.size(); ++i) {
//	    MetaAttribute attribute = attributes.get(i);
//	    LOG.debug("setAttributeValues: 1 attribute.getName()=" + attribute.getName() + "; values.get(i)=" + values.get(i));
//	}

	for (int i = 0; i < attributes.size(); ++i) {
	    MetaAttribute attribute = attributes.get(i);
	    LOG.debug("setAttributeValues: attribute.getName()=" + attribute.getName() + "; values.get(i)=" + values.get(i));
	    findAndSetAttributeValue(entity.getEntityClass(), entityInstance, entity.getAttributes(), attribute,
		    values.get(i), entity);
	}
    }

    @Override
    public Object setAttributeValue(Object parentInstance, Class<?> parentClass, MetaAttribute attribute,
	    Object value, MetaEntity entity) throws Exception {
	Object parent = parentInstance;
	if (parent == null) {
	    parent = parentClass.getDeclaredConstructor().newInstance();
	}

	LOG.debug("setAttributeValue: parent=" + parent + "; a.getWriteMethod()=" + attribute.getWriteMethod());
	LOG.debug("setAttributeValue: value=" + value);

	attribute.getWriteMethod().invoke(parent, value);
	Method m = entity.getModificationAttributeReadMethod();
	List list = (List) m.invoke(parent);
	list.remove(attribute.getName());

	return parent;
    }

    @Override
    public Object getAttributeValue(Object parentInstance, MetaAttribute attribute) throws Exception {
	LOG.debug(
		"getAttributeValue: parent=" + parentInstance + "; a.getReadMethod()=" + attribute.getReadMethod());

	return attribute.getReadMethod().invoke(parentInstance);
    }

    private Object findAndSetAttributeValue(Class<?> parentClass, Object parentInstance,
	    List<MetaAttribute> attributes, MetaAttribute attribute, Object value, MetaEntity entity)
	    throws Exception {
	LOG.debug("findAndSetAttributeValue: value=" + value + "; attribute=" + attribute);
	LOG.debug("findAndSetAttributeValue: parentInstance=" + parentInstance + "; parentClass=" + parentClass);
	LOG.debug("findAndSetAttributeValue: entity=" + entity);

	for (MetaAttribute a : attributes) {
	    LOG.debug("findAndSetAttributeValue: a=" + a);
	    if (a == attribute) {
		return setAttributeValue(parentInstance, parentClass, attribute, value, entity);
	    }
	}

	// search over embedded attributes
	for (MetaAttribute a : attributes) {
	    if (!a.isEmbedded())
		continue;

	    LOG.debug("findAndSetAttributeValue: embedded a=" + a);
	    Object aInstance = findAndSetAttributeValue(a.getType(), null,
		    a.getEmbeddableMetaEntity().getAttributes(), attribute, value, a.getEmbeddableMetaEntity());
	    if (aInstance != null) {
		return setAttributeValue(parentInstance, parentClass, a, aInstance, entity);
	    }
	}

	return null;
    }

    @Override
    public void removeChanges(MetaEntity entity, Object entityInstance) throws IllegalAccessException, InvocationTargetException {
	Method m = entity.getModificationAttributeReadMethod();
	List list = (List) m.invoke(entityInstance);
	list.clear();
    }

    private void unpackEmbedded(MetaAttribute metaAttribute, Object value,
	    AttributeValueArray<MetaAttribute> attributeValueArray) throws IllegalAccessException, InvocationTargetException {
	Optional<Map<String, Object>> optional = getEntityModifications(metaAttribute.getEmbeddableMetaEntity(), value);
	if (optional.isEmpty())
	    return;

	for (Map.Entry<String, Object> e : optional.get().entrySet()) {
	    MetaAttribute a = metaAttribute.getEmbeddableMetaEntity().getAttribute(e.getKey());
	    attributeValueArray.add(a, e.getValue());
	    if (a.isEmbedded())
		unpackEmbedded(a, e.getValue(), attributeValueArray);
	}
    }

    @Override
    public AttributeValueArray<MetaAttribute> getModifications(MetaEntity entity, Object entityInstance) throws IllegalAccessException, InvocationTargetException {
	AttributeValueArray<MetaAttribute> attributeValueArray = new AttributeValueArray();
	Optional<Map<String, Object>> optional = getEntityModifications(entity, entityInstance);
	if (optional.isEmpty())
	    return attributeValueArray;

	for (Map.Entry<String, Object> e : optional.get().entrySet()) {
	    MetaAttribute metaAttribute = entity.getAttribute(e.getKey());
	    if (metaAttribute.isEmbedded()) {
		unpackEmbedded(metaAttribute, e.getValue(), attributeValueArray);
	    } else
		attributeValueArray.add(metaAttribute, e.getValue());
	}

	return attributeValueArray;
    }

    private Optional<Map<String, Object>> getEntityModifications(MetaEntity entity, Object entityInstance)
	    throws IllegalAccessException, InvocationTargetException {
	Method m = entity.getModificationAttributeReadMethod();
	List list = (List) m.invoke(entityInstance);
	if (list.isEmpty())
	    return Optional.empty();

	Map<String, Object> map = new HashMap<>();
	for (Object p : list) {
	    String v = (String) p;
	    MetaAttribute attribute = entity.getAttribute(v);
	    Object value = attribute.getReadMethod().invoke(entityInstance);
	    map.put(v, value);
	}

	return Optional.of(map);
    }
}
