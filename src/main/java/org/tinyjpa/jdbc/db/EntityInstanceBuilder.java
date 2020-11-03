package org.tinyjpa.jdbc.db;

import java.util.List;
import java.util.Optional;

import org.tinyjpa.jdbc.AttributeValue;
import org.tinyjpa.jdbc.MetaAttribute;
import org.tinyjpa.jdbc.MetaEntity;

public interface EntityInstanceBuilder {
	public Object build(MetaEntity entity, List<MetaAttribute> attributes, List<Object> values, Object idValue)
			throws Exception;

	public Object setAttributeValue(Object parentInstance, Class<?> parentClass, MetaAttribute attribute, Object value)
			throws Exception;

	public Object getAttributeValue(Object parentInstance, MetaAttribute attribute) throws Exception;

	public Optional<List<AttributeValue>> getChanges(MetaEntity entity, Object entityInstance);

	public void removeChanges(Object entityInstance);

}
