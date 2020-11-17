package org.ssssssss.script.functions;

import org.ssssssss.script.annotation.Comment;
import org.ssssssss.script.reflection.AbstractReflection;
import org.ssssssss.script.reflection.JavaReflection;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class MapExtension {

	private final static AbstractReflection reflection = AbstractReflection.getInstance();

	@Comment("Map类型对象转JavaBean")
	public static Object asBean(Map<?, ?> source, @Comment("目标Class") Class<?> target) {
		Object result = null;
		try {
			result = target.newInstance();
			Set<? extends Map.Entry<?, ?>> entries = source.entrySet();
			for (Map.Entry<?, ?> entry : entries) {
				Object value = entry.getValue();
				String member = Objects.toString(entry.getKey(), null);
				if (value != null && member != null) {
					Field field = reflection.getField(target, member);
					setFieldValue(result, field, value);
				}
			}
		} catch (InstantiationException | IllegalAccessException ignored) {
			ignored.printStackTrace();
		}
		return result;
	}

	private static void setFieldValue(Object object, Field field, Object value) {
		if (field != null) {
			try {
				if (ObjectTypeConditionExtension.isCollection(field.getType())) {
					Type genericType = field.getGenericType();
					if (genericType instanceof ParameterizedType) {
						Class<?> type = (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
						reflection.setFieldValue(object, field, StreamExtension.asBean(value, type));
					}
				} else if (field.getType().isArray()) {
					reflection.setFieldValue(object, field, StreamExtension.asBean(value, field.getType().getComponentType(), true));
				} else if (JavaReflection.isPrimitiveAssignableFrom(value.getClass(), field.getType()) || field.getType().isAssignableFrom(value.getClass())) {
					reflection.setFieldValue(object, field, value);
				}
			} catch (Exception ignored) {
				ignored.printStackTrace();
			}
		}
	}
}
