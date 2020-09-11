package org.tinyjpa.jdbc.db;

import java.util.List;
import java.util.Map;

import org.tinyjpa.jdbc.MetaAttribute;

public interface EntityContainer {
	public void save(Object entityInstance) throws Exception;

	public Object find(Class<?> entityClass, Object primaryKey) throws Exception;

	public boolean isSaved(Object entityInstance) throws Exception;

	public boolean isSaved(List<Object> entityInstanceList) throws Exception;

	public void end();

	public void save(Object entityInstance, Object idValue) throws Exception;

	public void remove(Object entityInstance, Object idValue);

	public void detach(Object entityInstance) throws Exception;

	public void saveForeignKey(Object entityInstance, MetaAttribute attribute, Object value);

	public Object getForeignKeyValue(Object entityInstance, MetaAttribute attribute);

	public void addToPendingNew(Object entityInstance);

	public List<Object> getPendingNew();

	public void removePendingNew(Object entityInstance);

	public void addToPendingNewAttributes(MetaAttribute attribute, Object entityInstance, List<Object> objects);

	public List<MetaAttribute> getPendingNewAttributes();

	public Map<Object, List<Object>> getPendingNewAttributeValue(MetaAttribute attribute);

	public void removePendingNewAttribute(MetaAttribute attribute, Object entityInstance);

	public void setLoadedFromDb(Object entityInstance);

	public void removeLoadedFromDb(Object entityInstance);

	public boolean isLoadedFromDb(Object entityInstance);
}
