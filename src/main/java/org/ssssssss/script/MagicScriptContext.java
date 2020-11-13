package org.ssssssss.script;

import org.ssssssss.script.exception.MagicScriptException;
import org.ssssssss.script.interpreter.AstInterpreter;
import org.ssssssss.script.parsing.Parser;
import org.ssssssss.script.parsing.Tokenizer;
import org.ssssssss.script.parsing.ast.Expression;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


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

	private final ThreadLocal<Stack<Integer>> THREAD_ID = new InheritableThreadLocal<>();
	private VariableContext variableContext = null;
	private Map<String, Object> tempVariables = new HashMap<>();
	private Map<Integer, VariableContext> threadVariables = new ConcurrentHashMap<>();
	private AtomicInteger thread = new AtomicInteger(0);

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

	/**
	 * Sets the value of the variable with the given name. If the variable already exists in one of the scopes, that variable is
	 * set. Otherwise the variable is set on the last pushed scope.
	 */
	public MagicScriptContext set(String name, Object value) {
		tempVariables.put(name, value);
		return this;
	}


	/**
	 * Internal. Returns the value of the variable with the given name, walking the scope stack from top to bottom, similar to how
	 * scopes in programming languages are searched for variables.
	 */
	public Object get(String name) {
		VarNode varNode = variableContext.get(name);
		return wrapValue(name,varNode.getValue());
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
		return getVariableContext().getCurrentVariables(this);
	}

	public Object eval(String script) {
		try {
			VariableContext variableContext = getVariableContext().copy(true);
			variableContext.setCurrentScope(variableContext.getRuntimeScope());
			Parser parser = new Parser(variableContext);
			Expression expression = parser.parseExpression(Tokenizer.tokenize(script));
			MagicScriptContext context = new MagicScriptContext();
			context.setVariableContext(variableContext);
			return expression.evaluate(context);
		} catch (Exception e) {
			Throwable throwable = MagicScriptError.unwrap(e);
			if (throwable instanceof MagicScriptException) {
				throw new RuntimeException(((MagicScriptException) throwable).getSimpleMessage());
			}
			throw new RuntimeException(throwable);
		}
	}


	public void putMapIntoContext(Map<String, Object> map) {
		if (map != null && !map.isEmpty()) {
			tempVariables.putAll(map);
		}
	}

	public Object getValue(VarNode varNode) {
		Integer threadId = getThreadId();
		if (threadId != null) {
			return wrapValue(varNode.getName(), threadVariables.get(threadId).get(varNode.getScopeIndex(), varNode.getVarIndex()));
		}
		return wrapValue(varNode.getName(), variableContext.get(varNode.getScopeIndex(), varNode.getVarIndex()));
	}

	public Object findAndGet(VarNode varNode) {
		Integer threadId = getThreadId();
		if (threadId != null) {
			return wrapValue(varNode.getName(), threadVariables.get(threadId).findAndGet(varNode));
		}
		return wrapValue(varNode.getName(), variableContext.findAndGet(varNode));
	}

	Object wrapValue(String name, Object value) {
		value = value == null ? tempVariables.get(name) : value;
		value = value == null ? MagicPackageLoader.findClass(name) : value;
		return value == null ? MagicModuleLoader.loadModule(name) : value;
	}

	private Integer getThreadId() {
		Stack<Integer> stack = THREAD_ID.get();
		if (stack != null && !stack.isEmpty()) {
			return stack.peek();
		}
		return null;
	}

	public void setValue(VarNode varNode, Object value) {
		Integer threadId = getThreadId();
		if (threadId != null) {
			threadVariables.get(threadId).set(varNode.getScopeIndex(), varNode.getVarIndex(), value);
			return;
		}
		variableContext.set(varNode.getScopeIndex(), varNode.getVarIndex(), value);
	}

	public void pushThread(Integer threadId) {
		Stack<Integer> stack = THREAD_ID.get();
		if (stack != null) {
			stack.push(threadId);
		} else {
			stack = new Stack<>();
			stack.push(threadId);
		}
		THREAD_ID.set(stack);
	}

	public void popThread(Integer threadId) {
		Stack<Integer> stack = THREAD_ID.get();
		if (stack != null) {
			stack.remove(threadId);
		}
	}

	public synchronized Integer copyThreadVariables() {
		Integer id = thread.incrementAndGet();
		Integer threadId = getThreadId();
		if (threadId != null) {
			threadVariables.put(id, threadVariables.get(threadId).copy(true));
		} else {
			threadVariables.put(id, variableContext.copy(true));
		}
		return id;
	}

	public void deleteThreadVariables(Integer threadId) {
		threadVariables.remove(threadId);
	}

	public VariableContext getVariableContext() {
		Integer threadId = getThreadId();
		if (threadId != null) {
			return threadVariables.get(threadId);
		}
		return variableContext;
	}

	void setVariableContext(VariableContext variableContext) {
		this.variableContext = variableContext;
	}
}
