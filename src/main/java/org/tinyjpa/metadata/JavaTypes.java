package org.tinyjpa.metadata;

public class JavaTypes {
	public static Class<?> getClass(String className) throws ClassNotFoundException {
		if (className.equals("byte"))
			return Byte.TYPE;

		if (className.equals("short"))
			return Short.TYPE;

		if (className.equals("int"))
			return Integer.TYPE;

		if (className.equals("long"))
			return Long.TYPE;

		if (className.equals("float"))
			return Float.TYPE;

		if (className.equals("double"))
			return Double.TYPE;

		if (className.equals("boolean"))
			return Boolean.TYPE;

		if (className.equals("char"))
			return Character.TYPE;

		return Class.forName(className);
	}
}
