package org.ssssssss.script;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MagicModuleLoader {

	private static Map<String, Object> modules = new ConcurrentHashMap<>();

	private static Function<String, Object> classLoader = (className) -> {
		try {
			return Class.forName(className);
		} catch (Exception e) {
			return null;
		}
	};

	public static Map<String, ScriptClass> getModules() {
		return modules.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
			if (entry.getValue() instanceof Class) {
				Class<?> clazz = (Class<?>) entry.getValue();
				return MagicScriptEngine.getScriptClassFromClass(clazz);
			} else {
				return MagicScriptEngine.getScriptClassFromClass(entry.getValue().getClass());
			}
		}));
	}

	public static void setClassLoader(Function<String, Object> classLoader) {
		MagicModuleLoader.classLoader = classLoader;
	}

	public static void addModule(String moduleName, Object target) {
		modules.put(moduleName, target);
	}

	public static Object loadModule(String moduleName) {
		return modules.get(moduleName);
	}

	public static Object loadClass(String className) {
		return classLoader.apply(className);
	}
}
