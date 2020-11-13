package org.ssssssss.script.parsing.ast.literal;

import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Literal;

import java.math.BigDecimal;

/**
 * int常量
 */
public class BigDecimalLiteral extends Literal {
	public BigDecimalLiteral(Span literal) {
		super(literal, new BigDecimal(literal.getText().substring(0, literal.getText().length() - 1)));
	}

}