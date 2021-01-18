package org.minijpa.metadata;

public class BeanUtil {
	/**
	 * Capitalize the attribute name
	 * 
	 * @param name the attribute name
	 * @return the capitalized string
	 */
	public static String capitalize(String name) {
		if (name == null || name.length() == 0)
			return name;

		if (name.length() > 1 && (Character.isUpperCase(name.charAt(0)) || Character.isUpperCase(name.charAt(1))))
			return name;

		char chars[] = name.toCharArray();
		chars[0] = Character.toUpperCase(chars[0]);
		return new String(chars);
	}

}
