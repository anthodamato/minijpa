package org.minijpa.jpa.db.datasource;

import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

import javax.sql.DataSource;

import org.minijpa.metadata.BeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBCPDataSource {
	private Logger LOG = LoggerFactory.getLogger(DBCPDataSource.class);

	public DataSource getDataSource(Properties properties)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException, PropertyVetoException {
		Class<?> cs = Class.forName("org.apache.commons.dbcp2.BasicDataSource");
		Object instance = cs.getDeclaredConstructor().newInstance();

		String driverClass = (String) properties.get("javax.persistence.jdbc.driver");
		if (driverClass == null || driverClass.isEmpty())
			throw new IllegalArgumentException("'javax.persistence.jdbc.driver' property not found");

		invokeSet(cs, instance, "setDriverClassName", String.class, driverClass);

		String url = (String) properties.get("javax.persistence.jdbc.url");
		if (url == null || url.isEmpty())
			throw new IllegalArgumentException("'javax.persistence.jdbc.url' property not found");

		invokeSet(cs, instance, "setUrl", String.class, url);

		String user = (String) properties.get("javax.persistence.jdbc.user");
		invokeSet(cs, instance, "setUsername", String.class, user);
		String password = (String) properties.get("javax.persistence.jdbc.password");
		invokeSet(cs, instance, "setPassword", String.class, password);

		String pv = (String) properties.get("dbcp.initialSize");
		if (pv != null) {
			String methodName = BeanUtil.getSetterMethodName("initialSize");
			invokeSet(cs, instance, methodName, int.class, Integer.valueOf(pv));
		}

		pv = (String) properties.get("dbcp.maxTotal");
		if (pv != null) {
			String methodName = BeanUtil.getSetterMethodName("maxTotal");
			invokeSet(cs, instance, methodName, int.class, Integer.valueOf(pv));
		}


		pv = (String) properties.get("dbcp.maxIdle");
		if (pv != null) {
			String methodName = BeanUtil.getSetterMethodName("maxIdle");
			invokeSet(cs, instance, methodName, int.class, Integer.valueOf(pv));
		}

		pv = (String) properties.get("dbcp.minIdle");
		if (pv != null) {
			String methodName = BeanUtil.getSetterMethodName("minIdle");
			invokeSet(cs, instance, methodName, int.class, Integer.valueOf(pv));
		}

		pv = (String) properties.get("dbcp.maxWaitMillis");
		if (pv != null) {
			String methodName = BeanUtil.getSetterMethodName("maxWaitMillis");
			invokeSet(cs, instance, methodName, long.class, Integer.valueOf(pv));
		}

		return (DataSource) instance;
	}

	private void invokeSet(Class<?> cs, Object instance, String methodName, Class<?> paramType, Object value)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		Method method = cs.getMethod(methodName, paramType);
		method.invoke(instance, value);
	}
}
