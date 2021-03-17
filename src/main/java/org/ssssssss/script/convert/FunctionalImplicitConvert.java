package org.ssssssss.script.convert;

import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.function.Function;

public class FunctionalImplicitConvert implements ClassImplicitConvert {

	private final ClassLoader classLoader = FunctionalImplicitConvert.class.getClassLoader();

	@Override
	public boolean support(Class<?> from, Class<?> to) {
		return Function.class.isAssignableFrom(from) && to.getAnnotation(FunctionalInterface.class) != null;
	}

	@Override
	public Object convert(Object source, Class<?> target) {
		return Proxy.newProxyInstance(classLoader, new Class[]{target}, (proxy, method, args) -> {
			if(Modifier.isAbstract(method.getModifiers())){
				Function<Object[],Object> function = (Function<Object[], Object>) source;
				Object value = function.apply(args);
				return value;
			}
			if("toString".equalsIgnoreCase(method.getName())){
				return "Proxy(" + source + "," + target +")";
			}
			return null;
		});
	}
}
