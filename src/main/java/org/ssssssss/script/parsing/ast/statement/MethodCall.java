package org.ssssssss.script.parsing.ast.statement;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.functions.DynamicMethod;
import org.ssssssss.script.interpreter.AstInterpreter;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Expression;
import org.ssssssss.script.reflection.JavaInvoker;
import org.ssssssss.script.reflection.JavaReflection;
import org.ssssssss.script.reflection.MethodInvoker;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class MethodCall extends Expression {
	private final MemberAccess method;
	private final List<Expression> arguments;
	private final ThreadLocal<Object[]> cachedArguments;
	private JavaInvoker<Method> cachedMethod;
	private boolean inLinq;

	public MethodCall(Span span, MemberAccess method, List<Expression> arguments) {
		this(span, method, arguments, false);
	}

	public MethodCall(Span span, MemberAccess method, List<Expression> arguments, boolean inLinq) {
		super(span);
		this.method = method;
		this.arguments = arguments;
		this.cachedArguments = new InheritableThreadLocal<>();
		this.inLinq = inLinq;
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

	public JavaInvoker<Method> getCachedMethod() {
		return cachedMethod;
	}

	public void setCachedMethod(JavaInvoker<Method> cachedMethod) {
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

	public void clearCachedArguments() {
		Object[] args = getCachedArguments();
		for (int i = 0; i < args.length; i++) {
			args[i] = null;
		}
	}

	@Override
	public Object evaluate(MagicScriptContext context, Scope scope) {
		try {
			Object object = getObject().evaluate(context, scope, inLinq);
			if (object == null) {
				if (method.isOptional()) {
					return null;
				}
				MagicScriptError.error(String.format("??????[%s]??????", getObject().getSpan().getText()), getObject().getSpan());
			}
			Object[] argumentValues = getCachedArguments();
			List<Expression> arguments = getArguments();
			for (int i = 0, n = argumentValues.length, pIndex = 0; i < n; i++) {
				Expression expr = arguments.get(i);
				if (expr instanceof Spread) {
					Object[] spreadValues = ((Spread) expr).doSpread(context, scope, inLinq);
					int spreadLength = spreadValues.length;
					if (spreadLength > 0) {
						Object[] valTemp = argumentValues;
						argumentValues = new Object[argumentValues.length + spreadLength - 1];
						System.arraycopy(valTemp, 0, argumentValues, 0, valTemp.length);
						System.arraycopy(spreadValues, 0, argumentValues, pIndex, spreadLength);
						pIndex += spreadLength;
					}
				} else {
					argumentValues[pIndex++] = expr.evaluate(context, scope, inLinq);
				}
			}
			if (object instanceof DynamicMethod) {
				try {
					MethodInvoker invoker = new MethodInvoker(DynamicMethod.class.getDeclaredMethod("execute", String.class, List.class));
					Object[] newArgumentValues = new Object[]{getMethod().getName().getText(), Arrays.asList(argumentValues)};
					return invoker.invoke0(object, scope, newArgumentValues);
				} catch (Throwable t) {
					MagicScriptError.error(t.getMessage(), getSpan(), t);
					return null; // never reached
				}
			}

			// Otherwise try to find a corresponding method or field pointing to a lambda.
			JavaInvoker<Method> invoker = getCachedMethod();
			if (invoker != null) {
				try {
					return invoker.invoke0(object, scope, argumentValues);
				} catch (Throwable t) {
					MagicScriptError.error(t.getMessage(), getSpan(), t);
					return null; // never reached
				}
			}

			invoker = JavaReflection.getMethod(object, getMethod().getName().getText(), argumentValues);
			if (invoker != null) {
				setCachedMethod(invoker);
				try {
					return invoker.invoke0(object, scope, argumentValues);
				} catch (Throwable t) {
					MagicScriptError.error(t.getMessage(), getSpan(), t);
					return null; // never reached
				}
			} else {
				Field field = JavaReflection.getField(object, getMethod().getName().getText());
				String className = object instanceof Class ? ((Class<?>) object).getName() : object.getClass().getName();
				if (field == null) {
					MagicScriptError.error("???'" + className + "'?????????????????? " + getMethod().getName().getText() + "(" + String.join(",", JavaReflection.getStringTypes(argumentValues)) + ")",
							getSpan());
				}
				Object function = JavaReflection.getFieldValue(object, field);
				invoker = JavaReflection.getMethod(function, null, argumentValues);
				if (invoker == null) {
					MagicScriptError.error("???'" + className + "'?????????????????? " + getMethod().getName().getText() + "(" + String.join(",", JavaReflection.getStringTypes(argumentValues)) + ")",
							getSpan());
				}
				try {
					return invoker.invoke0(function, scope, argumentValues);
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