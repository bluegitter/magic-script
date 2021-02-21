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
 * ==、===操作
 */
public class EqualOperation extends BinaryOperation {

	private final boolean accurate;

	public EqualOperation(Expression leftOperand, Span span, Expression rightOperand, boolean accurate) {
		super(leftOperand, span, rightOperand);
		this.accurate = accurate;
	}

	@Override
	public Object evaluate(MagicScriptContext context, Scope scope) {
		Object left = getLeftOperand().evaluate(context, scope);
		Object right = getRightOperand().evaluate(context, scope);
		if(Objects.equals(left, right)){
			return true;
		}
		if(!accurate){
			if (left instanceof Number || right instanceof Number) {
				BigDecimal leftValue = ObjectConvertExtension.asDecimal(left, null);
				BigDecimal rightValue = ObjectConvertExtension.asDecimal(right, null);
				return leftValue!=null && rightValue != null && leftValue.compareTo(rightValue) == 0;
			}
		}
		return false;
	}
}
