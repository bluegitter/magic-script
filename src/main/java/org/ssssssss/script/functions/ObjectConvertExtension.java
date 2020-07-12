package org.ssssssss.script.functions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 类型转换
 */
public class ObjectConvertExtension {

	/**
	 * 转int
	 */
	public static int asInt(Object val) {
		return asInt(val, 0);
	}

	/**
	 * 转int
	 *
	 * @param defaultValue 默认值
	 */
	public static int asInt(Object val, int defaultValue) {
		try {
			return Integer.parseInt(asString(val));
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * 转double
	 */
	public static double asDouble(Object val) {
		return asDouble(val, 0.0);
	}

	/**
	 * 转double
	 *
	 * @param defaultValue 默认值
	 */
	public static double asDouble(Object val, double defaultValue) {
		try {
			return Double.parseDouble(asString(val));
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * 转long
	 */
	public static long asLong(Object val) {
		return asLong(val, 0L);
	}

	/**
	 * 转long
	 *
	 * @param defaultValue 默认值
	 */
	public static long asLong(Object val, long defaultValue) {
		try {
			return Long.parseLong(asString(val));
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * 转byte
	 */
	public static byte asByte(Object val) {
		return asByte(val, (byte) 0);
	}

	/**
	 * 转byte
	 *
	 * @param defaultValue 默认值
	 */
	public static byte asByte(Object val, byte defaultValue) {
		try {
			return Byte.parseByte(asString(val));
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * 转short
	 */
	public static short asShort(Object val) {
		return asShort(val, (short) 0);
	}

	/**
	 * 转short
	 *
	 * @param defaultValue 默认值
	 */
	public static short asShort(Object val, short defaultValue) {
		try {
			return Short.parseShort(asString(val), defaultValue);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * 转float
	 */
	public static float asFloat(Object val) {
		return asFloat(val, 0.0f);
	}

	/**
	 * 转float
	 *
	 * @param defaultValue 默认值
	 */
	public static float asFloat(Object val, float defaultValue) {
		try {
			return Float.parseFloat(asString(val));
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * 转String
	 */
	public static String asString(Object val) {
		return asString(val, null);
	}

	/**
	 * 转Date
	 */
	public static Date asDate(Object val) {
		return asDate(val, "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * 转Date
	 */
	public static Date asDate(Object val, String format) {
		if (val == null) {
			return null;
		}
		if (val instanceof String) {
			try {
				return new SimpleDateFormat(format).parse(val.toString());
			} catch (ParseException e) {
				long longVal = asLong(val, -1);
				if (longVal > 0) {
					return asDate(longVal, format);
				}
			}
		} else if (val instanceof Date) {
			return (Date) val;
		} else if (val instanceof Number) {
			Number number = (Number) val;
			if (val.toString().length() == 10) { //10位时间戳
				return new Date(number.longValue() * 1000L);
			} else if (val.toString().length() == 13) {    //13位时间戳
				return new Date(number.longValue());
			}
		}
		return null;
	}

	/**
	 * 转String
	 *
	 * @param defaultValue 默认值
	 */
	public static String asString(Object val, String defaultValue) {
		return val == null ? defaultValue : val.toString();
	}
}
