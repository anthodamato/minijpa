package org.tinyjpa.jpa;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import javax.persistence.spi.PersistenceUnitInfo;

public interface PersistenceContext {
	public void persist(Object entityInstance);

	public Object find(Class<?> entityClass, Object primaryKey)
			throws IllegalAccessException, InvocationTargetException, InstantiationException, SQLException;

	public boolean isPersistentOnDb(Object entityInstance) throws IllegalAccessException, InvocationTargetException;

	public void end();

	public PersistenceUnitInfo getPersistenceUnitInfo();

}
