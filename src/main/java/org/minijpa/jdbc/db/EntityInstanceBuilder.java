package org.minijpa.jdbc.db;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

import org.minijpa.jdbc.AttributeValue;
import org.minijpa.jdbc.AttributeValueArray;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;

public interface EntityInstanceBuilder {

    public Object build(MetaEntity entity, Object idValue)
	    throws Exception;

    public Object setAttributeValue(Object parentInstance, Class<?> parentClass, MetaAttribute attribute,
	    Object value, MetaEntity entity) throws Exception;

    public void setAttributeValues(MetaEntity entity, Object entityInstance, List<MetaAttribute> attributes,
	    List<Object> values) throws Exception;

    public Object getAttributeValue(Object parentInstance, MetaAttribute attribute) throws Exception;

    public Optional<List<AttributeValue>> getChanges(MetaEntity entity, Object entityInstance)
	    throws IllegalAccessException, InvocationTargetException;

    public void removeChanges(MetaEntity entity, Object entityInstance)
	    throws IllegalAccessException, InvocationTargetException;

    public AttributeValueArray getModifications(MetaEntity entity, Object entityInstance)
	    throws IllegalAccessException, InvocationTargetException;
}
