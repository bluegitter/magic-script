package org.ssssssss.script;

import org.ssssssss.script.ScriptClass.ScriptAttribute;
import org.ssssssss.script.ScriptClass.ScriptMethod;
import org.ssssssss.script.annotation.UnableCall;
import org.ssssssss.script.exception.DebugTimeoutException;
import org.ssssssss.script.interpreter.JavaReflection;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MagicScriptEngine {

	private static Map<String, Object> defaultImports = new ConcurrentHashMap<>();

	private static Map<String, ScriptClass> classMap = null;


	public static void addScriptClass(Class clazz) {
		if (classMap == null) {
			getScriptClassMap();
		}
		getScriptClass(clazz).forEach(scriptClass -> classMap.put(scriptClass.getClassName(), scriptClass));
	}

	public synchronized static Map<String, ScriptClass> getScriptClassMap() {
		if (classMap == null) {
			classMap = new HashMap<>();
			Arrays.asList(String.class, Object.class, Date.class, Integer.class, Double.class, Float.class, Long.class, List.class, Short.class, Byte.class, Boolean.class, BigDecimal.class).forEach(clazz -> {
				getScriptClass(clazz).forEach(scriptClass -> classMap.put(scriptClass.getClassName(), scriptClass));
			});
		}
		return classMap;
	}

	public static Map<String, ScriptClass> getExtensionScriptClass() {
		Map<Class<?>, List<Class<?>>> extensionMap = JavaReflection.getExtensionMap();
		Map<String, ScriptClass> classMap = new HashMap<>();
		for (Map.Entry<Class<?>, List<Class<?>>> entry : extensionMap.entrySet()) {
			ScriptClass clazz = classMap.get(entry.getKey().getName());
			if (clazz == null) {
				clazz = new ScriptClass();
				classMap.put(entry.getKey().getName(), clazz);
			}
			for (Class<?> extensionClass : entry.getValue()) {
				for (ScriptMethod method : getMethod(extensionClass, true)) {
					clazz.addMethod(method);
				}
			}
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
			Class[] interfaces = clazz.getInterfaces();
			if(interfaces != null){
				List<String> interfaceList = new ArrayList<>();
				for (Class interfaceClazz : interfaces) {
					classList.addAll(getScriptClass(interfaceClazz));
					interfaceList.add(interfaceClazz.getName());
				}
				scriptClass.setInterfaces(interfaceList);
			}
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
			if (!Modifier.isVolatile(declaredMethod.getModifiers())) {
				if (Modifier.isPublic(declaredMethod.getModifiers()) && declaredMethod.getAnnotation(UnableCall.class) == null) {
					boolean isStatic = Modifier.isStatic(declaredMethod.getModifiers());
					if ((!publicAndStatic) || isStatic) {
						methods.add(new ScriptMethod(declaredMethod));
					}
				}
			}
		}
		return methods;
	}

	public static void addDefaultImport(String name, Object target) {
		defaultImports.put(name, target);
	}

	public static Object execute(MagicScript magicScript, MagicScriptContext context) {
		for (Map.Entry<String, Object> entry : defaultImports.entrySet()) {
			context.set(entry.getKey(), entry.getValue());
		}
		if (context instanceof MagicScriptDebugContext) {
			MagicScriptDebugContext debugContext = (MagicScriptDebugContext) context;
			new Thread(() -> {
				try {
					debugContext.start();
					debugContext.setReturnValue(magicScript.execute(debugContext));
				} catch (Exception e) {
					debugContext.setException(true);
					debugContext.setReturnValue(e);
				}
			}, "magic-script").start();
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
