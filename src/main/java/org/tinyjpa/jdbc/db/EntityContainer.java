package org.tinyjpa.jdbc.db;

import java.util.List;

import org.tinyjpa.jdbc.Attribute;

public interface EntityContainer {
	public void save(Object entityInstance) throws Exception;

	public Object find(Class<?> entityClass, Object primaryKey) throws Exception;

	public boolean isSaved(Object entityInstance) throws Exception;

	public void end();

	public void save(Object entityInstance, Object idValue) throws Exception;

	public void remove(Object entityInstance, Object idValue);

	public void detach(Object entityInstance) throws Exception;

	public void saveForeignKey(Object entityInstance, Attribute attribute, Object value);

	public Object getForeignKeyValue(Object entityInstance, Attribute attribute);

	public void addToPendingNew(Object entityInstance) throws Exception;

	public List<Object> getPendingNew();

	public void removePendingNew(Object entityInstance);
}
