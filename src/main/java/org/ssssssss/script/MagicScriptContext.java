package org.ssssssss.script;

import org.ssssssss.script.exception.MagicScriptException;
import org.ssssssss.script.interpreter.AstInterpreter;
import org.ssssssss.script.parsing.Parser;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Tokenizer;
import org.ssssssss.script.parsing.ast.Expression;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


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

	private final ThreadLocal<Scope> CONTEXT_VAR_SCOPE = new InheritableThreadLocal<>();

	private Map<String, Object> rootVariables = new HashMap<>();

	public MagicScriptContext() {
	}

	public MagicScriptContext(Map<String, Object> variables) {
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


	public String getString(String name) {
		return Objects.toString(get(name), null);
	}

	public Object get(String name) {
		Scope scope = CONTEXT_VAR_SCOPE.get();
		if (scope != null) {
			return scope.getValue(name);
		}
		return null;
	}

	/**
	 * Sets the value of the variable with the given name. If the variable already exists in one of the scopes, that variable is
	 * set. Otherwise the variable is set on the last pushed scope.
	 */
	public MagicScriptContext set(String name, Object value) {
		rootVariables.put(name, value);
		return this;
	}

	public void setVarScope(Scope scope) {
		CONTEXT_VAR_SCOPE.set(scope);
	}

	public void removeVarScope() {
		CONTEXT_VAR_SCOPE.remove();
	}

	/**
	 * Internal. Returns all variables currently defined in this context.
	 */

	public Object eval(String script) {
		try {
			Parser parser = new Parser();
			Expression expression = parser.parseExpression(Tokenizer.tokenize(script));
			Scope scope = Scope.getTempScope();
			if(scope == null){
				scope = CONTEXT_VAR_SCOPE.get();
			}
			return expression.evaluate(this, scope.create(parser.getTopVarCount()));
		} catch (Exception e) {
			Throwable throwable = MagicScriptError.unwrap(e);
			if (throwable instanceof MagicScriptException) {
				throw new RuntimeException(((MagicScriptException) throwable).getSimpleMessage());
			}
			throw new RuntimeException(throwable);
		}
	}

	public Map<String, Object> getRootVariables() {
		return rootVariables;
	}

	public void putMapIntoContext(Map<String, Object> map) {
		if (map != null && !map.isEmpty()) {
			rootVariables.putAll(map);
		}
	}
}
