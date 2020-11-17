package org.ssssssss.script.convert;

public interface ClassImplicitConvert {

	/**
	 * 转换顺序
	 */
	default int sort() {
		return Integer.MAX_VALUE;
	}

	/**
	 * 是否支持隐式自动转换
	 */
	boolean support(Class<?> from, Class<?> to);

	/**
	 * 转换
	 */
	Object convert(Object source, Class<?> target);
}
