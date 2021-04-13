package org.minijpa.jdbc.db;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.minijpa.jdbc.ModelValueArray;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityInstanceBuilderImpl implements EntityInstanceBuilder {

    private final Logger LOG = LoggerFactory.getLogger(EntityInstanceBuilderImpl.class);

    @Override
    public Object build(MetaEntity entity, Object idValue) throws Exception {
	Object entityInstance = entity.getEntityClass().getDeclaredConstructor().newInstance();
	entity.getId().getWriteMethod().invoke(entityInstance, idValue);
	return entityInstance;
    }

    @Override
    public void writeAttributeValue(MetaEntity entity, Object parentInstance, MetaAttribute attribute,
	    Object value) throws Exception {
	LOG.debug("writeAttributeValue: parentInstance=" + parentInstance);
	LOG.debug("writeAttributeValue: attribute.getName()=" + attribute.getName() + "; value=" + value);
	findAndSetAttributeValue(entity.getEntityClass(), parentInstance, entity.getAttributes(), attribute,
		value, entity);
    }

    @Override
    public Object writeMetaAttributeValue(Object parentInstance, Class<?> parentClass, MetaAttribute attribute,
	    Object value, MetaEntity entity) throws Exception {
	Object parent = parentInstance;
	if (parent == null) {
	    parent = parentClass.getDeclaredConstructor().newInstance();
	}

	LOG.debug("writeAttributeValue: parent=" + parent + "; a.getWriteMethod()=" + attribute.getWriteMethod());
	LOG.debug("writeAttributeValue: value=" + value);

	attribute.getWriteMethod().invoke(parent, value);
	Method m = entity.getModificationAttributeReadMethod();
	List list = (List) m.invoke(parent);
	list.remove(attribute.getName());
	LOG.debug("writeAttributeValue: list=" + list);
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

	// search over all attributes
	for (MetaAttribute a : attributes) {
	    LOG.debug("findAndSetAttributeValue: a=" + a + "; a.isEmbedded()=" + a.isEmbedded());
	    if (a.isEmbedded()) {
		Object parent = parentInstance != null ? getAttributeValue(parentInstance, a) : null;
		Object aInstance = findAndSetAttributeValue(a.getType(), parent,
			a.getEmbeddableMetaEntity().getAttributes(), attribute, value, a.getEmbeddableMetaEntity());
		LOG.debug("findAndSetAttributeValue: embedded aInstance=" + aInstance);
		if (aInstance != null)
		    return writeMetaAttributeValue(parentInstance, parentClass, a, aInstance, entity);
	    } else if (a == attribute) {
		return writeMetaAttributeValue(parentInstance, parentClass, a, value, entity);
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
	    ModelValueArray<MetaAttribute> attributeValueArray) throws IllegalAccessException, InvocationTargetException {
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
    public ModelValueArray<MetaAttribute> getModifications(MetaEntity entity, Object entityInstance) throws IllegalAccessException, InvocationTargetException {
	ModelValueArray<MetaAttribute> attributeValueArray = new ModelValueArray();
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
