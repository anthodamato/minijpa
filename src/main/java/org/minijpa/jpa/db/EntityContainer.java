package org.minijpa.jpa.db;

import java.util.List;

import org.minijpa.jdbc.MetaAttribute;

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

//    public List<Object> findByForeignKey(MetaEntity owningEntity, MetaEntity targetEntity,
//	    MetaAttribute foreignKeyAttribute, Object foreignKey, EntityInstanceBuilder entityInstanceBuilder) throws Exception;
    public void addManaged(Object entityInstance, Object idValue) throws Exception;

    public void removeManaged(Object entityInstance) throws Exception;

    public void markForRemoval(Object entityInstance) throws Exception;

    public List<Object> getManagedEntityList();

    public boolean isManaged(Object entityInstance) throws Exception;

    public boolean isManaged(List<Object> entityInstanceList) throws Exception;

    public void close();

    public void detach(Object entityInstance) throws Exception;

    public void detachAll() throws Exception;

    public void resetLockType();

    public void saveForeignKey(Object entityInstance, MetaAttribute attribute, Object value);

    public Object getForeignKeyValue(Object entityInstance, MetaAttribute attribute);

    public void removeForeignKey(Object parentInstance, MetaAttribute attribute);
}
