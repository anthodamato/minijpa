package org.tinyjpa.metadata;

import org.tinyjpa.jdbc.Attribute;
import org.tinyjpa.jdbc.Entity;

public class EntityHelper {
	public Object getIdValue(Entity entity, Object entityInstance) throws Exception {
		Attribute id = entity.getId();
		return id.getReadMethod().invoke(entityInstance);
	}
}
