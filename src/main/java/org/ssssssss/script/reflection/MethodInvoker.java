package org.ssssssss.script.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodInvoker extends JavaInvoker<Method> {

	public MethodInvoker(Method method) {
		super(method);
	}
	public MethodInvoker(Method method,Object defaultTarget) {
		super(method);
		setDefaultTarget(defaultTarget);
	}

	@Override
	Object invoke(Object target, Object... args) throws InvocationTargetException, IllegalAccessException {
		Object defaultTarget = getDefaultTarget();
		return getExecutable().invoke(defaultTarget == null ? target : defaultTarget, args);
	}
}
