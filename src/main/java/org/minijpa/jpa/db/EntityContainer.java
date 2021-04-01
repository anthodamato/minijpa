package org.minijpa.jpa.db;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.db.EntityInstanceBuilder;

public interface EntityContainer {

    /**
     * Finds an entity instance.
     *
     * @param entityClass
     * @param primaryKey
     * @return
     * @throws Exception
     */
    public Object find(Class<?> entityClass, Object primaryKey) throws Exception;

    public List<Object> findByForeignKey(MetaEntity owningEntity, MetaEntity targetEntity,
	    MetaAttribute foreignKeyAttribute, Object foreignKey, EntityInstanceBuilder entityInstanceBuilder) throws Exception;

    public boolean isManaged(Object entityInstance) throws Exception;

    public boolean isManaged(List<Object> entityInstanceList) throws Exception;

    public void close();

    /**
     * Save in the PC a persistent and flushed entity.
     *
     * @param entityInstance
     * @param idValue
     * @throws Exception
     */
    public void addFlushedPersist(Object entityInstance, Object idValue) throws Exception;

    /**
     * Save in the PC a no flushed entity.
     *
     * @param entityInstance
     * @param idValue
     * @throws Exception
     */
    public void addNotFlushedPersist(Object entityInstance, Object idValue) throws Exception;

    public void addNotFlushedRemove(Object entityInstance, Object idValue) throws Exception;

    public boolean isNotFlushedRemove(Class<?> c, Object primaryKey) throws Exception;

    public boolean isFlushedPersist(Object entityInstance) throws Exception;

    public boolean isNotFlushedPersist(Object entityInstance) throws Exception;

    public boolean isNotFlushedPersist(Object entityInstance, Object primaryKey) throws Exception;

    public List<Object> getNotFlushedEntities();

    /**
     * Save in the PC a persistent and flushed entity.
     *
     * @param entityInstance
     * @throws Exception
     */
    public void addFlushedPersist(Object entityInstance) throws Exception;

    public Set<Class<?>> getNotFlushedPersistClasses();

    public Set<Class<?>> getNotFlushedRemoveClasses();

    public Map<Object, Object> getNotFlushedPersistEntities(Class<?> c);

    public Map<Object, Object> getNotFlushedRemoveEntities(Class<?> c);

    public Set<Class<?>> getFlushedPersistClasses();

    public Map<Object, Object> getFlushedPersistEntities(Class<?> c);

    public void removeNotFlushedPersist(Object entityInstance, Object primaryKey) throws Exception;

    public void removeNotFlushedRemove(Object entityInstance, Object primaryKey) throws Exception;

    public void removeFlushed(Object entityInstance, Object idValue);

    public void detach(Object entityInstance) throws Exception;

    public void detachAll() throws Exception;

    public boolean isDetached(Object entityInstance) throws Exception;

    public void saveForeignKey(Object entityInstance, MetaAttribute attribute, Object value);

    public Object getForeignKeyValue(Object entityInstance, MetaAttribute attribute);

    public void removeForeignKey(Object parentInstance, MetaAttribute attribute);

    public void addPendingNew(Object entityInstance);

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
