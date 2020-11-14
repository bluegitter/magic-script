package org.ssssssss.script.parsing.ast.statement;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Node;

public class Return extends Node {

	private final Node returnValue;

	public Return(Span span, Node returnValue) {
		super(span);
		this.returnValue = returnValue;
	}

	@Override
	public Object evaluate(MagicScriptContext context, Scope scope) {
		return new ReturnValue(returnValue == null ? null : returnValue.evaluate(context, scope));
	}

	/**
	 * A sentital of which only one instance exists. Uses thread local storage to store an (optional) return value. See
	 **/
	public static class ReturnValue {
		private Object value;

		public ReturnValue(Object value) {
			this.value = value;
		}

		public Object getValue() {
			return value;
		}
	}
}