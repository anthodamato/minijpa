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
package org.minijpa.jpa.db;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CollectionUtils {

	/**
	 * Given an attribute class declared as interface, this method returns an
	 * implementation class to use in the lazy attributes.
	 *
	 * @param c
	 * @return
	 */
	public static Class<?> findCollectionImplementationClass(Class<?> c) {
		if (!isCollectionClass(c))
			throw new IllegalArgumentException("Class '" + c.getName() + "' is not a collection or map class");

		if (!c.isInterface() && !Modifier.isAbstract(c.getModifiers()))
			return c;

		if (c == Collection.class || c == Set.class)
			return HashSet.class;

		if (c == List.class)
			return ArrayList.class;

		if (c == Map.class)
			return HashMap.class;

		return null;
	}

	public static Object createInstance(Object currentValue, Class<?> collectionClass) throws Exception {
		if (currentValue != null)
			return (Collection<Object>) currentValue;

		Constructor<?>[] cs = collectionClass.getConstructors();
		for (Constructor<?> c : cs) {
			if (c.getParameterCount() == 0)
				return c.newInstance();
		}

		throw new IllegalArgumentException("Unable to create a '" + collectionClass.getName() + "' instance");
	}

	public static boolean isCollectionName(String name) {
		if (name.equals(Collection.class.getName()))
			return true;

		if (name.equals(Map.class.getName()) || name.equals(HashMap.class.getName()))
			return true;

		if (name.equals(List.class.getName()))
			return true;

		if (name.equals(Set.class.getName()) || name.equals(HashSet.class.getName()))
			return true;

		return false;
	}

	private static boolean implementsInterface(Class<?> c, Class<?> interfaceClass) {
		if (c == interfaceClass)
			return true;

		Class<?>[] ics = c.getInterfaces();
		for (Class<?> ic : ics) {
			if (ic == interfaceClass)
				return true;
		}

		Class<?> sc = c.getSuperclass();
		if (sc == null)
			return false;

		return implementsInterface(sc, interfaceClass);
	}

	public static boolean isCollectionClass(Class<?> c) {
		if (implementsInterface(c, Collection.class))
			return true;

		if (implementsInterface(c, Map.class))
			return true;

		return false;
	}

	public static boolean isCollectionEmpty(Object instance) {
		Class<?> c = instance.getClass();
		if (implementsInterface(c, Collection.class))
			return ((Collection<?>) instance).isEmpty();
		else if (implementsInterface(c, Map.class))
			return ((Map<?, ?>) instance).isEmpty();

		return true;
	}

	public static Collection<?> getCollectionFromCollectionOrMap(Object instance) {
		Class<?> c = instance.getClass();
		if (implementsInterface(c, Collection.class))
			return (Collection<?>) instance;
		else if (implementsInterface(c, Map.class)) {
			Map<?, ?> map = (Map<?, ?>) instance;
			return map.values();
		}

		return new ArrayList<>();
	}

}
