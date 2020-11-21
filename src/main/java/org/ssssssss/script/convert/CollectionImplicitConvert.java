package org.ssssssss.script.convert;

import org.ssssssss.script.functions.StreamExtension;
import org.ssssssss.script.reflection.JavaReflection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

public class CollectionImplicitConvert implements ClassImplicitConvert {

	private Class<?> fromClazz;

	private Class<?> toClazz;

	@Override
	public boolean support(Class<?> from, Class<?> to) {
		if ((from.isArray() || Collection.class.isAssignableFrom(from) || Iterator.class.isAssignableFrom(from) || Enumeration.class.isAssignableFrom(from)) &&
				(to.isArray() || Collection.class.isAssignableFrom(to) || Iterator.class.isAssignableFrom(to) || Enumeration.class.isAssignableFrom(to))) {
			return (fromClazz = getGenericType(from)) != null
					&& !JavaReflection.isPrimitiveAssignableFrom(fromClazz, fromClazz)
					&& (toClazz = getGenericType(to)) != null
					&& !JavaReflection.isPrimitiveAssignableFrom(toClazz, toClazz)
					&& toClazz.isAssignableFrom(fromClazz);
		}
		return false;
	}

	private Class<?> getGenericType(Class<?> target) {
		Type type = target.getGenericSuperclass();
		if (type instanceof ParameterizedType) {
			return (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
		}
		return null;
	}

	@Override
	public Object convert(Object source, Class<?> target) {
		return StreamExtension.asBean(source, target, target.isArray());
	}
}