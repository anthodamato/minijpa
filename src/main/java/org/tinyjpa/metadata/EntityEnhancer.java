package org.tinyjpa.metadata;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;

public class EntityEnhancer {
	private Logger LOG = LoggerFactory.getLogger(EntityEnhancer.class);

	public void enhance(String className) throws NotFoundException, CannotCompileException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		ClassPool pool = ClassPool.getDefault();
//		Loader cl = new Loader(pool);
		LOG.info("Enhancing: " + className);

		CtClass ct = pool.get(className);
		
//		CtMethod m = CtNewMethod.make("public void print() { System.out.println(\"Hello!!!\"); }", ct);
//		ct.addMethod(m);

//		CtMethod method = ct.getDeclaredMethod("setName");
//		method.insertBefore("print();");

		Class<?> delegateClass = EntityDelegate.class;
//		LOG.info("delegateClass.getPackage().getName()=" + delegateClass.getPackage().getName());
		pool.importPackage(delegateClass.getPackage().getName());
		CtField f = CtField.make("private EntityDelegate entityDelegate = EntityDelegate.getInstance();", ct);
		ct.addField(f);

		modifyGetterSetterMethods(ct);

		ct.toClass();

//		Class<?> c = cl.loadClass(className);
//		Object entity = c.newInstance();
//		LOG.info("entity.getClass().getClassLoader()=" + entity.getClass().getClassLoader());
	}

	private void modifyGetMethod(CtMethod ctMethod, CtField ctField) throws CannotCompileException, NotFoundException {
		String mc = ctField.getName() + " = (" + ctMethod.getReturnType().getName() + ") entityDelegate.get("
				+ ctField.getName() + ",\"" + ctField.getName() + "\", this);";
		LOG.info("modifyGetMethod: mc=" + mc);
		ctMethod.insertBefore(mc);
	}

	private void modifySetMethod(CtMethod ctMethod, CtField ctField) throws CannotCompileException, NotFoundException {
		String mc = "entityDelegate.set(" + ctField.getName() + ",\"" + ctField.getName() + "\", this);";
		LOG.info("modifySetMethod: mc=" + mc);
		ctMethod.insertBefore(mc);
	}

	private void modifyGetterSetterMethods(CtClass ctClass) throws CannotCompileException, NotFoundException {
		CtField[] ctFields = ctClass.getDeclaredFields();
		for (CtField ctField : ctFields) {
			LOG.info("modifyGetterSetterMethods: ctField.getName()=" + ctField.getName());
			LOG.info("modifyGetterSetterMethods: ctField.getModifiers()=" + ctField.getModifiers());
			if (ctField.getModifiers() != Modifier.PRIVATE)
				continue;

			Optional<CtMethod> getMethod = findGetMethod(ctClass, ctField);
			if (!getMethod.isPresent())
				continue;

			Optional<CtMethod> setMethod = findSetMethod(ctClass, ctField);
			if (!setMethod.isPresent())
				continue;

			modifyGetMethod(getMethod.get(), ctField);
			modifySetMethod(setMethod.get(), ctField);
		}
	}

	private String buildGetMethodName(String attrName) {
		if (attrName.length() > 1)
			return "get" + Character.toUpperCase(attrName.charAt(0)) + attrName.substring(1);
		else
			return "get" + Character.toUpperCase(attrName.charAt(0));
	}

	private String buildSetMethodName(String attrName) {
		if (attrName.length() > 1)
			return "set" + Character.toUpperCase(attrName.charAt(0)) + attrName.substring(1);
		else
			return "set" + Character.toUpperCase(attrName.charAt(0));
	}

	private Optional<CtMethod> findGetMethod(CtClass ctClass, CtField ctField) {
		try {
			CtMethod getMethod = ctClass.getDeclaredMethod(buildGetMethodName(ctField.getName()));
			LOG.info("findGetMethod: getMethod.getName()=" + getMethod.getName());
			CtClass[] params = getMethod.getParameterTypes();
			LOG.info("findGetMethod: params.length=" + params.length);
			if (params.length != 0)
				return Optional.empty();

			LOG.info("findGetMethod: ctField.getType().getName()=" + ctField.getType().getName());
			LOG.info("findGetMethod: getMethod.getReturnType().getName()=" + getMethod.getReturnType().getName());
			if (!getMethod.getReturnType().subtypeOf(ctField.getType()))
				return Optional.empty();

			LOG.info("findGetMethod: subtypeOf=true");
			return Optional.of(getMethod);
		} catch (NotFoundException e) {
			return Optional.empty();
		}
	}

	private Optional<CtMethod> findSetMethod(CtClass ctClass, CtField ctField) {
		try {
			CtMethod setMethod = ctClass.getDeclaredMethod(buildSetMethodName(ctField.getName()));
			LOG.info("findSetMethod: setMethod.getName()=" + setMethod.getName());
			CtClass[] params = setMethod.getParameterTypes();
			LOG.info("findSetMethod: params.length=" + params.length);
			if (params.length != 1)
				return Optional.empty();

			if (!ctField.getType().subtypeOf(params[0]))
				return Optional.empty();

			LOG.info("findSetMethod: setMethod.getReturnType().getName()=" + setMethod.getReturnType().getName());
			if (!setMethod.getReturnType().getName().equals("void")) // void type
				return Optional.empty();

			LOG.info("findSetMethod: subtypeOf=true");
			return Optional.of(setMethod);
		} catch (NotFoundException e) {
			return Optional.empty();
		}
	}

}
