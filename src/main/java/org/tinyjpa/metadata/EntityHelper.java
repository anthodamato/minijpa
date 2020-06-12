package org.tinyjpa.metadata;

import java.lang.reflect.InvocationTargetException;

import org.tinyjpa.jdbc.Attribute;
import org.tinyjpa.jdbc.Entity;

public class EntityHelper {
	public Object getIdValue(Entity entity, Object entityInstance)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Attribute id = entity.getId();
		return id.getReadMethod().invoke(entityInstance);
	}
}
