package org.ssssssss.script.parsing.ast.binary;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.functions.ObjectConvertExtension;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.BinaryOperation;
import org.ssssssss.script.parsing.ast.Expression;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * !=操作
 */
public class NotEqualOperation extends BinaryOperation {

	public NotEqualOperation(Expression leftOperand, Span span, Expression rightOperand) {
		super(leftOperand, span, rightOperand);
	}

	@Override
	public Object evaluate(MagicScriptContext context, Scope scope) {
		Object left = getLeftOperand().evaluate(context, scope);
		Object right = getRightOperand().evaluate(context, scope);
		if (left instanceof BigDecimal || right instanceof BigDecimal) {
			return ObjectConvertExtension.asDecimal(left).compareTo(ObjectConvertExtension.asDecimal(right)) != 0;
		}
		return !Objects.equals(left, right);
	}
}
