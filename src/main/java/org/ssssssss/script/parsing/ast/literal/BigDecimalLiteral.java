package org.ssssssss.script.parsing.ast.literal;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Literal;

import java.math.BigDecimal;

/**
 * int常量
 */
public class BigDecimalLiteral extends Literal {
	private BigDecimal value;

	public BigDecimalLiteral(Span literal) {
		super(literal);
		this.value = new BigDecimal(literal.getText().substring(0, literal.getText().length() - 1));
	}

	@Override
	public Object evaluate(MagicScriptContext context) {
		return value;
	}
}