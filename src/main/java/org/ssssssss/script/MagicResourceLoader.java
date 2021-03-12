package org.ssssssss.script;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MagicResourceLoader {

	private static Map<String, Object> MODULES = new ConcurrentHashMap<>();

	private static final Set<String> PACKAGES = new HashSet<>();

	private static List<Function<String, Object>> FUNCTION_LOADERS = new ArrayList<>();

	private static List<Function<String, BiFunction<Map<String, Object>, String, Object>>> SCRIPT_LANGUAGE_LOADERS = new ArrayList<>();

	static {
		addPackage("java.util.*");
		addPackage("java.lang.*");
	}

	private static Function<String, Object> classLoader = (className) -> {
		try {
			return Class.forName(className);
		} catch (Exception e) {
			return null;
		}
	};

	public static Map<String, ScriptClass> getModules() {
		return MODULES.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
			if (entry.getValue() instanceof Class) {
				Class<?> clazz = (Class<?>) entry.getValue();
				return MagicScriptEngine.getScriptClassFromClass(clazz);
			} else {
				return MagicScriptEngine.getScriptClassFromClass(entry.getValue().getClass());
			}
		}));
	}

	public static void addFunctionLoader(Function<String, Object> functionLoader) {
		FUNCTION_LOADERS.add(functionLoader);
	}

	public static void setClassLoader(Function<String, Object> classLoader) {
		MagicResourceLoader.classLoader = classLoader;
	}

	public static void addModule(String moduleName, Object target) {
		MODULES.put(moduleName, target);
	}

	public static Object loadModule(String moduleName) {
		return MODULES.get(moduleName);
	}

	public static Object loadClass(String className) {
		return classLoader.apply(className);
	}

	public static Set<String> getModuleNames() {
		return MODULES.keySet();
	}

	public static void addPackage(String prefix) {
		PACKAGES.add(prefix.replace("*", ""));
	}

	public static Class<?> findClass(String simpleName) {
		for (String prefix : PACKAGES) {
			try {
				return Class.forName(prefix + simpleName);
			} catch (Exception ignored) {
			}
		}
		return null;
	}

	public static void addScriptLanguageLoader(Function<String, BiFunction<Map<String, Object>, String, Object>> loader){
		SCRIPT_LANGUAGE_LOADERS.add(loader);
	}

	public static BiFunction<Map<String, Object>, String, Object> loadScriptLanguage(String name) {
		for (Function<String, BiFunction<Map<String, Object>, String, Object>> languageLoader : SCRIPT_LANGUAGE_LOADERS) {
			try {
				BiFunction<Map<String, Object>, String, Object> function = languageLoader.apply(name);
				if(function != null){
					return function;
				}
			} catch (Exception ignored) {
			}
		}
		return null;
	}

	public static Object loadFunction(String name) {
		for (Function<String, Object> loader : FUNCTION_LOADERS) {
			try {
				Object value = loader.apply(name);
				if (value != null) {
					return value;
				}
			} catch (Exception ignored) {
			}
		}
		return null;
	}
}
