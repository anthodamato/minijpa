package org.minijpa.jpa.db;

import java.util.Collection;
import java.util.List;

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

    public void addManaged(Object entityInstance, Object idValue) throws Exception;

    public void removeManaged(Object entityInstance) throws Exception;

    public void markForRemoval(Object entityInstance) throws Exception;

    public List<Object> getManagedEntityList();

    public boolean isManaged(Object entityInstance) throws Exception;

//    public boolean isManaged(List<Object> entityInstanceList) throws Exception;
    public boolean isManaged(Collection<?> entityInstanceList) throws Exception;

    public void close();

    public void detach(Object entityInstance) throws Exception;

    public void detachAll() throws Exception;

    public void resetLockType();
}
