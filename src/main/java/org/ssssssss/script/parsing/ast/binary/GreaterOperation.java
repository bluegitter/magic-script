package org.ssssssss.script.parsing.ast.binary;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.functions.ObjectConvertExtension;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.BinaryOperation;
import org.ssssssss.script.parsing.ast.Expression;

import java.math.BigDecimal;

/**
 * > 运算
 */
public class GreaterOperation extends BinaryOperation {

	public GreaterOperation(Expression leftOperand, Span span, Expression rightOperand) {
		super(leftOperand, span, rightOperand);
	}

	@Override
	public Object evaluate(MagicScriptContext context, Scope scope) {
		Object left = getLeftOperand().evaluate(context, scope);
		if (left == null) {
			MagicScriptError.error(getLeftOperand().getSpan().getText() + "[>]操作的值不能为空", getLeftOperand().getSpan());
		}
		Object right = getRightOperand().evaluate(context, scope);
		if (right == null) {
			MagicScriptError.error(getRightOperand().getSpan().getText() + "[>]操作的值不能为空", getRightOperand().getSpan());
		}
		if (left instanceof BigDecimal || right instanceof BigDecimal) {
			return ObjectConvertExtension.asDecimal(left).compareTo(ObjectConvertExtension.asDecimal(right)) == 1;
		}
		if (left instanceof Double || right instanceof Double) {
			return ((Number) left).doubleValue() > ((Number) right).doubleValue();
		} else if (left instanceof Float || right instanceof Float) {
			return ((Number) left).floatValue() > ((Number) right).floatValue();
		} else if (left instanceof Long || right instanceof Long) {
			return ((Number) left).longValue() > ((Number) right).longValue();
		} else if (left instanceof Integer || right instanceof Integer) {
			return ((Number) left).intValue() > ((Number) right).intValue();
		} else if (left instanceof Short || right instanceof Short) {
			return ((Number) left).shortValue() > ((Number) right).shortValue();
		} else if (left instanceof Byte || right instanceof Byte) {
			return ((Number) left).byteValue() > ((Number) right).byteValue();
		} else {
			MagicScriptError.error("[>]操作的值必须是数值类型，获得的值为：" + left + "，" + right, getSpan());
			return null; // never reached
		}
	}
}
