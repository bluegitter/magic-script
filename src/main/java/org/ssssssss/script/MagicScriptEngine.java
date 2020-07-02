package org.ssssssss.script;

import org.ssssssss.script.ScriptClass.ScriptAttribute;
import org.ssssssss.script.ScriptClass.ScriptMethod;
import org.ssssssss.script.annotation.UnableCall;
import org.ssssssss.script.exception.DebugTimeoutException;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;

public class MagicScriptEngine {

	private static Map<String, Object> defaultImports = new ConcurrentHashMap<>();

	private static ExecutorService service = Executors.newCachedThreadPool();

	private static Map<String, ScriptClass> classMap = null;

	static {
		addDefaultImport("range", (BiFunction<Integer, Integer, Iterator<Integer>>) (from, to) -> new Iterator<Integer>() {
			int idx = from;

			public boolean hasNext() {
				return idx <= to;
			}

			public Integer next() {
				return idx++;
			}
		});
	}

	public static void addScriptClass(Class clazz) {
		if (classMap == null) {
			getScriptClassMap();
		}
		getScriptClass(clazz).forEach(scriptClass -> classMap.put(scriptClass.getClassName(), scriptClass));
	}

	public synchronized static Map<String, ScriptClass> getScriptClassMap() {
		if (classMap == null) {
			classMap = new HashMap<>();
			Arrays.asList(String.class, Object.class, Date.class, Integer.class, Double.class, Float.class, Long.class, List.class, Short.class, Byte.class, Boolean.class).forEach(clazz -> {
				getScriptClass(clazz).forEach(scriptClass -> classMap.put(scriptClass.getClassName(), scriptClass));
			});
		}
		return classMap;
	}

	public static ScriptClass getScriptClassFromClass(Class clazz) {
		Class<?> superClass = clazz.getSuperclass();
		ScriptClass scriptClass = new ScriptClass();
		scriptClass.setClassName(clazz.getName());
		scriptClass.setSuperClass(superClass != null ? superClass.getName() : null);
		getMethod(clazz, false).forEach(method -> {
			if (method.getName().startsWith("get") && method.getParameters().size() == 0 && method.getName().length() > 3) {
				String attributeName = method.getName().substring(3);
				attributeName = attributeName.substring(0, 1).toLowerCase() + attributeName.substring(1);
				scriptClass.addAttribute(new ScriptAttribute(method.getReturnType(), attributeName));
			} else {
				scriptClass.addMethod(method);
			}
		});
		return scriptClass;
	}

	public static List<ScriptClass> getScriptClass(Class clazz) {
		List<ScriptClass> classList = new ArrayList<>();
		Class<?> superClass;
		do {
			superClass = clazz.getSuperclass();
			ScriptClass scriptClass = new ScriptClass();
			scriptClass.setClassName(clazz.getName());
			scriptClass.setSuperClass(superClass != null ? superClass.getName() : null);
			getMethod(clazz, false).forEach(method -> {
				if (method.getName().startsWith("get") && method.getParameters().size() == 0 && method.getName().length() > 3) {
					String attributeName = method.getName().substring(3);
					attributeName = attributeName.substring(0, 1).toLowerCase() + attributeName.substring(1);
					scriptClass.addAttribute(new ScriptAttribute(method.getReturnType(), attributeName));
				} else {
					scriptClass.addMethod(method);
				}
			});
			classList.add(scriptClass);
			clazz = superClass;
		} while (superClass != null && superClass != Object.class && superClass != Class.class);
		return classList;
	}

	public static List<ScriptClass> getScriptClass(String className) {
		try {
			return getScriptClass(Class.forName(className));
		} catch (ClassNotFoundException e) {
			return new ArrayList<>();
		}
	}

	private static List<ScriptMethod> getMethod(Class clazz, boolean publicAndStatic) {
		List<ScriptMethod> methods = new ArrayList<>();
		Method[] declaredMethods = clazz.getDeclaredMethods();
		for (int i = 0; i < declaredMethods.length; i++) {
			Method declaredMethod = declaredMethods[i];
			if (Modifier.isPublic(declaredMethod.getModifiers()) && declaredMethod.getAnnotation(UnableCall.class) == null) {
				boolean isStatic = Modifier.isStatic(declaredMethod.getModifiers());
				if ((!publicAndStatic) || isStatic) {
					methods.add(new ScriptMethod(declaredMethod));
				}
			}
		}
		return methods;
	}

	public static void addDefaultImport(String name, Object target) {
		defaultImports.put(name, target);
	}

	public static Object execute(String script, MagicScriptContext context) {
		Iterator<Map.Entry<String, Object>> iterator = defaultImports.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, Object> entry = iterator.next();
			context.set(entry.getKey(), entry.getValue());
		}
		MagicScript magicScript = MagicScript.create(script);
		if (context instanceof MagicScriptDebugContext) {
			MagicScriptDebugContext debugContext = (MagicScriptDebugContext) context;
			service.submit(() -> {
				try {
					debugContext.setReturnValue(magicScript.execute(debugContext));
				} catch (Exception e) {
					debugContext.setException(true);
					debugContext.setReturnValue(e);
				}
			});
			try {
				debugContext.await();
			} catch (InterruptedException e) {
				throw new DebugTimeoutException(e);
			}
			return debugContext.isRunning() ? debugContext.getDebugInfo() : debugContext.getReturnValue();
		}
		return magicScript.execute(context);
	}
}
