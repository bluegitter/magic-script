package org.ssssssss.script.functions;

import org.ssssssss.script.interpreter.JavaReflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

public class ClassExtension {

	public static Object newInstance(Class<?> clazz) throws IllegalAccessException, InstantiationException {
		return clazz.newInstance();
	}

	public static Object newInstance(Class<?> clazz, Object... values) throws IllegalAccessException, InstantiationException, InvocationTargetException {
		if (values == null || values.length == 0) {
			return newInstance(clazz);
		}
		Class<?>[] parametersTypes = new Class<?>[values.length];
		for (int i = 0; i < values.length; i++) {
			Object value = values[i];
			parametersTypes[i] = value == null ? JavaReflection.Null.class : value.getClass();
		}
		List<Constructor<?>> constructors = Arrays.asList(clazz.getConstructors());
		Constructor<?> constructor = JavaReflection.findExecutable(constructors, parametersTypes);
		return constructor.newInstance(values);
	}
}
