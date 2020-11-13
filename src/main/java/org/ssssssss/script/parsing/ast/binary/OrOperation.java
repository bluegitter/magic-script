package org.ssssssss.script.parsing.ast.binary;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.BinaryOperation;
import org.ssssssss.script.parsing.ast.Expression;
import org.ssssssss.script.parsing.ast.literal.BooleanLiteral;

/**
 * || 操作
 */
public class OrOperation extends BinaryOperation {

	public OrOperation(Expression leftOperand, Span span, Expression rightOperand) {
		super(leftOperand, span, rightOperand);
	}

	@Override
	public Object evaluate(MagicScriptContext context) {
		Object left = getLeftOperand().evaluate(context);
		if (BooleanLiteral.isTrue(left)) {
			return left;
		}
		return getRightOperand().evaluate(context);
	}
}
