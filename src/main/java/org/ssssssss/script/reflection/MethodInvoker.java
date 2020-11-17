package org.ssssssss.script.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodInvoker extends JavaInvoker<Method> {

	public MethodInvoker(Method method) {
		super(method);
	}

	@Override
	Object invoke(Object target, Object... args) throws InvocationTargetException, IllegalAccessException {
		return getExecutable().invoke(target, args);
	}
}
