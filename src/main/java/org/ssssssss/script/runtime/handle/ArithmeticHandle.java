package org.ssssssss.script.runtime.handle;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static java.lang.invoke.MethodHandles.catchException;
import static java.lang.invoke.MethodType.methodType;

public class ArithmeticHandle {

	private static final MethodHandle FALLBACK;

	static {
		try {
			MethodHandles.Lookup lookup = MethodHandles.lookup();
			FALLBACK = lookup.findStatic(ArithmeticHandle.class, "fallback", methodType(Object.class, MethodCallSite.class, Object[].class));
		} catch (NoSuchMethodException | IllegalAccessException e) {
			throw new Error("ArithmeticHandle初始化失败", e);
		}
	}

	public static CallSite bootstrap(MethodHandles.Lookup caller, String name, MethodType type, int count) {
		MethodCallSite callSite = new MethodCallSite(caller, name, type, ArithmeticHandle.class);
		MethodHandle fallback = FALLBACK.bindTo(callSite)
				.asCollector(Object[].class, type.parameterCount())
				.asType(type);
		callSite.setTarget(fallback);
		callSite.fallback = fallback;
		return callSite;
	}

	public static Object fallback(MethodCallSite callSite, Object[] args) throws Throwable {
		Class<?> arg1Class = (args[0] == null) ? Object.class : args[0].getClass();
		Class<?> arg2Class = (args[1] == null) ? Object.class : args[1].getClass();
		MethodHandle target;
		try {
			target = callSite.findStatic(methodType(Object.class, arg1Class, arg2Class));
		} catch (Throwable ignored) {
			try {
				target = callSite.findStatic(callSite.methodName + "_fallback", methodType(Object.class, Object.class, Object.class));
			} catch (Throwable t) {
				return reject(args[0], args[1], callSite.methodName);
			}
		}
		target = target.asType(methodType(Object.class, Object.class, Object.class));
		target = catchException(target, ClassCastException.class, callSite.fallback);
		callSite.setTarget(target);
		return target.invokeWithArguments(args);
	}


	/* byte + 操作实现 */
	public static Object plus(Byte a, Byte b) {
		return a + b;
	}

	public static Object plus(Byte a, Short b) {
		return a + b;
	}

	public static Object plus(Byte a, Integer b) {
		return a + b;
	}

	public static Object plus(Byte a, Float b) {
		return a + b;
	}

	public static Object plus(Byte a, Double b) {
		return a + b;
	}

	public static Object plus(Byte a, Long b) {
		return a + b;
	}

	public static Object plus(Byte a, BigDecimal b) {
		return b.add(new BigDecimal(a));
	}

	public static Object plus(Byte a, String b) {
		return a + b;
	}
	/* byte + 操作实现 */

	/* short + 操作实现 */
	public static Object plus(Short a, Byte b) {
		return a + b;
	}

	public static Object plus(Short a, Short b) {
		return a + b;
	}

	public static Object plus(Short a, Integer b) {
		return a + b;
	}

	public static Object plus(Short a, Float b) {
		return a + b;
	}

	public static Object plus(Short a, Double b) {
		return a + b;
	}

	public static Object plus(Short a, Long b) {
		return a + b;
	}

	public static Object plus(Short a, BigDecimal b) {
		return b.add(new BigDecimal(a));
	}

	public static Object plus(Short a, String b) {
		return a + b;
	}
	/* short + 操作实现 */

	/* int + 操作实现 */
	public static Object plus(Integer a, Byte b) {
		return a + b;
	}

	public static Object plus(Integer a, Short b) {
		return a + b;
	}

	public static Object plus(Integer a, Integer b) {
		return a + b;
	}

	public static Object plus(Integer a, Float b) {
		return a + b;
	}

	public static Object plus(Integer a, Double b) {
		return a + b;
	}

	public static Object plus(Integer a, Long b) {
		return a + b;
	}

	public static Object plus(Integer a, BigDecimal b) {
		return b.add(new BigDecimal(a));
	}

	public static Object plus(Integer a, String b) {
		return a + b;
	}
	/* int + 操作实现 */

	/* float + 操作实现 */
	public static Object plus(Float a, Byte b) {
		return a + b;
	}

	public static Object plus(Float a, Short b) {
		return a + b;
	}

	public static Object plus(Float a, Integer b) {
		return a + b;
	}

	public static Object plus(Float a, Float b) {
		return a + b;
	}

	public static Object plus(Float a, Double b) {
		return a + b;
	}

	public static Object plus(Float a, Long b) {
		return a + b;
	}

	public static Object plus(Float a, BigDecimal b) {
		return b.add(new BigDecimal(a));
	}

	public static Object plus(Float a, String b) {
		return a + b;
	}
	/* float + 操作实现 */

	/* double + 操作实现 */
	public static Object plus(Double a, Byte b) {
		return a + b;
	}

	public static Object plus(Double a, Short b) {
		return a + b;
	}

	public static Object plus(Double a, Integer b) {
		return a + b;
	}

	public static Object plus(Double a, Float b) {
		return a + b;
	}

	public static Object plus(Double a, Double b) {
		return a + b;
	}

	public static Object plus(Double a, Long b) {
		return a + b;
	}

	public static Object plus(Double a, BigDecimal b) {
		return b.add(new BigDecimal(a));
	}

	public static Object plus(Double a, String b) {
		return a + b;
	}
	/* double + 操作实现 */

	/* long + 操作实现 */
	public static Object plus(Long a, Byte b) {
		return a + b;
	}

	public static Object plus(Long a, Short b) {
		return a + b;
	}

	public static Object plus(Long a, Integer b) {
		return a + b;
	}

	public static Object plus(Long a, Float b) {
		return a + b;
	}

	public static Object plus(Long a, Double b) {
		return a + b;
	}

	public static Object plus(Long a, Long b) {
		return a + b;
	}

	public static Object plus(Long a, BigDecimal b) {
		return b.add(new BigDecimal(a));
	}

	public static Object plus(Long a, String b) {
		return a + b;
	}
	/* long + 操作实现 */

	/* bigecimal + 操作实现 */
	public static Object plus(BigDecimal a, Byte b) {
		return a.add(new BigDecimal(b));
	}

	public static Object plus(BigDecimal a, Short b) {
		return a.add(new BigDecimal(b));
	}

	public static Object plus(BigDecimal a, Integer b) {
		return a.add(new BigDecimal(b));
	}

	public static Object plus(BigDecimal a, Float b) {
		return a.add(new BigDecimal(b));
	}

	public static Object plus(BigDecimal a, Double b) {
		return a.add(new BigDecimal(b));
	}

	public static Object plus(BigDecimal a, Long b) {
		return a.add(new BigDecimal(b));
	}

	public static Object plus(BigDecimal a, BigDecimal b) {
		return a.add(b);
	}

	public static Object plus(BigDecimal a, String b) {
		return a + b;
	}
	/* bigdecimal + 操作实现 */

	/* string + 操作实现 */
	public static Object plus(String a, Byte b) {
		return a + b;
	}

	public static Object plus(String a, Short b) {
		return a + b;
	}

	public static Object plus(String a, Integer b) {
		return a + b;
	}

	public static Object plus(String a, Float b) {
		return a + b;
	}

	public static Object plus(String a, Double b) {
		return a + b;
	}

	public static Object plus(String a, Long b) {
		return a + b;
	}

	public static Object plus(String a, BigDecimal b) {
		return a + b;
	}

	public static Object plus(String a, String b) {
		return a + b;
	}

	public static Object plus(String a, Object b) {
		return a + b;
	}
	/* string + 操作实现 */

	public static Object plus_fallback(Object a, Object b) {
		if(a == null && b == null){
			return null;
		}
		return "" + a + b;
	}


	/* byte - 操作实现 */
	public static Object minus(Byte a, Byte b) {
		return a - b;
	}

	public static Object minus(Byte a, Short b) {
		return a - b;
	}

	public static Object minus(Byte a, Integer b) {
		return a - b;
	}

	public static Object minus(Byte a, Float b) {
		return a - b;
	}

	public static Object minus(Byte a, Double b) {
		return a - b;
	}

	public static Object minus(Byte a, Long b) {
		return a - b;
	}

	public static Object minus(Byte a, BigDecimal b) {
		return new BigDecimal(a).subtract(b);
	}
	/* byte - 操作实现 */

	/* short - 操作实现 */
	public static Object minus(Short a, Byte b) {
		return a - b;
	}

	public static Object minus(Short a, Short b) {
		return a - b;
	}

	public static Object minus(Short a, Integer b) {
		return a - b;
	}

	public static Object minus(Short a, Float b) {
		return a - b;
	}

	public static Object minus(Short a, Double b) {
		return a - b;
	}

	public static Object minus(Short a, Long b) {
		return a - b;
	}

	public static Object minus(Short a, BigDecimal b) {
		return new BigDecimal(a).subtract(b);
	}
	/* short - 操作实现 */

	/* int - 操作实现 */
	public static Object minus(Integer a, Byte b) {
		return a - b;
	}

	public static Object minus(Integer a, Short b) {
		return a - b;
	}

	public static Object minus(Integer a, Integer b) {
		return a - b;
	}

	public static Object minus(Integer a, Float b) {
		return a - b;
	}

	public static Object minus(Integer a, Double b) {
		return a - b;
	}

	public static Object minus(Integer a, Long b) {
		return a - b;
	}

	public static Object minus(Integer a, BigDecimal b) {
		return new BigDecimal(a).subtract(b);
	}
	/* int - 操作实现 */

	/* float - 操作实现 */
	public static Object minus(Float a, Byte b) {
		return a - b;
	}

	public static Object minus(Float a, Short b) {
		return a - b;
	}

	public static Object minus(Float a, Integer b) {
		return a - b;
	}

	public static Object minus(Float a, Float b) {
		return a - b;
	}

	public static Object minus(Float a, Double b) {
		return a - b;
	}

	public static Object minus(Float a, Long b) {
		return a - b;
	}

	public static Object minus(Float a, BigDecimal b) {
		return new BigDecimal(a).subtract(new BigDecimal(a));
	}
	/* float - 操作实现 */

	/* double - 操作实现 */
	public static Object minus(Double a, Byte b) {
		return a - b;
	}

	public static Object minus(Double a, Short b) {
		return a - b;
	}

	public static Object minus(Double a, Integer b) {
		return a - b;
	}

	public static Object minus(Double a, Float b) {
		return a - b;
	}

	public static Object minus(Double a, Double b) {
		return a - b;
	}

	public static Object minus(Double a, Long b) {
		return a - b;
	}

	public static Object minus(Double a, BigDecimal b) {
		return new BigDecimal(a).subtract(b);
	}
	/* double - 操作实现 */

	/* long - 操作实现 */
	public static Object minus(Long a, Byte b) {
		return a - b;
	}

	public static Object minus(Long a, Short b) {
		return a - b;
	}

	public static Object minus(Long a, Integer b) {
		return a - b;
	}

	public static Object minus(Long a, Float b) {
		return a - b;
	}

	public static Object minus(Long a, Double b) {
		return a - b;
	}

	public static Object minus(Long a, Long b) {
		return a - b;
	}

	public static Object minus(Long a, BigDecimal b) {
		return new BigDecimal(a).subtract(b);
	}
	/* long - 操作实现 */

	/* bigecimal - 操作实现 */
	public static Object minus(BigDecimal a, Byte b) {
		return a.subtract(new BigDecimal(b));
	}

	public static Object minus(BigDecimal a, Short b) {
		return a.subtract(new BigDecimal(b));
	}

	public static Object minus(BigDecimal a, Integer b) {
		return a.subtract(new BigDecimal(b));
	}

	public static Object minus(BigDecimal a, Float b) {
		return a.subtract(new BigDecimal(b));
	}

	public static Object minus(BigDecimal a, Double b) {
		return a.subtract(new BigDecimal(b));
	}

	public static Object minus(BigDecimal a, Long b) {
		return a.subtract(new BigDecimal(b));
	}

	public static Object minus(BigDecimal a, BigDecimal b) {
		return a.subtract(b);
	}
	/* bigdecimal - 操作实现 */


	public static Object minus_fallback(Object a, Object b) {
		return reject(a, b, "-");
	}

	/* byte * 操作实现 */
	public static Object mul(Byte a, Byte b) {
		return a * b;
	}

	public static Object mul(Byte a, Short b) {
		return a * b;
	}

	public static Object mul(Byte a, Integer b) {
		return a * b;
	}

	public static Object mul(Byte a, Float b) {
		return a * b;
	}

	public static Object mul(Byte a, Double b) {
		return a * b;
	}

	public static Object mul(Byte a, Long b) {
		return a * b;
	}

	public static Object mul(Byte a, BigDecimal b) {
		return new BigDecimal(a).multiply(b);
	}
	/* byte * 操作实现 */

	/* short * 操作实现 */
	public static Object mul(Short a, Byte b) {
		return a * b;
	}

	public static Object mul(Short a, Short b) {
		return a * b;
	}

	public static Object mul(Short a, Integer b) {
		return a * b;
	}

	public static Object mul(Short a, Float b) {
		return a * b;
	}

	public static Object mul(Short a, Double b) {
		return a * b;
	}

	public static Object mul(Short a, Long b) {
		return a * b;
	}

	public static Object mul(Short a, BigDecimal b) {
		return new BigDecimal(a).multiply(b);
	}
	/* short * 操作实现 */

	/* int * 操作实现 */
	public static Object mul(Integer a, Byte b) {
		return a * b;
	}

	public static Object mul(Integer a, Short b) {
		return a * b;
	}

	public static Object mul(Integer a, Integer b) {
		return a * b;
	}

	public static Object mul(Integer a, Float b) {
		return a * b;
	}

	public static Object mul(Integer a, Double b) {
		return a * b;
	}

	public static Object mul(Integer a, Long b) {
		return a * b;
	}

	public static Object mul(Integer a, BigDecimal b) {
		return new BigDecimal(a).multiply(b);
	}
	/* int * 操作实现 */

	/* float * 操作实现 */
	public static Object mul(Float a, Byte b) {
		return a * b;
	}

	public static Object mul(Float a, Short b) {
		return a * b;
	}

	public static Object mul(Float a, Integer b) {
		return a * b;
	}

	public static Object mul(Float a, Float b) {
		return a * b;
	}

	public static Object mul(Float a, Double b) {
		return a * b;
	}

	public static Object mul(Float a, Long b) {
		return a * b;
	}

	public static Object mul(Float a, BigDecimal b) {
		return new BigDecimal(a).multiply(new BigDecimal(a));
	}
	/* float * 操作实现 */

	/* double * 操作实现 */
	public static Object mul(Double a, Byte b) {
		return a * b;
	}

	public static Object mul(Double a, Short b) {
		return a * b;
	}

	public static Object mul(Double a, Integer b) {
		return a * b;
	}

	public static Object mul(Double a, Float b) {
		return a * b;
	}

	public static Object mul(Double a, Double b) {
		return a * b;
	}

	public static Object mul(Double a, Long b) {
		return a * b;
	}

	public static Object mul(Double a, BigDecimal b) {
		return new BigDecimal(a).multiply(b);
	}
	/* double * 操作实现 */

	/* long * 操作实现 */
	public static Object mul(Long a, Byte b) {
		return a * b;
	}

	public static Object mul(Long a, Short b) {
		return a * b;
	}

	public static Object mul(Long a, Integer b) {
		return a * b;
	}

	public static Object mul(Long a, Float b) {
		return a * b;
	}

	public static Object mul(Long a, Double b) {
		return a * b;
	}

	public static Object mul(Long a, Long b) {
		return a * b;
	}

	public static Object mul(Long a, BigDecimal b) {
		return new BigDecimal(a).multiply(b);
	}
	/* long * 操作实现 */

	/* bigecimal * 操作实现 */
	public static Object mul(BigDecimal a, Byte b) {
		return a.multiply(new BigDecimal(b));
	}

	public static Object mul(BigDecimal a, Short b) {
		return a.multiply(new BigDecimal(b));
	}

	public static Object mul(BigDecimal a, Integer b) {
		return a.multiply(new BigDecimal(b));
	}

	public static Object mul(BigDecimal a, Float b) {
		return a.multiply(new BigDecimal(b));
	}

	public static Object mul(BigDecimal a, Double b) {
		return a.multiply(new BigDecimal(b));
	}

	public static Object mul(BigDecimal a, Long b) {
		return a.multiply(new BigDecimal(b));
	}

	public static Object mul(BigDecimal a, BigDecimal b) {
		return a.multiply(b);
	}
	/* bigdecimal * 操作实现 */


	public static Object mul_fallback(Object a, Object b) {
		return reject(a, b, "*");
	}

	/* byte / 操作实现 */
	public static Object divide(Byte a, Byte b) {
		return a / b;
	}

	public static Object divide(Byte a, Short b) {
		return a / b;
	}

	public static Object divide(Byte a, Integer b) {
		return a / b;
	}

	public static Object divide(Byte a, Float b) {
		return a / b;
	}

	public static Object divide(Byte a, Double b) {
		return a / b;
	}

	public static Object divide(Byte a, Long b) {
		return a / b;
	}

	public static Object divide(Byte a, BigDecimal b) {
		return new BigDecimal(a).divide(b);
	}
	/* byte / 操作实现 */

	/* short / 操作实现 */
	public static Object divide(Short a, Byte b) {
		return a / b;
	}

	public static Object divide(Short a, Short b) {
		return a / b;
	}

	public static Object divide(Short a, Integer b) {
		return a / b;
	}

	public static Object divide(Short a, Float b) {
		return a / b;
	}

	public static Object divide(Short a, Double b) {
		return a / b;
	}

	public static Object divide(Short a, Long b) {
		return a / b;
	}

	public static Object divide(Short a, BigDecimal b) {
		return new BigDecimal(a).divide(b);
	}
	/* short / 操作实现 */

	/* int / 操作实现 */
	public static Object divide(Integer a, Byte b) {
		return a / b;
	}

	public static Object divide(Integer a, Short b) {
		return a / b;
	}

	public static Object divide(Integer a, Integer b) {
		return a / b;
	}

	public static Object divide(Integer a, Float b) {
		return a / b;
	}

	public static Object divide(Integer a, Double b) {
		return a / b;
	}

	public static Object divide(Integer a, Long b) {
		return a / b;
	}

	public static Object divide(Integer a, BigDecimal b) {
		return new BigDecimal(a).divide(b);
	}
	/* int / 操作实现 */

	/* float / 操作实现 */
	public static Object divide(Float a, Byte b) {
		return a / b;
	}

	public static Object divide(Float a, Short b) {
		return a / b;
	}

	public static Object divide(Float a, Integer b) {
		return a / b;
	}

	public static Object divide(Float a, Float b) {
		return a / b;
	}

	public static Object divide(Float a, Double b) {
		return a / b;
	}

	public static Object divide(Float a, Long b) {
		return a / b;
	}

	public static Object divide(Float a, BigDecimal b) {
		return new BigDecimal(a).divide(new BigDecimal(a));
	}
	/* float / 操作实现 */

	/* double / 操作实现 */
	public static Object divide(Double a, Byte b) {
		return a / b;
	}

	public static Object divide(Double a, Short b) {
		return a / b;
	}

	public static Object divide(Double a, Integer b) {
		return a / b;
	}

	public static Object divide(Double a, Float b) {
		return a / b;
	}

	public static Object divide(Double a, Double b) {
		return a / b;
	}

	public static Object divide(Double a, Long b) {
		return a / b;
	}

	public static Object divide(Double a, BigDecimal b) {
		return new BigDecimal(a).divide(b);
	}
	/* double / 操作实现 */

	/* long / 操作实现 */
	public static Object divide(Long a, Byte b) {
		return a / b;
	}

	public static Object divide(Long a, Short b) {
		return a / b;
	}

	public static Object divide(Long a, Integer b) {
		return a / b;
	}

	public static Object divide(Long a, Float b) {
		return a / b;
	}

	public static Object divide(Long a, Double b) {
		return a / b;
	}

	public static Object divide(Long a, Long b) {
		return a / b;
	}

	public static Object divide(Long a, BigDecimal b) {
		return new BigDecimal(a).divide(b);
	}
	/* long / 操作实现 */

	/* bigecimal / 操作实现 */
	public static Object divide(BigDecimal a, Byte b) {
		return a.divide(new BigDecimal(b));
	}

	public static Object divide(BigDecimal a, Short b) {
		return a.divide(new BigDecimal(b));
	}

	public static Object divide(BigDecimal a, Integer b) {
		return a.divide(new BigDecimal(b));
	}

	public static Object divide(BigDecimal a, Float b) {
		return a.divide(new BigDecimal(b));
	}

	public static Object divide(BigDecimal a, Double b) {
		return a.divide(new BigDecimal(b));
	}

	public static Object divide(BigDecimal a, Long b) {
		return a.divide(new BigDecimal(b));
	}

	public static Object divide(BigDecimal a, BigDecimal b) {
		return a.divide(b);
	}
	/* bigdecimal / 操作实现 */


	public static Object divide_fallback(Object a, Object b) {
		return reject(a, b, "/");
	}


	/* byte % 操作实现 */
	public static Object divideAndRemainder(Byte a, Byte b) {
		return a % b;
	}

	public static Object divideAndRemainder(Byte a, Short b) {
		return a % b;
	}

	public static Object divideAndRemainder(Byte a, Integer b) {
		return a % b;
	}

	public static Object divideAndRemainder(Byte a, Float b) {
		return a % b;
	}

	public static Object divideAndRemainder(Byte a, Double b) {
		return a % b;
	}

	public static Object divideAndRemainder(Byte a, Long b) {
		return a % b;
	}

	public static Object divideAndRemainder(Byte a, BigDecimal b) {
		return new BigDecimal(a).divideAndRemainder(b);
	}
	/* byte % 操作实现 */

	/* short % 操作实现 */
	public static Object divideAndRemainder(Short a, Byte b) {
		return a % b;
	}

	public static Object divideAndRemainder(Short a, Short b) {
		return a % b;
	}

	public static Object divideAndRemainder(Short a, Integer b) {
		return a % b;
	}

	public static Object divideAndRemainder(Short a, Float b) {
		return a % b;
	}

	public static Object divideAndRemainder(Short a, Double b) {
		return a % b;
	}

	public static Object divideAndRemainder(Short a, Long b) {
		return a % b;
	}

	public static Object divideAndRemainder(Short a, BigDecimal b) {
		return new BigDecimal(a).divideAndRemainder(b);
	}
	/* short % 操作实现 */

	/* int % 操作实现 */
	public static Object divideAndRemainder(Integer a, Byte b) {
		return a % b;
	}

	public static Object divideAndRemainder(Integer a, Short b) {
		return a % b;
	}

	public static Object divideAndRemainder(Integer a, Integer b) {
		return a % b;
	}

	public static Object divideAndRemainder(Integer a, Float b) {
		return a % b;
	}

	public static Object divideAndRemainder(Integer a, Double b) {
		return a % b;
	}

	public static Object divideAndRemainder(Integer a, Long b) {
		return a % b;
	}

	public static Object divideAndRemainder(Integer a, BigDecimal b) {
		return new BigDecimal(a).divideAndRemainder(b);
	}
	/* int % 操作实现 */

	/* float % 操作实现 */
	public static Object divideAndRemainder(Float a, Byte b) {
		return a % b;
	}

	public static Object divideAndRemainder(Float a, Short b) {
		return a % b;
	}

	public static Object divideAndRemainder(Float a, Integer b) {
		return a % b;
	}

	public static Object divideAndRemainder(Float a, Float b) {
		return a % b;
	}

	public static Object divideAndRemainder(Float a, Double b) {
		return a % b;
	}

	public static Object divideAndRemainder(Float a, Long b) {
		return a % b;
	}

	public static Object divideAndRemainder(Float a, BigDecimal b) {
		return new BigDecimal(a).divideAndRemainder(new BigDecimal(a));
	}
	/* float % 操作实现 */

	/* double % 操作实现 */
	public static Object divideAndRemainder(Double a, Byte b) {
		return a % b;
	}

	public static Object divideAndRemainder(Double a, Short b) {
		return a % b;
	}

	public static Object divideAndRemainder(Double a, Integer b) {
		return a % b;
	}

	public static Object divideAndRemainder(Double a, Float b) {
		return a % b;
	}

	public static Object divideAndRemainder(Double a, Double b) {
		return a % b;
	}

	public static Object divideAndRemainder(Double a, Long b) {
		return a % b;
	}

	public static Object divideAndRemainder(Double a, BigDecimal b) {
		return new BigDecimal(a).divideAndRemainder(b);
	}
	/* double % 操作实现 */

	/* long % 操作实现 */
	public static Object divideAndRemainder(Long a, Byte b) {
		return a % b;
	}

	public static Object divideAndRemainder(Long a, Short b) {
		return a % b;
	}

	public static Object divideAndRemainder(Long a, Integer b) {
		return a % b;
	}

	public static Object divideAndRemainder(Long a, Float b) {
		return a % b;
	}

	public static Object divideAndRemainder(Long a, Double b) {
		return a % b;
	}

	public static Object divideAndRemainder(Long a, Long b) {
		return a % b;
	}

	public static Object divideAndRemainder(Long a, BigDecimal b) {
		return new BigDecimal(a).divideAndRemainder(b);
	}
	/* long % 操作实现 */

	/* bigecimal % 操作实现 */
	public static Object divideAndRemainder(BigDecimal a, Byte b) {
		return a.divideAndRemainder(new BigDecimal(b));
	}

	public static Object divideAndRemainder(BigDecimal a, Short b) {
		return a.divideAndRemainder(new BigDecimal(b));
	}

	public static Object divideAndRemainder(BigDecimal a, Integer b) {
		return a.divideAndRemainder(new BigDecimal(b));
	}

	public static Object divideAndRemainder(BigDecimal a, Float b) {
		return a.divideAndRemainder(new BigDecimal(b));
	}

	public static Object divideAndRemainder(BigDecimal a, Double b) {
		return a.divideAndRemainder(new BigDecimal(b));
	}

	public static Object divideAndRemainder(BigDecimal a, Long b) {
		return a.divideAndRemainder(new BigDecimal(b));
	}

	public static Object divideAndRemainder(BigDecimal a, BigDecimal b) {
		return a.divideAndRemainder(b);
	}
	/* bigdecimal % 操作实现 */


	public static Object divideAndRemainder_fallback(Object a, Object b) {
		return reject(a, b, "%");
	}

	private static Object reject(Object a, Object b, String symbol) throws IllegalArgumentException {
		throw new IllegalArgumentException(String.format("`%s` 运算不支持 (%s,%s) 类型", symbol, a.getClass().getName(), b.getClass().getName()));
	}
}
