package org.ssssssss.script.parsing.ast.statement;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.exception.ExceptionUtils;
import org.ssssssss.script.exception.MagicScriptException;
import org.ssssssss.script.interpreter.AstInterpreter;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Expression;
import org.ssssssss.script.parsing.ast.Literal;
import org.ssssssss.script.parsing.ast.VariableSetter;
import org.ssssssss.script.parsing.ast.literal.StringLiteral;
import org.ssssssss.script.reflection.AbstractReflection;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class MemberAccess extends Expression implements VariableSetter {
	private final Expression object;
	private final Span name;
	private Field cachedMember;
	private final boolean optional;
	private final boolean whole;

	public MemberAccess(Expression object, boolean optional, Span name, boolean whole) {
		super(name);
		this.object = object;
		this.name = name;
		this.optional = optional;
		this.whole = whole;
	}

	public boolean isWhole() {
		return whole;
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
	 **/
	public Field getCachedMember() {
		return cachedMember;
	}

	/**
	 * Sets the member descriptor as returned by {@link AbstractReflection#getField(Object, String)} or
	 * {@link AbstractReflection#getMethod(Object, String, Object...)} for faster member lookups. Called by {@link AstInterpreter} the
	 * first time this node is evaluated. Subsequent evaluations can use the cached descriptor, avoiding a costly reflective
	 * lookup.
	 **/
	public void setCachedMember(Field cachedMember) {
		this.cachedMember = cachedMember;
	}

	public boolean isOptional() {
		return optional;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public Object evaluate(MagicScriptContext context, Scope scope) {
		Object object = getObject().evaluate(context, scope);
		if (object == null) {
			if (optional) {
				return null;
			}
			MagicScriptError.error(String.format("对象[%s]为空", getObject().getSpan().getText()), getObject().getSpan());
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
		if (object instanceof Class<?>) {
			if (getName().getText().equals("class")) {
				return object;
			}
		}

		Field field = getCachedMember();
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
			// [{a:1},{a:2}] --> list.a == 1
			if (object instanceof Collection) {
				if (isInLinq()) {
					return ((Collection) object).stream().map(it -> {
						if (it instanceof Map) {
							Map map = (Map) it;
							return map.get(getName().getText());
						}
						return null;
					}).collect(Collectors.toList());
				} else {
					Object value = ((Collection) object).stream().findFirst().orElse(Collections.emptyMap());
					if (value instanceof Map) {
						Map map = (Map) value;
						return map.get(getName().getText());
					}
				}
			}
			String methodName;
			if (text.length() > 1) {
				methodName = text.substring(0, 1).toUpperCase() + text.substring(1);
			} else {
				methodName = text.toUpperCase();
			}
			MemberAccess access = new MemberAccess(this.object, this.optional, new Span("get" + methodName), whole);
			MethodCall methodCall = new MethodCall(getName(), access, Collections.emptyList());
			try {
				return methodCall.evaluate(context, scope);
			} catch (MagicScriptException e) {
				if (ExceptionUtils.indexOfThrowable(e, InvocationTargetException.class) > -1) {
					MagicScriptError.error(String.format("在%s中调用方法get%s发生异常"
							, object.getClass()
							, methodName), getSpan(), e);
					return null;
				}
				access = new MemberAccess(this.object, this.optional, new Span("get"), false);
				methodCall = new MethodCall(getName(), access, Arrays.asList(new StringLiteral(getName())));
				try {
					return methodCall.evaluate(context, scope);
				} catch (MagicScriptException e3) {
					if (ExceptionUtils.indexOfThrowable(e3, InvocationTargetException.class) > -1) {
						MagicScriptError.error(String.format("在%s中调用方法get发生异常"
								, object.getClass()
								, methodName), getSpan(), e);
						return null;
					}
					access = new MemberAccess(this.object, this.optional, new Span("is" + methodName), whole);
					methodCall = new MethodCall(getName(), access, Collections.emptyList());
					try {
						return methodCall.evaluate(context, scope);
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
	public void setValue(MagicScriptContext context, Scope scope, Object value) {
		Object object = getObject().evaluate(context, scope);
		if (object == null) {
			// ?
		} else if (object instanceof Map) {
			Map map = (Map) object;
			map.put(getName().getText(), value);
		} else {
			Field field = getCachedMember();
			if (field != null) {
				try {
					AbstractReflection.getInstance().setFieldValue(object, field, value);
				} catch (Throwable t) {
					// fall through
				}
			} else {
				String text = getName().getText();
				field = AbstractReflection.getInstance().getField(object, text);
				if (field == null) {
					String methodName;
					if (text.length() > 1) {
						methodName = text.substring(0, 1).toUpperCase() + text.substring(1);
					} else {
						methodName = text.toUpperCase();
					}
					MemberAccess access = new MemberAccess(this.object, this.optional, new Span("set" + methodName), whole);
					MethodCall methodCall = new MethodCall(getName(), access, Collections.singletonList(new Literal(null) {
						@Override
						public Object evaluate(MagicScriptContext context, Scope scope) {
							return value;
						}
					}));
					try {
						methodCall.evaluate(context, scope);
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