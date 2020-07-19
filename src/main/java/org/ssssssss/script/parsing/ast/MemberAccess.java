package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.exception.ExceptionUtils;
import org.ssssssss.script.exception.MagicScriptException;
import org.ssssssss.script.interpreter.AbstractReflection;
import org.ssssssss.script.interpreter.AstInterpreter;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.literal.StringLiteral;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class MemberAccess extends Expression implements VariableSetter {
	private final Expression object;
	private final Span name;
	private Object cachedMember;

	public MemberAccess(Expression object, Span name) {
		super(name);
		this.object = object;
		this.name = name;
	}

	/**
	 * Returns the object on which to access the member.
	 **/
	public Expression getObject() {
		return object;
	}

	/**
	 * The name of the member.
	 **/
	public Span getName() {
		return name;
	}

	/**
	 * Returns the cached member descriptor as returned by {@link AbstractReflection#getField(Object, String)} or
	 * {@link AbstractReflection#getMethod(Object, String, Object...)}. See {@link #setCachedMember(Object)}.
	 **/
	public Object getCachedMember() {
		return cachedMember;
	}

	/**
	 * Sets the member descriptor as returned by {@link AbstractReflection#getField(Object, String)} or
	 * {@link AbstractReflection#getMethod(Object, String, Object...)} for faster member lookups. Called by {@link AstInterpreter} the
	 * first time this node is evaluated. Subsequent evaluations can use the cached descriptor, avoiding a costly reflective
	 * lookup.
	 **/
	public void setCachedMember(Object cachedMember) {
		this.cachedMember = cachedMember;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object evaluate(MagicScriptContext context) {
		Object object = getObject().evaluate(context);
		if (object == null) {
			MagicScriptError.error(String.format("对象[%s]为空",getObject().getSpan().getText()),getObject().getSpan());
		}

		// special case for array.length
		if (object.getClass().isArray() && "length".equals(getName().getText())) {
			return Array.getLength(object);
		}

		// special case for map, allows to do map.key instead of map[key]
		if (object instanceof Map) {
			Map map = (Map) object;
			return map.get(getName().getText());
		}

		Object field = getCachedMember();
		if (field != null) {
			try {
				return AbstractReflection.getInstance().getFieldValue(object, field);
			} catch (Throwable t) {
				// fall through
			}
		}
		String text = getName().getText();
		field = AbstractReflection.getInstance().getField(object, text);
		if (field == null) {
			String methodName;
			if (text.length() > 1) {
				methodName = text.substring(0, 1).toUpperCase() + text.substring(1);
			} else {
				methodName = text.toUpperCase();
			}
			MemberAccess access = new MemberAccess(this.object, new Span("get" + methodName));
			MethodCall methodCall = new MethodCall(getName(), access, Collections.emptyList());
			try {
				return methodCall.evaluate(context);
			} catch (MagicScriptException e) {
				if (ExceptionUtils.indexOfThrowable(e, InvocationTargetException.class) > -1) {
					MagicScriptError.error(String.format("在%s中调用方法get%s发生异常"
							, object.getClass()
							, methodName), getSpan(), e);
					return null;
				}
				access = new MemberAccess(this.object, new Span("get"));
				methodCall = new MethodCall(getName(), access, Arrays.asList(new StringLiteral(getName())));
				try {
					return methodCall.evaluate(context);
				} catch (MagicScriptException e3) {
					if (ExceptionUtils.indexOfThrowable(e3, InvocationTargetException.class) > -1) {
						MagicScriptError.error(String.format("在%s中调用方法get发生异常"
								, object.getClass()
								, methodName), getSpan(), e);
						return null;
					}
					access = new MemberAccess(this.object, new Span("is" + methodName));
					methodCall = new MethodCall(getName(), access, Collections.emptyList());
					try {
						return methodCall.evaluate(context);
					} catch (MagicScriptException e1) {
						if (ExceptionUtils.indexOfThrowable(e1, InvocationTargetException.class) > -1) {
							MagicScriptError.error(String.format("在%s中调用方法is%s发生异常"
									, object.getClass()
									, methodName), getSpan(), e);
							return null;
						}
						MagicScriptError.error(String.format("在%s中找不到属性%s或者方法get%s、方法is%s"
								, object.getClass()
								, getName().getText()
								, methodName
								, methodName), getSpan());
					}
				}
			}
		}
		setCachedMember(field);
		return AbstractReflection.getInstance().getFieldValue(object, field);
	}

	@Override
	public void setValue(MagicScriptContext context, Object value) {
		Object object = getObject().evaluate(context);
		if (object == null) {
			// ?
		} else if (object instanceof Map) {
			Map map = (Map) object;
			map.put(getName().getText(), value);
		} else {
			Object field = getCachedMember();
			if (field != null) {
				try {
					AbstractReflection.getInstance().setFieldValue(object, field, value);
				} catch (Throwable t) {
					// fall through
				}
			}else{
				String text = getName().getText();
				field = AbstractReflection.getInstance().getField(object, text);
				if (field == null) {
					String methodName;
					if (text.length() > 1) {
						methodName = text.substring(0, 1).toUpperCase() + text.substring(1);
					} else {
						methodName = text.toUpperCase();
					}
					MemberAccess access = new MemberAccess(this.object, new Span("set" + methodName));
					MethodCall methodCall = new MethodCall(getName(), access, Collections.singletonList(new Literal(null) {
						@Override
						public Object evaluate(MagicScriptContext context) {
							return value;
						}
					}));
					try {
						methodCall.evaluate(context);
					} catch (MagicScriptException e) {
						if (ExceptionUtils.indexOfThrowable(e, InvocationTargetException.class) > -1) {
							MagicScriptError.error(String.format("在%s中调用方法get%s发生异常"
									, object.getClass()
									, methodName), getSpan(), e);
						}
						MagicScriptError.error(String.format("在%s中找不到属性%s或者方法set%s"
								, object.getClass()
								, getName().getText()
								, methodName), getSpan());
					}
				}
			}
		}
	}
}