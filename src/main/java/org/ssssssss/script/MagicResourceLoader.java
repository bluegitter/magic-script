package org.ssssssss.script;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 资源加载器
 */
public class MagicResourceLoader {

	/**
	 * 保存已注册的模块
	 */
	private static Map<String, Object> MODULES = new ConcurrentHashMap<>();

	/**
	 * 保存自动导入的包路径
	 */
	private static final Set<String> PACKAGES = new HashSet<>();

	/**
	 * 函数加载器
	 */
	private static final List<Function<String, Object>> FUNCTION_LOADERS = new ArrayList<>();

	/**
	 * JSR223 脚本函数加载器
	 */
	private static final List<Function<String, BiFunction<Map<String, Object>, String, Object>>> SCRIPT_LANGUAGE_LOADERS = new ArrayList<>();

	static {
		// 默认导入 java.util.* 、java.lang.*
		addPackage("java.util.*");
		addPackage("java.lang.*");
	}

	/**
	 * 默认的类加载器
	 */
	private static Function<String, Object> classLoader = (className) -> {
		try {
			return Class.forName(className);
		} catch (Exception e) {
			return null;
		}
	};

	/**
	 * 获取已注册的模块信息，此方法主要用于代码提示
	 */
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

	/**
	 * 添加函数加载器
	 */
	public static void addFunctionLoader(Function<String, Object> functionLoader) {
		FUNCTION_LOADERS.add(functionLoader);
	}

	/**
	 * 设置类加载器
	 */
	public static void setClassLoader(Function<String, Object> classLoader) {
		MagicResourceLoader.classLoader = classLoader;
	}

	/**
	 * 添加模块
	 * @param moduleName	模块名称
	 * @param target	模块，可以是对象实例，也可以是Class类型的，此时只能使用类中的静态方法
	 */
	public static void addModule(String moduleName, Object target) {
		MODULES.put(moduleName, target);
	}

	/**
	 * 加载模块
	 * @param moduleName	模块名称
	 */
	public static Object loadModule(String moduleName) {
		return MODULES.get(moduleName);
	}

	/**
	 * 加载类
	 * @param className	类全限定名
	 */
	public static Object loadClass(String className) {
		return classLoader.apply(className);
	}

	/**
	 * 获取可用的模块列表
	 */
	public static Set<String> getModuleNames() {
		return MODULES.keySet();
	}

	/**
	 * 添加自动导包
	 * @param prefix	包前缀，如java.lang.*， 不支持 java.lang.**.*
	 */
	public static void addPackage(String prefix) {
		PACKAGES.add(prefix.replace("*", ""));
	}

	/**
	 * 加载类
	 * @param simpleName	类缩写，如HashMap、ArrayList
	 */
	public static Class<?> findClass(String simpleName) {
		for (String prefix : PACKAGES) {
			try {
				return Class.forName(prefix + simpleName);
			} catch (Exception ignored) {
			}
		}
		return null;
	}

	/**
	 * 添加JSR223 脚本函数加载器
	 */
	public static void addScriptLanguageLoader(Function<String, BiFunction<Map<String, Object>, String, Object>> loader){
		SCRIPT_LANGUAGE_LOADERS.add(loader);
	}

	/**
	 * 加载脚本函数加载器
	 * @param name	脚本名称
	 */
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

	/**
	 * 加载函数加载器
	 * @param name	函数名称
	 */
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
