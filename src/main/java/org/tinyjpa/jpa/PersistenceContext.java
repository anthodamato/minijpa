package org.tinyjpa.jpa;

import javax.persistence.spi.PersistenceUnitInfo;

public interface PersistenceContext {
	public void persist(Object entityInstance) throws Exception;

	public Object find(Class<?> entityClass, Object primaryKey) throws Exception;

	public boolean isPersistentOnDb(Object entityInstance) throws Exception;

	public void end();

	public PersistenceUnitInfo getPersistenceUnitInfo();

	public void add(Object entityInstance, Object primaryKey);

	public void remove(Object entityInstance, Object primaryKey);

	public void detach(Object entityInstance) throws Exception;

}
