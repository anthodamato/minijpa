package org.tinyjpa.jpa;

import java.lang.reflect.InvocationTargetException;

import javax.persistence.spi.PersistenceUnitInfo;

public interface PersistenceContext {
	public void persist(Object entityInstance);

	public Object find(Class<?> entityClass, Object primaryKey) throws Exception;

	public boolean isPersistentOnDb(Object entityInstance) throws IllegalAccessException, InvocationTargetException;

	public void end();

	public PersistenceUnitInfo getPersistenceUnitInfo();

}
