/*
 * Copyright 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

	public static String getSetterMethodName(String propertyName) {
		return "set" + capitalize(propertyName);
	}

	public static String getGetterMethodName(String propertyName) {
		return "get" + capitalize(propertyName);
	}

	public static String getIsMethodName(String propertyName) {
		return "is" + capitalize(propertyName);
	}
}
