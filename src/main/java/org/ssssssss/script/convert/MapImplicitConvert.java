package org.ssssssss.script.convert;

import org.ssssssss.script.functions.MapExtension;
import org.ssssssss.script.reflection.JavaReflection;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

public class MapImplicitConvert implements ClassImplicitConvert {
	@Override
	public boolean support(Class<?> from, Class<?> to) {
		return Map.class.isAssignableFrom(from) && !JavaReflection.isPrimitiveAssignableFrom(to, to)
				&& !to.isArray() && !Collection.class.isAssignableFrom(to)
				&& !Iterator.class.isAssignableFrom(to) && !Enumeration.class.isAssignableFrom(to);
	}

	@Override
	public Object convert(Object source, Class<?> target) {
		return MapExtension.asBean((Map<?, ?>) source, target);
	}
}
