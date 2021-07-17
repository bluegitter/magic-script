package org.ssssssss.script;

import org.ssssssss.script.exception.MagicScriptException;
import org.ssssssss.script.runtime.MagicScriptRuntime;

import java.util.*;


/**
 * 脚本环境上下文
 * 编译后的类每个方法的第一个参数都是本类。
 * 此类主要用于辅助读写变量以及设置/读取/
 */
public class MagicScriptContext {

	private final static ThreadLocal<MagicScriptContext> CONTEXT_THREAD_LOCAL = new InheritableThreadLocal<>();

	/**
	 * 保存手动设置的环境变量
	 */
	private final Map<String, Object> rootVariables = new HashMap<>();

	private static final Object NULL_VALUE = new Object();

	/**
	 * 保存脚本中的变量
	 */
	private final Stack<Object[]> vars = new Stack<>();

	private final Stack<int[]> copied = new Stack<>();

	private final List<String> importPackages = new ArrayList<>();

	private MagicScriptRuntime runtime;

	public MagicScriptContext() {
	}

	public MagicScriptContext(Map<String, Object> variables) {
		putMapIntoContext(variables);
	}

	/**
	 * 从本地线程中获取当前上下文
	 */
	public static MagicScriptContext get() {
		return CONTEXT_THREAD_LOCAL.get();
	}

	/**
	 * 从本地线程中移除上下文
	 */
	public static void remove() {
		CONTEXT_THREAD_LOCAL.remove();
	}

	/**
	 * 设置上下文到本地线程中
	 */
	public static void set(MagicScriptContext context) {
		CONTEXT_THREAD_LOCAL.set(context);
	}

	/**
	 * 获取当前作用域内的String变量值
	 *
	 * @param name 变量名称
	 * @return 变量值
	 */
	public String getString(String name) {
		return Objects.toString(get(name), null);
	}

	/**
	 * 添加 .* 的导包
	 * @param packageName	包名 如 java.text.
	 */
	public void addImport(String packageName){
		importPackages.add(packageName);
	}

	public Class<?> getImportClass(String simpleClassName){
		for (int i = importPackages.size() - 1; i >= 0 ; i--) {
			try {
				return Class.forName(importPackages.get(i) + simpleClassName);
			} catch (ClassNotFoundException ignored) {
			}
		}
		return null;
	}

	/**
	 * 获取当前作用域内的变量值
	 *
	 * @param name 变量名称
	 * @return 变量值
	 */
	public Object get(String name) {
		return rootVariables.get(name);
	}

	/**
	 * 设置环境变量
	 *
	 * @param name  变量名
	 * @param value 变量值
	 */
	public MagicScriptContext set(String name, Object value) {
		rootVariables.put(name, value);
		return this;
	}

	public Object[] createVariables(MagicScriptRuntime runtime, int size) {
		this.runtime = runtime;
		Object[] variables = new Object[size];
		for (int i = 0; i < size; i++) {
			variables[i] = NULL_VALUE;
		}
		setVars(variables);
		return variables;
	}

	/**
	 * 从当前上下文中动态执行脚本
	 *
	 * @param script 脚本内容
	 */
	public Object eval(String script) {
		try {
			MagicScript magicScript = MagicScript.create(script, null);
			MagicScriptRuntime runtime = magicScript.compile();
			return runtime.execute(new MagicScriptContext(MagicScriptContext.get().getVariables()));
		} catch (Exception e) {
			Throwable throwable = MagicScriptError.unwrap(e);
			if (throwable instanceof MagicScriptException) {
				throw new RuntimeException(((MagicScriptException) throwable).getSimpleMessage());
			}
			throw new RuntimeException(throwable);
		}
	}

	public Map<String, Object> getVariables() {
		Map<String, Object> map = new LinkedHashMap<>();
		String[] names = runtime.getVarNames();
		Object[] vars = getVars();
		for (int i = 0, len = names.length; i < len; i++) {
			Object value = vars[i];
			if (value != NULL_VALUE) {
				map.put(names[i], value);
			}
		}
		return map;
	}

	public Map<String, Object> getRootVariables() {
		return rootVariables;
	}

	/**
	 * 批量设置环境变量
	 */
	public void putMapIntoContext(Map<String, Object> map) {
		if (map != null && !map.isEmpty()) {
			rootVariables.putAll(map);
		}
	}

	/**
	 * 从环境中获取值，此方法给编译后的类专用。
	 *
	 * @param name 变量名
	 */
	public Object getEnvironmentValue(String name) {
		Object value = get(name);
		value = value == null ? getImportClass(name) : value;
		value = value == null ? MagicResourceLoader.findClass(name) : value;
		return value == null ? MagicResourceLoader.loadModule(name) : value;
	}

	/**
	 * 无条件复制一份变量表
	 */
	private Object[] newVars() {
		Object[] peek = vars.peek();
		Object[] dest = new Object[peek.length];
		System.arraycopy(peek, 0, dest, 0, peek.length);
		return dest;
	}


	/**
	 * 设置变量表的值，此方法给编译后的类专用。
	 */
	public void setVars(Object[] vars) {
		this.vars.push(vars);
	}

	/**
	 * 获取变量表，此方法给编译后的类专用。
	 */
	public Object[] getVars() {
		return vars.peek();
	}
	/**
	 * 获取变量值，此方法给编译后的类专用。
	 */
	public void setVarValue(int index, Object value) {
		if(index > -1){
			vars.peek()[index] = value;
		}
	}

	/**
	 * 复制变量值，此方法给编译后的类专用。
	 *
	 * @param target 传入的参数
	 * @param args   参数在数组中的位置
	 * @return 复制结果
	 */
	public Object[] copy(Object[] target, int... args) {
		Object[] vars;
		if(args.length > 0){
			vars = newVars();
			for (int i = 0, len = args.length; i < len; i++) {
				vars[args[i]] = target[i];
			}
		}else{
			vars = getVars();
		}
		setVars(vars);
		copied.push(args);
		return vars;
	}


	public void restore() {
		Object[] dest = vars.pop();
		Object[] src = getVars();
		int[] pos = copied.pop();
		outer:
		for (int i = 0, fromIndex = 0, len = src.length, size = pos.length; i < len; i++) {
			for (int j = fromIndex; j < size; j++) {
				if (i == pos[j]) {
					fromIndex = j;
					continue outer;
				}
			}
			src[i] = dest[i];
		}
	}
}
