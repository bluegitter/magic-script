package org.ssssssss.script.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

class ConstructorInvoker extends JavaInvoker<Constructor> {

	ConstructorInvoker(Constructor constructor) {
		super(constructor);
	}

	@Override
	Object invoke(Object target, Object... args) throws InvocationTargetException, IllegalAccessException, InstantiationException {
		return getExecutable().newInstance(args);
	}
}
