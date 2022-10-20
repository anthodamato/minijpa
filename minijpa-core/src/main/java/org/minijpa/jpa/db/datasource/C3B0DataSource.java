package org.minijpa.jpa.db.datasource;

import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

import javax.sql.DataSource;

import org.minijpa.metadata.BeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class C3B0DataSource {
	private Logger LOG = LoggerFactory.getLogger(C3B0DataSource.class);

	public DataSource getDataSource(Properties properties)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException, PropertyVetoException {
		Class<?> cs = Class.forName("com.mchange.v2.c3p0.ComboPooledDataSource");
		Object instance = cs.getDeclaredConstructor().newInstance();

		String driverClass = (String) properties.get("javax.persistence.jdbc.driver");
		if (driverClass == null || driverClass.isEmpty())
			throw new IllegalArgumentException("'javax.persistence.jdbc.driver' property not found");

		invokeSet(cs, instance, "setDriverClass", String.class, driverClass);

		String url = (String) properties.get("javax.persistence.jdbc.url");
		if (url == null || url.isEmpty())
			throw new IllegalArgumentException("'javax.persistence.jdbc.url' property not found");

		invokeSet(cs, instance, "setJdbcUrl", String.class, url);

		String user = (String) properties.get("javax.persistence.jdbc.user");
		invokeSet(cs, instance, "setUser", String.class, user);
		String password = (String) properties.get("javax.persistence.jdbc.password");
		invokeSet(cs, instance, "setPassword", String.class, password);

		String pv = (String) properties.get("c3p0.initialPoolSize");
		if (pv != null) {
			String methodName = BeanUtil.getSetterMethodName("initialPoolSize");
			invokeSet(cs, instance, methodName, int.class, Integer.valueOf(pv));
		}

		pv = (String) properties.get("c3p0.minPoolSize");
		if (pv != null) {
			String methodName = BeanUtil.getSetterMethodName("minPoolSize");
			invokeSet(cs, instance, methodName, int.class, Integer.valueOf(pv));
		}

		pv = (String) properties.get("c3p0.maxPoolSize");
		if (pv != null) {
			String methodName = BeanUtil.getSetterMethodName("maxPoolSize");
			invokeSet(cs, instance, methodName, int.class, Integer.valueOf(pv));
		}

		pv = (String) properties.get("c3p0.acquireIncrement");
		if (pv != null) {
			String methodName = BeanUtil.getSetterMethodName("acquireIncrement");
			invokeSet(cs, instance, methodName, int.class, Integer.valueOf(pv));
		}

		pv = (String) properties.get("c3p0.maxIdleTime");
		if (pv != null) {
			String methodName = BeanUtil.getSetterMethodName("maxIdleTime");
			invokeSet(cs, instance, methodName, int.class, Integer.valueOf(pv));
		}

		pv = (String) properties.get("c3p0.maxStatements");
		if (pv != null) {
			String methodName = BeanUtil.getSetterMethodName("maxStatements");
			invokeSet(cs, instance, methodName, int.class, Integer.valueOf(pv));
		}

		pv = (String) properties.get("c3p0.maxStatementsPerConnection");
		if (pv != null) {
			String methodName = BeanUtil.getSetterMethodName("maxStatementsPerConnection");
			invokeSet(cs, instance, methodName, int.class, Integer.valueOf(pv));
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
