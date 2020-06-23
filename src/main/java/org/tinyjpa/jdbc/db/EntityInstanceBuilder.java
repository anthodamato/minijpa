package org.tinyjpa.jdbc.db;

import java.util.List;
import java.util.Optional;

import org.tinyjpa.jdbc.Attribute;
import org.tinyjpa.jdbc.AttributeValue;
import org.tinyjpa.jdbc.Entity;

public interface EntityInstanceBuilder {
	public Object build(Entity entity, List<Attribute> attributes, List<Object> values, Object idValue)
			throws Exception;

	public Object setAttributeValue(Object parentInstance, Class<?> parentClass, Attribute attribute, Object value)
			throws Exception;

	public Object getAttributeValue(Object parentInstance, Attribute attribute) throws Exception;

	public Optional<List<AttributeValue>> getChanges(Entity entity, Object entityInstance);

	public void removeChanges(Object entityInstance);

}
