package org.ssssssss.script.parsing.ast.binary;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.functions.ObjectConvertExtension;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.BinaryOperation;
import org.ssssssss.script.parsing.ast.Expression;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * == 操作
 */
public class EqualOperation extends BinaryOperation {

	public EqualOperation(Expression leftOperand, Span span, Expression rightOperand) {
		super(leftOperand, span, rightOperand);
	}

	@Override
	public Object evaluate(MagicScriptContext context) {
		Object left = getLeftOperand().evaluate(context);
		Object right = getRightOperand().evaluate(context);
		if (left instanceof BigDecimal || right instanceof BigDecimal) {
			return ObjectConvertExtension.asDecimal(left).compareTo(ObjectConvertExtension.asDecimal(right)) == 0;
		}
		return Objects.equals(left, right);
	}
}
