package org.ssssssss.script.parsing.ast.statement;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Expression;
import org.ssssssss.script.reflection.AbstractReflection;
import org.ssssssss.script.reflection.JavaInvoker;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class FunctionCall extends Expression {
	private static final BiFunction<Integer, Integer, Iterator<Integer>> range = (from, to) -> new Iterator<Integer>() {
		int idx = from;

		public boolean hasNext() {
			return idx <= to;
		}

		public Integer next() {
			return idx++;
		}
	};
	private final Expression function;
	private final List<Expression> arguments;
	private JavaInvoker<Method> cachedFunction;
	private final ThreadLocal<Object[]> cachedArguments;

	public FunctionCall(Span span, Expression function, List<Expression> arguments) {
		super(span);
		this.function = function;
		this.arguments = arguments;
		this.cachedArguments = new InheritableThreadLocal<>();
	}

	public Expression getFunction() {
		return function;
	}

	public List<Expression> getArguments() {
		return arguments;
	}

	public JavaInvoker<Method> getCachedFunction() {
		return cachedFunction;
	}

	public void setCachedFunction(JavaInvoker<Method> cachedFunction) {
		this.cachedFunction = cachedFunction;
	}

	public Object[] getCachedArguments() {
		Object[] args = cachedArguments.get();
		if (args == null) {
			args = new Object[arguments.size()];
			cachedArguments.set(args);
		}
		return args;
	}

	/**
	 * Must be invoked when this node is done evaluating so we don't leak memory
	 **/
	public void clearCachedArguments() {
		Object[] args = getCachedArguments();
		for (int i = 0; i < args.length; i++) {
			args[i] = null;
		}
	}

	@Override
	public Object evaluate(MagicScriptContext context, Scope scope) {
		try {
			Object[] argumentValues = getCachedArguments();
			List<Expression> arguments = getArguments();
			for (int i = 0, n = argumentValues.length; i < n; i++) {
				Expression expr = arguments.get(i);
				if (expr instanceof Spread) {
					Object targetVal = ((Spread) expr).getTarget().evaluate(context, scope);
					if (targetVal instanceof Collection) {
						n += ((Collection<?>) targetVal).size() - 1;
						Object[] valTemp = argumentValues;
						argumentValues = new Object[n];
						System.arraycopy(valTemp, 0, argumentValues, 0, valTemp.length);
						for (Object o : ((Collection<?>) targetVal)) {
							arguments.add(i, ((Spread) expr).getTarget());
							argumentValues[i++] = o;
						}
					} else {
						MagicScriptError.error("展开的不是一个list", expr.getSpan());
					}
				} else {
					argumentValues[i] = expr.evaluate(context, scope);
				}
			}

			// This is a special case to handle magicScript level macros. If a call to a macro is
			// made, evaluating the function expression will result in an exception, as the
			// function name can't be found in the context. Instead we need to manually check
			// if the function expression is a VariableAccess and if so, if it can be found
			// in the context.
			Object function = null;
			if ("range".equals(getFunction().getSpan().getText())) {
				function = range;
			} else {
				function = getFunction().evaluate(context, scope);
			}
			if (function instanceof Function) {
				return ((Function<Object[], Object>) function).apply(argumentValues);
			}
			if (function != null) {
				JavaInvoker<Method> invoker = getCachedFunction();
				if (invoker != null) {
					try {
						return invoker.invoke0(function, scope, argumentValues);
					} catch (Throwable t) {
						// fall through
					}
				}
				invoker = AbstractReflection.getInstance().getMethod(function, null, argumentValues);
				if (invoker == null) {
					MagicScriptError.error("Couldn't find function.", getSpan());
				}
				setCachedFunction(invoker);
				try {
					return invoker.invoke0(function, scope, argumentValues);
				} catch (Throwable t) {
					MagicScriptError.error(t.getMessage(), getSpan(), t);
					return null; // never reached
				}
			} else {
				MagicScriptError.error("找不到方法 " + getFunction(), getSpan());
				return null; // never reached
			}
		} finally {
			clearCachedArguments();
		}
	}
}