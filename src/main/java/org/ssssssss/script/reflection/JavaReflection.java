package org.ssssssss.script.reflection;

import org.ssssssss.script.annotation.UnableCall;
import org.ssssssss.script.convert.ClassImplicitConvert;
import org.ssssssss.script.convert.CollectionImplicitConvert;
import org.ssssssss.script.convert.MapImplicitConvert;
import org.ssssssss.script.functions.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;


public class JavaReflection extends AbstractReflection {
	private final Map<Class<?>, Map<String, Field>> fieldCache = new ConcurrentHashMap<Class<?>, Map<String, Field>>();
	private static SortedSet<ClassImplicitConvert> converts;
	private final Map<Class<?>, Map<String, List<Method>>> extensionmethodCache = new ConcurrentHashMap<>();
	private final Map<Class<?>, Map<MethodSignature, JavaInvoker<Method>>> methodCache = new ConcurrentHashMap<>();
	private static Map<Class<?>, List<Class<?>>> extensionMap;

	public JavaReflection() {
		registerExtensionClass(Class.class, ClassExtension.class);
		registerExtensionClass(Collection.class, StreamExtension.class);
		registerExtensionClass(Object[].class, StreamExtension.class);
		registerExtensionClass(Enumeration.class, StreamExtension.class);
		registerExtensionClass(Iterator.class, StreamExtension.class);
		registerExtensionClass(Object.class, ObjectConvertExtension.class);
		registerExtensionClass(Object.class, ObjectTypeConditionExtension.class);
		registerExtensionClass(Map.class, MapExtension.class);
		registerExtensionClass(Date.class, DateExtension.class);
		registerExtensionClass(Number.class, NumberExtension.class);

		converts = new TreeSet<>(Comparator.comparingInt(ClassImplicitConvert::sort));

		registerImplicitConvert(new MapImplicitConvert());
		registerImplicitConvert(new CollectionImplicitConvert());
	}

	public static Map<Class<?>, List<Class<?>>> getExtensionMap() {
		return extensionMap;
	}

	/**
	 * Returns the <code>apply()</code> method of a functional interface.
	 **/
	private static MethodInvoker findApply(Class<?> cls) {
		for (Method method : cls.getDeclaredMethods()) {
			if ("apply".equals(method.getName())) {
				return new MethodInvoker(method);
			}
		}
		return null;
	}

	private static int calcToObjectDistanceWithInterface(Class<?>[] interfaces, int distance, int score) {
		if (interfaces == null) {
			return distance;
		}
		return Arrays.stream(interfaces).mapToInt(i -> {
			int v = calcToObjectDistanceWithInterface(i.getInterfaces(), distance, score + 2);
			return v + distance + score;
		}).sum();
	}

	private static int calcToObjectDistance(Class<?> clazz) {
		return calcToObjectDistance(clazz, 0);
	}

	private static int calcToObjectDistance(Class<?> clazz, int distance) {
		if (clazz == null) {
			return distance + 3;
		}
		if (Object.class.equals(clazz)) {
			return distance;
		}
		int interfaceScore = calcToObjectDistanceWithInterface(clazz.getInterfaces(), distance + 2, 0);
		if (clazz.isInterface()) {
			return interfaceScore;
		}
		int classScore = calcToObjectDistance(clazz.getSuperclass(), distance + 3);
		return classScore + interfaceScore;
	}

	private static int matchTypes(JavaInvoker<?> invoker, Class<?>[] parameterTypes, Class<?>[] otherTypes, boolean matchCount) {
		if (matchCount && parameterTypes.length != otherTypes.length) {
			return -1;
		}
		int score = 0;
		for (int ii = 0, nn = parameterTypes.length; ii < nn; ii++) {
			Class<?> type = parameterTypes[ii];
			Class<?> otherType = otherTypes[ii];
			if (Null.class.equals(type)) {
				score += 1000;
			} else if (!otherType.isAssignableFrom(type)) {
				score += 1000;
				if (!isPrimitiveAssignableFrom(type, otherType)) {
					score += 1000;
					if (!isCoercible(type, otherType)) {
						score += 1000;
						boolean found = false;
						for (ClassImplicitConvert convert : converts) {
							if (convert.support(type, otherType)) {
								invoker.addClassImplicitConvert(ii, convert);
								found = true;
								break;
							}
						}
						if (!found) {
							return -1;
						}
					}
				}
			}
		}
		return score;
	}

	private static boolean isImplicitConvert(Class<?> from, Class<?> to) {
		if (isPrimitiveAssignableFrom(from, from) || isPrimitiveAssignableFrom(to, to)) {
			return false;
		} else if (Collection.class.isAssignableFrom(to) || Iterator.class.isAssignableFrom(to) || Enumeration.class.isAssignableFrom(to) || to.isArray()) {
			Class<?> toClazz = getGenericType(to);
			return toClazz != null && (!isPrimitiveAssignableFrom(toClazz, toClazz));
		}
		return Map.class.isAssignableFrom(from);
	}

	private static Class<?> getGenericType(Class<?> target) {
		Type type = target.getGenericSuperclass();
		if (type instanceof ParameterizedType) {
			return (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
		}
		return null;
	}

	public static JavaInvoker<Method> findMethodInvoker(List<Method> methods, Class<?>[] parameterTypes) {
		return findInvoker(methods.stream().map(MethodInvoker::new).collect(Collectors.toList()), parameterTypes);
	}

	public static JavaInvoker<Constructor> findConstructorInvoker(List<Constructor<?>> constructors, Class<?>[] parameterTypes) {
		return findInvoker(constructors.stream().map(ConstructorInvoker::new).collect(Collectors.toList()), parameterTypes);
	}

	public static <T extends Executable> JavaInvoker<T> findInvoker(List<JavaInvoker<T>> executables, Class<?>[] parameterTypes) {
		JavaInvoker<T> foundInvoker = null;
		int foundScore = 0;
		List<JavaInvoker<T>> executableWithVarArgs = new ArrayList<>();
		for (JavaInvoker<T> invoker : executables) {
			// Check if the types match.
			Class<?>[] otherTypes = invoker.getParameterTypes();
			int score = matchTypes(invoker, parameterTypes, otherTypes, true);
			if (score > -1) {
				if (foundInvoker == null) {
					foundInvoker = invoker;
					foundScore = score;
				} else {
					if (score < foundScore) {
						foundScore = score;
						foundInvoker = invoker;
					}
				}
			} else if (invoker.isVarArgs()) {
				executableWithVarArgs.add(invoker);
			}
		}
		if (foundInvoker == null) {
			for (JavaInvoker<T> invoker : executableWithVarArgs) {
				Class<?>[] otherTypes = invoker.getParameterTypes();
				int score = -1;
				int fixedParaLength = otherTypes.length - 1;
				if (parameterTypes.length >= fixedParaLength) {
					Class<?>[] argTypes = new Class<?>[fixedParaLength];
					System.arraycopy(parameterTypes, 0, argTypes, 0, fixedParaLength);
					score = matchTypes(invoker, argTypes, otherTypes, false);
					if (score > -1) {
						Class<?> target = otherTypes[fixedParaLength].getComponentType();
						for (int i = fixedParaLength; i < parameterTypes.length; i++) {
							Class<?> type = parameterTypes[i];
							if (Null.class.equals(type)) {
								score++;
							} else if (!target.isAssignableFrom(type)) {
								score++;
								if (!isPrimitiveAssignableFrom(type, target)) {
									score++;
									if (!isCoercible(type, target)) {
										boolean found = false;
										for (ClassImplicitConvert convert : converts) {
											if (convert.support(type, target)) {
												found = true;
											}
										}
										if (!found) {
											score = -1;
											break;
										}
										score++;
									} else {
										score++;
									}
								}
							}
						}
					}
				}
				if (score > -1) {
					if (foundInvoker == null) {
						foundInvoker = invoker;
						foundScore = score;
					} else {
						if (score < foundScore) {
							foundScore = score;
							foundInvoker = invoker;
						}
					}
				}
			}
		}
		return foundInvoker;
	}

	/**
	 * Returns the method best matching the given signature, including type coercion, or null.
	 **/
	private static JavaInvoker<Method> findInvoker(Class<?> cls, String name, Class<?>[] parameterTypes) {
		List<Method> methodList = new ArrayList<>();
		Method[] methods = cls.getDeclaredMethods();
		for (int i = 0, n = methods.length; i < n; i++) {
			Method method = methods[i];
			if (!method.getName().equals(name)) {
				continue;
			}
			if (method.getAnnotation(UnableCall.class) != null) {
				continue;
			}
			methodList.add(method);
		}
		return findMethodInvoker(methodList, parameterTypes);
	}

	/**
	 * Returns whether the from type can be assigned to the to type, assuming either type is a (boxed) primitive type. We can
	 * relax the type constraint a little, as we'll invoke a method via reflection. That means the from type will always be boxed,
	 * as the {@link Method#invoke(Object, Object...)} method takes objects.
	 **/
	public static boolean isPrimitiveAssignableFrom(Class<?> from, Class<?> to) {
		if ((from == Boolean.class || from == boolean.class) && (to == boolean.class || to == Boolean.class)) {
			return true;
		}
		if ((from == Integer.class || from == int.class) && (to == int.class || to == Integer.class)) {
			return true;
		}
		if ((from == Float.class || from == float.class) && (to == float.class || to == Float.class)) {
			return true;
		}
		if ((from == Double.class || from == double.class) && (to == double.class || to == Double.class)) {
			return true;
		}
		if ((from == Byte.class || from == byte.class) && (to == byte.class || to == Byte.class)) {
			return true;
		}
		if ((from == Short.class || from == short.class) && (to == short.class || to == Short.class)) {
			return true;
		}
		if ((from == Long.class || from == long.class) && (to == long.class || to == Long.class)) {
			return true;
		}
		if ((from == Character.class || from == char.class) && (to == char.class || to == Character.class)) {
			return true;
		}
		return false;
	}

	public static String[] getStringTypes(Object[] objects) {
		String[] parameterTypes = new String[objects == null ? 0 : objects.length];
		if (objects != null) {
			for (int i = 0, len = objects.length; i < len; i++) {
				Object value = objects[i];
				parameterTypes[i] = value == null ? "null" : value.getClass().getSimpleName();
			}
		}
		return parameterTypes;
	}

	/**
	 * Returns whether the from type can be coerced to the to type. The coercion rules follow those of Java. See JLS 5.1.2
	 * https://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html
	 **/
	private static boolean isCoercible(Class<?> from, Class<?> to) {
		if (from == Integer.class || from == int.class) {
			return to == float.class || to == Float.class || to == double.class || to == Double.class || to == long.class || to == Long.class;
		}

		if (from == Float.class || from == float.class) {
			return to == double.class || to == Double.class;
		}

		if (from == Double.class || from == double.class) {
			return false;
		}

		if (from == Character.class || from == char.class) {
			return to == int.class || to == Integer.class || to == float.class || to == Float.class || to == double.class || to == Double.class || to == long.class
					|| to == Long.class;
		}

		if (from == Byte.class || from == byte.class) {
			return to == int.class || to == Integer.class || to == float.class || to == Float.class || to == double.class || to == Double.class || to == long.class
					|| to == Long.class || to == short.class || to == Short.class;
		}

		if (from == Short.class || from == short.class) {
			return to == int.class || to == Integer.class || to == float.class || to == Float.class || to == double.class || to == Double.class || to == long.class
					|| to == Long.class;
		}

		if (from == Long.class || from == long.class) {
			return to == float.class || to == Float.class || to == double.class || to == Double.class;
		}

		if (from == int[].class || from == Integer[].class) {
			return to == Object[].class || to == float[].class || to == Float[].class || to == double[].class || to == Double[].class || to == long[].class || to == Long[].class;
		}

		return false;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Field getField(Object obj, String name) {
		Class cls = obj instanceof Class ? (Class) obj : obj.getClass();
		Map<String, Field> fields = fieldCache.get(cls);
		if (fields == null) {
			fields = new ConcurrentHashMap<>();
			fieldCache.put(cls, fields);
		}

		Field field = fields.get(name);
		if (field == null) {
			try {
				field = cls.getDeclaredField(name);
				if (field.getAnnotation(UnableCall.class) != null) {
					field = null;
				} else {
					field.setAccessible(true);
					fields.put(name, field);
				}
			} catch (Throwable t) {
				// fall through, try super classes
			}

			if (field == null) {
				Class parentClass = cls.getSuperclass();
				while (parentClass != Object.class && parentClass != null) {
					try {
						field = parentClass.getDeclaredField(name);
						if (field.getAnnotation(UnableCall.class) != null) {
							field = null;
						} else {
							field.setAccessible(true);
							fields.put(name, field);
						}
					} catch (NoSuchFieldException e) {
						// fall through
					}
					parentClass = parentClass.getSuperclass();
				}
			}
		}

		return field;
	}

	@Override
	public void registerImplicitConvert(ClassImplicitConvert classImplicitConvert) {
		converts.add(classImplicitConvert);
	}

	@Override
	public void registerExtensionClass(Class<?> target, Class<?> clazz) {
		if (extensionMap == null) {
			extensionMap = new ConcurrentHashMap<>();
		}
		List<Class<?>> classList = extensionMap.get(target);
		if (classList == null) {
			classList = new ArrayList<>();
			extensionMap.put(target, classList);
		}
		classList.add(clazz);
		Method[] methods = clazz.getDeclaredMethods();
		if (methods != null) {
			Map<String, List<Method>> cachedMethodMap = extensionmethodCache.get(target);
			if (cachedMethodMap == null) {
				cachedMethodMap = new HashMap<>();
				extensionmethodCache.put(target, cachedMethodMap);
			}
			for (Method method : methods) {
				if (Modifier.isStatic(method.getModifiers()) && method.getParameterCount() > 0 && method.getAnnotation(UnableCall.class) == null) {
					List<Method> cachedList = cachedMethodMap.get(method.getName());
					if (cachedList == null) {
						cachedList = new ArrayList<>();
						cachedMethodMap.put(method.getName(), cachedList);
					}
					cachedList.add(method);
				}
			}
			Collection<List<Method>> methodsValues = cachedMethodMap.values();
			for (List<Method> methodList : methodsValues) {
				methodList.sort((m1, m2) -> {
					int sum1 = Arrays.stream(m1.getParameterTypes()).mapToInt(JavaReflection::calcToObjectDistance).sum();
					int sum2 = Arrays.stream(m2.getParameterTypes()).mapToInt(JavaReflection::calcToObjectDistance).sum();
					return sum2 - sum1;
				});
			}
		}
	}

	@Override
	public Object getFieldValue(Object obj, Field field) {
		try {
			return field.get(obj);
		} catch (Throwable e) {
			throw new RuntimeException("Couldn't get value of field '" + field.getName() + "' from object of type '" + obj.getClass().getSimpleName() + "'");
		}
	}

	@Override
	public void setFieldValue(Object obj, Field field, Object value) {
		try {
			field.set(obj, value);
		} catch (Throwable e) {
			throw new RuntimeException("Couldn't set value of field '" + field.getName() + "' from object of type '" + obj.getClass().getSimpleName() + "'");
		}
	}

	@Override
	public JavaInvoker<Method> getExtensionMethod(Object obj, String name, Object... arguments) {
		Class<?> cls = obj instanceof Class ? Class.class : obj.getClass();
		if (cls.isArray()) {
			cls = Object[].class;
		}
		return getExtensionMethod(cls, name, arguments);
	}

	private JavaInvoker<Method> getExtensionMethod(Class<?> cls, String name, Object... arguments) {
		if (cls == null) {
			cls = Object.class;
		}
		Map<String, List<Method>> methodMap = extensionmethodCache.get(cls);
		if (methodMap != null) {
			List<Method> methodList = methodMap.get(name);
			if (methodList != null) {
				Class<?>[] parameterTypes = new Class[arguments.length + 1];
				parameterTypes[0] = cls;
				for (int i = 0; i < arguments.length; i++) {
					parameterTypes[i + 1] = arguments[i] == null ? Null.class : arguments[i].getClass();
				}
				return findMethodInvoker(methodList, parameterTypes);
			}
		}
		if (cls != Object.class) {
			Class<?>[] interfaces = cls.getInterfaces();
			if (interfaces != null) {
				for (Class<?> clazz : interfaces) {
					JavaInvoker<Method> invoker = getExtensionMethod(clazz, name, arguments);
					if (invoker != null) {
						return invoker;
					}
				}
			}
			return getExtensionMethod(cls.getSuperclass(), name, arguments);
		}
		return null;
	}

	@Override
	public JavaInvoker<Method> getMethod(Object obj, String name, Object... arguments) {
		Class<?> cls = obj instanceof Class ? (Class<?>) obj : (obj instanceof Function ? Function.class : obj.getClass());
		Map<MethodSignature, JavaInvoker<Method>> methods = methodCache.get(cls);
		if (methods == null) {
			methods = new ConcurrentHashMap<>();
			methodCache.put(cls, methods);
		}

		Class<?>[] parameterTypes = new Class[arguments.length];
		for (int i = 0; i < arguments.length; i++) {
			parameterTypes[i] = arguments[i] == null ? Null.class : arguments[i].getClass();
		}

		JavaReflection.MethodSignature signature = new MethodSignature(name, parameterTypes);
		JavaInvoker<Method> invoker = methods.get(signature);

		if (invoker == null) {
			try {
				if (name == null) {
					invoker = findApply(cls);
				} else {
					invoker = findInvoker(cls, name, parameterTypes);
					if (invoker == null) {
						invoker = findInvoker(cls, name, new Class<?>[]{Object[].class});
					}
				}
				methods.put(signature, invoker);
			} catch (Throwable e) {
				// fall through
			}

			if (invoker == null) {
				Class<?> parentClass = cls.getSuperclass();
				while (parentClass != Object.class && parentClass != null) {
					try {
						if (name == null) {
							invoker = findApply(parentClass);
						} else {
							invoker = findInvoker(parentClass, name, parameterTypes);
						}
						methods.put(signature, invoker);
						if (invoker != null) {
							break;
						}
					} catch (Throwable e) {
						// fall through
					}
					parentClass = parentClass.getSuperclass();
				}
			}
		}

		return invoker;
	}

	public static final class Null {

	}

	private static class MethodSignature {
		private final String name;
		@SuppressWarnings("rawtypes")
		private final Class[] parameters;
		private final int hashCode;

		@SuppressWarnings("rawtypes")
		public MethodSignature(String name, Class[] parameters) {
			this.name = name;
			this.parameters = parameters;
			final int prime = 31;
			int hash = 1;
			hash = prime * hash + ((name == null) ? 0 : name.hashCode());
			hash = prime * hash + Arrays.hashCode(parameters);
			hashCode = hash;
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			JavaReflection.MethodSignature other = (JavaReflection.MethodSignature) obj;
			if (name == null) {
				if (other.name != null) {
					return false;
				}
			} else if (!name.equals(other.name)) {
				return false;
			}
			if (!Arrays.equals(parameters, other.parameters)) {
				return false;
			}
			return true;
		}
	}
}
