package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;

/**
 * 常量
 */
public abstract class Literal extends Expression {

	private Object value;

	public Literal(Span span) {
		super(span);
	}

	public Literal(Span span, Object value) {
		super(span);
		this.value = value;
	}

	@Override
	public Object evaluate(MagicScriptContext context, Scope scope) {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
}
