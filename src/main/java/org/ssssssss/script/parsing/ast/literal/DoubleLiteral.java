package org.ssssssss.script.parsing.ast.literal;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Literal;

/**
 * double常量
 */
public class DoubleLiteral extends Literal {
	private Double value;

	public DoubleLiteral(Span literal) {
		super(literal);
		try {
			this.value = Double.parseDouble(literal.getText().substring(0, literal.getText().length() - 1));
		} catch (NumberFormatException e) {
			MagicScriptError.error("定义double变量值不合法", literal, e);
		}
	}

	@Override
	public Object evaluate(MagicScriptContext context) {
		return value;
	}
}