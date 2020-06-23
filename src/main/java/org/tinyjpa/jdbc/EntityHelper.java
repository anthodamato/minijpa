package org.tinyjpa.jdbc;

public class EntityHelper {
	public Object getIdValue(Entity entity, Object entityInstance) throws Exception {
		Attribute id = entity.getId();
		return id.getReadMethod().invoke(entityInstance);
	}
}
