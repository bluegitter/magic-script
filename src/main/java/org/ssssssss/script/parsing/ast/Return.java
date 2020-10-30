package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.parsing.Span;

public class Return extends Node {

    private final Node returnValue;

    public Return(Span span, Node returnValue) {
        super(span);
        this.returnValue = returnValue;
    }

    @Override
    public Object evaluate(MagicScriptContext context) {
		return new ReturnValue(returnValue.evaluate(context));
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