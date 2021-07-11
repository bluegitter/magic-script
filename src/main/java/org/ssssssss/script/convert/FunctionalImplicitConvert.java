package org.ssssssss.script.convert;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.runtime.function.MagicScriptLambdaFunction;

import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.function.Function;

/**
 *	脚本内部lambda到Java函数式的转换
 */
public class FunctionalImplicitConvert implements ClassImplicitConvert {

	private final ClassLoader classLoader = FunctionalImplicitConvert.class.getClassLoader();

	@Override
	public boolean support(Class<?> from, Class<?> to) {
		return MagicScriptLambdaFunction.class.isAssignableFrom(from) && to.getAnnotation(FunctionalInterface.class) != null;
	}

	@Override
	public Object convert(MagicScriptContext context, Object source, Class<?> target) {
		MagicScriptLambdaFunction function = (MagicScriptLambdaFunction) source;
		if(target == Function.class){
			return (Function<Object[],Object>)args -> function.apply(context, args);
		}
		return Proxy.newProxyInstance(classLoader, new Class[]{target}, (proxy, method, args) -> {
			if(Modifier.isAbstract(method.getModifiers())){
				return function.apply(context, args == null ? new Object[0] : args);
			}
			if("toString".equalsIgnoreCase(method.getName())){
				return "Proxy(" + source + "," + target +")";
			}
			return null;
		});
	}
}
