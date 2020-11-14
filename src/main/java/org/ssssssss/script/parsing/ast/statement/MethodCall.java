package org.ssssssss.script.parsing.ast.statement;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.functions.DynamicMethod;
import org.ssssssss.script.interpreter.AbstractReflection;
import org.ssssssss.script.interpreter.AstInterpreter;
import org.ssssssss.script.interpreter.JavaReflection;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Expression;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class MethodCall extends Expression {
	private final MemberAccess method;
	private final List<Expression> arguments;
	private final ThreadLocal<Object[]> cachedArguments;
	private Object cachedMethod;

	public MethodCall(Span span, MemberAccess method, List<Expression> arguments) {
		super(span);
		this.method = method;
		this.arguments = arguments;
		this.cachedArguments = new InheritableThreadLocal<>();
	}

	/**
	 * Returns the object on which to call the method.
	 **/
	public Expression getObject() {
		return method.getObject();
	}

	/**
	 * Returns the method to call.
	 **/
	public MemberAccess getMethod() {
		return method;
	}

	/**
	 * Returns the list of expressions to be passed to the function as arguments.
	 **/
	public List<Expression> getArguments() {
		return arguments;
	}

	/**
	 * Returns the cached member descriptor as returned by {@link AbstractReflection#getMethod(Object, String, Object...)}. See
	 **/
	public Object getCachedMethod() {
		return cachedMethod;
	}

	/**
	 * Sets the method descriptor as returned by {@link AbstractReflection#getMethod(Object, String, Object...)} for faster lookups.
	 * Called by {@link AstInterpreter} the first time this node is evaluated. Subsequent evaluations can use the cached
	 * descriptor, avoiding a costly reflective lookup.
	 **/
	public void setCachedMethod(Object cachedMethod) {
		this.cachedMethod = cachedMethod;
	}

	/**
	 * Returns a scratch buffer to store arguments in when calling the function in {@link AstInterpreter}. Avoids generating
	 * garbage.
	 **/
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
			Object object = getObject().evaluate(context, scope);
			if (object == null) {
				MagicScriptError.error(String.format("对象[%s]为空",getObject().getSpan().getText()),getObject().getSpan());
			}
			Object[] argumentValues = getCachedArguments();
			List<Expression> arguments = getArguments();
			for (int i = 0, n = argumentValues.length; i < n; i++) {
				Expression expr = arguments.get(i);
				argumentValues[i] = expr.evaluate(context, scope);
			}
			if (object instanceof DynamicMethod) {
				try {
					Object method = DynamicMethod.class.getDeclaredMethod("execute", String.class, List.class);
					Object[] newArgumentValues = new Object[]{getMethod().getName().getText(), Arrays.asList(argumentValues)};
					return AbstractReflection.getInstance().callMethod(object, method, newArgumentValues);
				} catch (Throwable t) {
					MagicScriptError.error(t.getMessage(), getSpan(), t);
					return null; // never reached
				}
			}

			// Otherwise try to find a corresponding method or field pointing to a lambda.
			Object method = getCachedMethod();
			if (method != null) {
				try {
					return AbstractReflection.getInstance().callMethod(object, method, argumentValues);
				} catch (Throwable t) {
					MagicScriptError.error(t.getMessage(), getSpan(), t);
					return null; // never reached
				}
			}

			method = AbstractReflection.getInstance().getMethod(object, getMethod().getName().getText(), argumentValues);
			if (method != null) {
				// found the method on the object, call it
				setCachedMethod(method);
				try {
					return AbstractReflection.getInstance().callMethod(object, method, argumentValues);
				} catch (Throwable t) {
					MagicScriptError.error(t.getMessage(), getSpan(), t);
					return null; // never reached
				}
			}
			method = AbstractReflection.getInstance().getExtensionMethod(object, getMethod().getName().getText(), argumentValues);
			if (method != null) {
				try {
					int argumentLength = argumentValues == null ? 0 : argumentValues.length;
					Object[] parameters = new Object[argumentLength + 1];
					if (argumentLength > 0) {
						for (int i = 0; i < argumentLength; i++) {
							parameters[i + 1] = argumentValues[i];
						}
					}
					parameters[0] = object;
					if (object.getClass().isArray()) {
						Object[] objs = new Object[Array.getLength(object)];
						for (int i = 0, len = objs.length; i < len; i++) {
							Array.set(objs, i, Array.get(object, i));
						}
						parameters[0] = objs;
					}
					return AbstractReflection.getInstance().callMethod(object, method, parameters);
				} catch (Throwable t) {
					MagicScriptError.error(t.getMessage(), getSpan(), t);
					// fall through
					return null;
				}
			} else {
				Field field = AbstractReflection.getInstance().getField(object, getMethod().getName().getText());
				String className = object instanceof Class ? ((Class<?>) object).getName() : object.getClass().getName();
				if (field == null) {
					MagicScriptError.error("在'" + className + "'中找不到方法 " + getMethod().getName().getText() + "(" + String.join(",", JavaReflection.getStringTypes(argumentValues)) + ")",
							getSpan());
				}
				Object function = AbstractReflection.getInstance().getFieldValue(object, field);
				method = AbstractReflection.getInstance().getMethod(function, null, argumentValues);
				if (method == null) {
					MagicScriptError.error("在'" + className + "'中找不到方法 " + getMethod().getName().getText() + "(" + String.join(",", JavaReflection.getStringTypes(argumentValues)) + ")",
							getSpan());
				}
				try {
					return AbstractReflection.getInstance().callMethod(function, method, argumentValues);
				} catch (Throwable t) {
					MagicScriptError.error(t.getMessage(), getSpan(), t);
					return null; // never reached
				}
			}
		} finally {
			clearCachedArguments();
		}
	}

}