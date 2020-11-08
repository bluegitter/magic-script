package org.ssssssss.script;

import org.ssssssss.script.interpreter.AstInterpreter;

import java.util.*;


/**
 * <p>
 * A magicScript context stores mappings from variable names to user provided variable values. A {@link MagicScript} is given a context
 * for rendering to resolve variable values it references in magicScript expressions.
 * </p>
 *
 * <p>
 * Internally, a magicScript context is a stack of these mappings, similar to scopes in a programming language, and used as such by
 * the {@link AstInterpreter}.
 * </p>
 */
public class MagicScriptContext {
	private final static ThreadLocal<MagicScriptContext> CONTEXT_THREAD_LOCAL = new InheritableThreadLocal<>();
	private final ThreadLocal<List<Map<String, Object>>> scopes = new InheritableThreadLocal<>();

	/**
	 * Keeps track of previously allocated, unused scopes. New scopes are first tried to be retrieved from this pool to avoid
	 * generating garbage.
	 **/
	private final List<Map<String, Object>> freeScopes = new ArrayList<Map<String, Object>>();

	public MagicScriptContext() {
		push();
	}

	public MagicScriptContext(Map<String, Object> variables) {
		this();
		if (variables != null) {
			for (Map.Entry<String, Object> entry : variables.entrySet()) {
				set(entry.getKey(), entry.getValue());
			}
		}
	}

	public static MagicScriptContext get() {
		return CONTEXT_THREAD_LOCAL.get();
	}

	public static void remove() {
		CONTEXT_THREAD_LOCAL.remove();
	}

	public static void set(MagicScriptContext context) {
		CONTEXT_THREAD_LOCAL.set(context);
	}

	/**
	 * Sets the value of the variable with the given name. If the variable already exists in one of the scopes, that variable is
	 * set. Otherwise the variable is set on the last pushed scope.
	 */
	public MagicScriptContext set(String name, Object value) {
		List<Map<String, Object>> scopeList = scopes.get();
		for (int i = scopeList.size() - 1; i >= 0; i--) {
			Map<String, Object> ctx = scopeList.get(i);
			if (ctx.isEmpty()) {
				continue;
			}
			if (ctx.containsKey(name)) {
				ctx.put(name, value);
				return this;
			}
		}

		scopeList.get(scopeList.size() - 1).put(name, value);
		return this;
	}

	/**
	 * Sets the value of the variable with the given name on the last pushed scope
	 **/
	public MagicScriptContext setOnCurrentScope(String name, Object value) {
		List<Map<String, Object>> scopeList = scopes.get();
		scopeList.get(scopeList.size() - 1).put(name, value);
		return this;
	}

	/**
	 * Internal. Returns the value of the variable with the given name, walking the scope stack from top to bottom, similar to how
	 * scopes in programming languages are searched for variables.
	 */
	public Object get(String name) {
		List<Map<String, Object>> scopeList = scopes.get();
		for (int i = scopeList.size() - 1; i >= 0; i--) {
			Map<String, Object> ctx = scopeList.get(i);
			if (ctx.isEmpty()) {
				continue;
			}
			Object value = ctx.get(name);
			if (value != null) {
				return value;
			}
		}
		return MagicPackageLoader.findClass(name);
	}

	public String getString(String name) {
		return getString(name, null);
	}

	public String getString(String name, String defaultValue) {
		Object value = get(name);
		return value == null ? defaultValue : value.toString();
	}

	/**
	 * Internal. Returns all variables currently defined in this context.
	 */
	public Map<String, Object> getVariables() {
		List<Map<String, Object>> scopeList = scopes.get();
		Map<String, Object> variables = new HashMap<>();
		for (Map<String, Object> scope : scopeList) {
			variables.putAll(scope);
		}
		return variables;
	}

	public List<Map<String, Object>> getScopes() {
		List<Map<String, Object>> list = new ArrayList<>();
		List<Map<String, Object>> scopeList = scopes.get();
		for (Map<String, Object> item : scopeList) {
			list.add(new HashMap<>(item));
		}
		return list;
	}

	public void setScopes(List<Map<String, Object>> scopeList) {
		scopes.set(scopeList);
	}

	/**
	 * Internal. Pushes a new "scope" onto the stack.
	 **/
	public void push() {
		Map<String, Object> newScope = freeScopes.size() > 0 ? freeScopes.remove(freeScopes.size() - 1) : new HashMap<String, Object>();
		List<Map<String, Object>> scopeList = scopes.get();
		if (scopeList == null) {
			scopeList = new ArrayList<>();
			scopes.set(scopeList);
			;
		}
		scopeList.add(newScope);
	}

	/**
	 * Internal. Pops the top of the "scope" stack.
	 **/
	public void pop() {
		List<Map<String, Object>> scopeList = scopes.get();
		if (scopeList != null) {
			Map<String, Object> oldScope = scopeList.remove(scopeList.size() - 1);
			oldScope.clear();
			freeScopes.add(oldScope);
		}
	}

	public void putMapIntoContext(Map<String, Object> map) {
		if (map != null && !map.isEmpty()) {
			Set<Map.Entry<String, Object>> entries = map.entrySet();
			for (Map.Entry<String, Object> entry : entries) {
				set(entry.getKey(), entry.getValue());
			}
		}
	}
}
