package org.ssssssss.script.runtime.handle;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.exception.MagicScriptRuntimeException;
import org.ssssssss.script.functions.ClassExtension;
import org.ssssssss.script.parsing.ast.statement.AsyncCall;
import org.ssssssss.script.parsing.ast.statement.ClassConverter;
import org.ssssssss.script.parsing.ast.statement.Spread;
import org.ssssssss.script.reflection.JavaInvoker;
import org.ssssssss.script.reflection.JavaReflection;
import org.ssssssss.script.runtime.SpreadValue;
import org.ssssssss.script.runtime.function.MagicScriptLambdaFunction;
import org.ssssssss.script.runtime.function.MagicScriptLanguageFunction;
import org.ssssssss.script.runtime.lang.ArrayKeyValueIterator;
import org.ssssssss.script.runtime.lang.ArrayValueIterator;
import org.ssssssss.script.runtime.lang.KeyValueIterator;
import org.ssssssss.script.runtime.lang.MapKeyValueIterator;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.invoke.MethodType.methodType;

public class FunctionCallHandle {

	private static final MethodHandle FALLBACK;

	private static final MethodHandle INVOKE_METHOD;

	private static final Field ARRAYLIST_FIELD_ELEMENT_DATA = JavaReflection.getField(ArrayList.class, "elementData");

	private static final Field ARRAYLIST_FIELD_SIZE = JavaReflection.getField(ArrayList.class, "size");

	private static final Object[] EMPTY_ARGS = new Object[0];

	static {
		try {
			MethodHandles.Lookup lookup = MethodHandles.lookup();
			FALLBACK = lookup.findStatic(FunctionCallHandle.class, "fallback", methodType(Object.class, MethodCallSite.class, Object[].class));
			INVOKE_METHOD = lookup.findStatic(FunctionCallHandle.class, "invoke_method", methodType(Object.class, Object.class, MagicScriptContext.class, String.class, boolean.class, boolean.class, Object[].class));
		} catch (NoSuchMethodException | IllegalAccessException e) {
			throw new Error("FunctionCallHandle初始化失败", e);
		}
	}

	public static CallSite bootstrap(MethodHandles.Lookup caller, String name, MethodType type, int flag) {
		MethodCallSite callSite = new MethodCallSite(caller, name, type, FunctionCallHandle.class);
		if ("invoke_method".equals(name)) {
			MethodHandle fallback = INVOKE_METHOD
					.asVarargsCollector(Object[].class)
					.asType(type);
			callSite.setTarget(fallback);
			callSite.fallback = fallback;
		} else {
			MethodHandle fallback = FALLBACK
					.bindTo(callSite)
					.asCollector(Object[].class, type.parameterCount())
					.asType(type);
			callSite.setTarget(fallback);
			callSite.fallback = fallback;
		}
		return callSite;
	}

	public static Object fallback(MethodCallSite callSite, Object[] args) throws Throwable {
		JavaInvoker<Method> method = JavaReflection.getMethod(FunctionCallHandle.class, callSite.methodName, args);
		if (method != null) {
			return method.invoke0(null, null, args);
		}
		return null;
	}

	public static Object invoke_method(Object target, MagicScriptContext context, String name, boolean hasSpread, boolean optional, Object[] args) throws Throwable {
		if (target == null && optional) {
			return null;
		}
		JavaInvoker<Method> method;
		if (hasSpread) {
			args = do_spread(args);
		}
		if (target == null) {
			method = JavaReflection.getFunction(name, args);
		} else if (target instanceof MagicScriptLambdaFunction) {
			Object value = ((MagicScriptLambdaFunction) target).apply(context, args);
			if (!(target instanceof MagicScriptLanguageFunction)) {
				context.restore();
			}
			return value;
		} else {
			method = JavaReflection.getMethod(target, name, args);
		}
		if (method != null) {
			return method.invoke0(target, context, args);
		}
		throw new NoSuchMethodException(String.format("在'%s'中找不到方法%s(%s)", target, name, String.join(",", JavaReflection.getStringTypes(args))));
	}

	public static Object spread(List<Object> source, List<Object> target) {
		source.addAll(target);
		return source;
	}

	public static Object spread(Map<Object, Object> source, Map<Object, Object> target) {
		source.putAll(target);
		return source;
	}

	public static Object member_access(Object target, String name, boolean optional) {
		return member_access(target, name, optional, false);
	}

	public static Object member_access(Object target, String name, boolean optional, boolean inLinq) {
		if (target == null) {
			if (optional) {
				return null;
			}
			throw new NullPointerException("target is null");
		}
		if ("class".equals(name)) {
			return target instanceof Class ? target : target;
		} else if (target instanceof Map) {
			return ((Map) target).get(name);
		}
		String methodName;
		Field field = JavaReflection.getField(target, name);
		if (field != null) {
			return JavaReflection.getFieldValue(target, field);
		} else {
			if (name.length() > 1) {
				methodName = name.substring(0, 1).toUpperCase() + name.substring(1);
			} else {
				methodName = name.toUpperCase();
			}
			JavaInvoker<Method> invoker = JavaReflection.getMethod(target, "get" + methodName, EMPTY_ARGS);
			try {
				if (invoker != null) {
					return invoker.invoke0(target, null, EMPTY_ARGS);
				} else if ((invoker = JavaReflection.getMethod(target, "get" + methodName, EMPTY_ARGS)) != null) {
					return invoker.invoke0(target, null, EMPTY_ARGS);
				}
			} catch (Throwable throwable) {
				throw new MagicScriptRuntimeException(throwable);
			}
		}
		Object innerClass = JavaReflection.getInnerClass(target, name);
		if (innerClass != null) {
			return innerClass;
		}
		if (target instanceof List) {
			List list = (List) target;
			if (inLinq) {
				return list.stream().map(it -> member_access(it, name, optional)).collect(Collectors.toList());
			}else if(list.size() > 0){
				return member_access(list.get(0), name, optional,false);
			}
			return null;
		}
		throw new MagicScriptRuntimeException(String.format("在%s中找不到属性%s或者方法get%s、方法is%s,内部类%s", target, name, methodName, methodName, name));
	}

	public static Object call_async(MagicScriptLambdaFunction function, MagicScriptContext context, Object... args) {
		return AsyncCall.execute(function, context, args);
	}

	public static Object member_access(Map map, String name, boolean optional) {
		return map == null && optional ? null : map.get(name);
	}

	public static Object invoke_new_instance(Object target, Object[] args) throws Throwable {
		return ClassExtension.newInstance(target, args);
	}

	public static Object newArrayList(boolean hasSpread, Object[] args) {
		if (!hasSpread) {
			ArrayList<Object> list = new ArrayList<>();
			// 使用反射是因为不需要对数组进行clone操作，为了避免clone，故采用反射绕过。
			if(args != null && args.length > 0){
				JavaReflection.setFieldValue(list, ARRAYLIST_FIELD_ELEMENT_DATA, args);
				JavaReflection.setFieldValue(list, ARRAYLIST_FIELD_SIZE, args.length);
			}
			return list;
		}
		List<Object> list = new ArrayList<>(args.length);
		for (int i = 0, len = args.length; i < len; i++) {
			Object item = args[i];
			if (item instanceof SpreadValue) {
				Object res = ((SpreadValue) item).getValue();
				if (res == null) {
					// 其实是因为该变量未定义
				} else if (res instanceof Collection) {
					list.addAll(((Collection) res));
				} else if (res instanceof Map) {
					throw new MagicScriptRuntimeException("不能在list中展开map");
				} else {
					throw new MagicScriptRuntimeException("不能展开的类型:" + res.getClass());
				}
			} else {
				list.add(item);
			}
		}
		return list;
	}

	public static Iterator<?> newValueIterator(Object target) {
		if (target instanceof Iterable) {
			return ((Iterable<?>) target).iterator();
		} else if (target instanceof Iterator) {
			return (Iterator<?>) target;
		} else if (target instanceof Map) {
			return ((Map) target).values().iterator();
		} else if (target.getClass().isArray()) {
			return new ArrayValueIterator(target);
		} else {
			throw new MagicScriptRuntimeException("不支持循环" + target.getClass());
		}
	}

	public static Iterator<?> newKeyValueIterator(Object target) {
		if (target instanceof Iterable) {
			return new KeyValueIterator(((Iterable) target).iterator());
		} else if (target instanceof Iterator) {
			return new KeyValueIterator((Iterator) target);
		} else if (target instanceof Map) {
			return new MapKeyValueIterator((Map<Object, Object>) target);
		} else if (target.getClass().isArray()) {
			return new ArrayKeyValueIterator(target);
		} else {
			throw new MagicScriptRuntimeException("不支持循环" + target.getClass());
		}
	}

	public static Object newLinkedHashMap(Boolean hasSpread, Object[] args) {
		Map<Object, Object> map = new LinkedHashMap<>();
		if (args != null) {
			for (int i = 0, len = args.length; i < len; ) {
				Object key = args[i++];
				if (hasSpread && key instanceof SpreadValue) {
					Object res = ((SpreadValue) key).getValue();
					if (res == null) {
						// 其实是因为该变量未定义
					} else if (res instanceof Map) {
						// 可能导致 map put 入非String 的 key
						map.putAll((Map<String, ?>) res);
					} else if (res instanceof Collection) {
						int index = 0;
						for (Object obj : ((Collection) res)) {
							map.put(String.valueOf(index++), obj);
						}
					} else {
						throw new MagicScriptRuntimeException("不能展开的类型:" + res.getClass());
					}
					continue;
				}
				map.put(key, args[i++]);
			}
		}
		return map;
	}

	public static Object set_variable_value(Object target, Object name, Object value) throws Throwable {
		if (target == null) {
			throw new NullPointerException("target is null");
		}
		if (name == null) {
			throw new NullPointerException("key is null");
		}
		if (target instanceof Map) {
			((Map) target).put(name, value);
		} else if (target instanceof List) {
			if (name instanceof Number) {
				// TODO NEW EXCEPTION
				((List) target).set(((Number) name).intValue(), value);
			} else {
				throw new MagicScriptRuntimeException("不支持此赋值操作");
			}
		} else {
			String text = name.toString();
			Field field = JavaReflection.getField(target, text);
			if (field != null) {
				JavaReflection.setFieldValue(target, field, value);
			} else {
				String methodName;
				if (text.length() > 1) {
					methodName = text.substring(0, 1).toUpperCase() + text.substring(1);
				} else {
					methodName = text.toUpperCase();
				}
				JavaInvoker<Method> invoker = JavaReflection.getMethod(target, "set" + methodName, value);
				if (invoker == null) {
					throw new MagicScriptRuntimeException(String.format("在%s中找不到属性%s或者方法set%s", target.getClass(), name, methodName));
				}
				invoker.invoke0(target, null, new Object[]{value});
			}
		}
		return value;
	}

	public static Object type_cast(Object object, String target, Object... args) {
		return ClassConverter.process(object, target, args);
	}

	private static Object[] do_spread(Object[] args) {
		Object[] dest = new Object[args.length];
		for (int i = 0, n = args.length, pIndex = 0; i < n; i++) {
			Object item = args[i];
			if (item instanceof SpreadValue) {
				Object value = ((SpreadValue) item).getValue();
				if (value instanceof Collection) {
					Object[] spreadValues = ((Collection<?>) value).toArray();
					int spreadLength = spreadValues.length;
					if (spreadLength > 0) {
						Object[] valTemp = dest;
						dest = new Object[dest.length + spreadLength - 1];
						System.arraycopy(valTemp, 0, dest, 0, valTemp.length);
						System.arraycopy(spreadValues, 0, dest, pIndex, spreadLength);
						pIndex += spreadLength;
					}
				} else {
					throw new MagicScriptRuntimeException("展开的不是一个集合");
				}
			} else {
				dest[pIndex++] = item;
			}
		}
		return dest;
	}

}
