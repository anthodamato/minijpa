package org.tinyjpa.jdbc.db;

import org.tinyjpa.jdbc.Attribute;

public interface EntityContainer {
	public void save(Object entityInstance) throws Exception;

	public Object find(Class<?> entityClass, Object primaryKey) throws Exception;

	public boolean isSaved(Object entityInstance) throws Exception;

	public void end();

	public void save(Object entityInstance, Object primaryKey) throws Exception;

	public void remove(Object entityInstance, Object primaryKey);

	public void detach(Object entityInstance) throws Exception;

	public void saveForeignKey(Object entityInstance, Attribute attribute, Object value);

	public Object getForeignKeyValue(Object entityInstance, Attribute attribute);
}
