package org.tinyjpa.metadata;

import java.util.List;

import org.tinyjpa.jdbc.Attribute;
import org.tinyjpa.jdbc.Entity;

public interface EntityInstanceBuilder {
	public Object build(Entity entity, List<Attribute> attributes, List<Object> values, Object idValue)
			throws Exception;

	public Object setAttributeValue(Object parentInstance, Class<?> parentClass, Attribute attribute, Object value)
			throws Exception;

	public Object getAttributeValue(Object parentInstance, Attribute attribute) throws Exception;

}
