package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.parsing.Span;

public class IntegerLiteral extends Literal {
	private Integer value;

	public IntegerLiteral(Span literal) {
		super(literal);
		try {
			this.value = Integer.parseInt(literal.getText());
		} catch (NumberFormatException e) {
			MagicScriptError.error("定义int变量值不合法", literal, e);
		}
	}

	public Integer getValue() {
		return value;
	}

	@Override
	public Object evaluate(MagicScriptContext context) {
		return value;
	}
}