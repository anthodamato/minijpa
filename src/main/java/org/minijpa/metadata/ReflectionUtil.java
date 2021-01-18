package org.minijpa.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ReflectionUtil {
	public static Class<?> findTargetEntity(Field field) {
		Type type = field.getGenericType();
		Class<?> targetEntity = null;
		if (type instanceof ParameterizedType) {
			ParameterizedType aType = (ParameterizedType) type;
			Type[] fieldArgTypes = aType.getActualTypeArguments();
			for (Type fieldArgType : fieldArgTypes) {
				targetEntity = (Class<?>) fieldArgType;
			}
		}

		return targetEntity;
	}

}
