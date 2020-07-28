package org.ssssssss.script.functions;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 类型判断
 */
public class ObjectTypeConditionExtension {

	/**
	 * 判断是否是目标类型
	 */
	public static boolean is(Object target, String type) {
		if(type == null){
			return false;
		}
		Class<?> clazz;
		if (target instanceof Class) {
			clazz = (Class<?>) target;
		} else {
			clazz = target.getClass();
		}
		if (clazz.getName().equals(type) || clazz.getSimpleName().equals(type)) {
			return true;
		}
		if ("string".equalsIgnoreCase(type) && clazz == String.class) {
			return true;
		}
		if ("int".equalsIgnoreCase(type) && clazz == Integer.class) {
			return true;
		}
		if ("double".equalsIgnoreCase(type) && clazz == Double.class) {
			return true;
		}
		if ("float".equalsIgnoreCase(type) && clazz == Float.class) {
			return true;
		}
		if ("long".equalsIgnoreCase(type) && clazz == Long.class) {
			return true;
		}
		if ("byte".equalsIgnoreCase(type) && clazz == Byte.class) {
			return true;
		}
		if ("short".equalsIgnoreCase(type) && clazz == Short.class) {
			return true;
		}
		if ("boolean".equalsIgnoreCase(type) && clazz == Boolean.class) {
			return true;
		}
		if ("decimal".equalsIgnoreCase(type) && clazz == BigDecimal.class) {
			return true;
		}
		return false;
	}

	/**
	 * 判断是否是目标类型
	 */
	public static boolean is(Object target,Class<?> clazz) {
		if(clazz == null){
			return false;
		}
		return clazz.isAssignableFrom(target.getClass());
	}

	/**
	 * 判断是否是String
	 */
	public static boolean isString(Object target) {
		return is(target, String.class);
	}

	/**
	 * 判断是否是int
	 */
	public static boolean isInt(Object target) {
		return is(target, Integer.class);
	}

	/**
	 * 判断是否是double
	 */
	public static boolean isDouble(Object target) {
		return is(target, Double.class);
	}

	/**
	 * 判断是否是long
	 */
	public static boolean isLong(Object target) {
		return is(target, Long.class);
	}

	/**
	 * 判断是否是byte
	 */
	public static boolean isByte(Object target) {
		return is(target, Byte.class);
	}

	/**
	 * 判断是否是short
	 */
	public static boolean isShort(Object target) {
		return is(target, Short.class);
	}

	/**
	 * 判断是否是boolean
	 */
	public static boolean isBoolean(Object target) {
		return is(target, Boolean.class);
	}

	/**
	 * 判断是否是BigDecimal
	 */
	public static boolean isDecimal(Object target) {
		return is(target, BigDecimal.class);
	}

	/**
	 * 判断是否是Date
	 */
	public static boolean isDate(Object target) {
		return is(target, Date.class);
	}


	/**
	 * 判断是否是数组
	 */
	public static boolean isArray(Object target) {
		if (target instanceof Class) {
			return ((Class<?>) target).isArray();
		}
		return target.getClass().isArray();
	}

	/**
	 * 判断是否是List
	 */
	public static boolean isList(Object target) {
		if (target instanceof Class) {
			return List.class.isAssignableFrom((Class<?>) target);
		}
		return List.class.isAssignableFrom(target.getClass());
	}

	/**
	 * 判断是否是集合
	 */
	public static boolean isCollection(Object target) {
		if (target instanceof Class) {
			return Collection.class.isAssignableFrom((Class<?>) target);
		}
		return Collection.class.isAssignableFrom(target.getClass());
	}

	/**
	 * 判断是否是Map
	 */
	public static boolean isMap(Object target) {
		if (target instanceof Class) {
			return Map.class.isAssignableFrom((Class<?>) target);
		}
		return Map.class.isAssignableFrom(target.getClass());
	}
}
