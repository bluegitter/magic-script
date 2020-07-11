package org.ssssssss.script.parsing.ast.binary;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Expression;

/**
 * - 操作
 */
public class SubtractionOperation extends BinaryOperation {

	public SubtractionOperation(Expression leftOperand, Span span, Expression rightOperand) {
		super(leftOperand, span, rightOperand);
	}

	@Override
	public Object evaluate(MagicScriptContext context) {
		Object left = getLeftOperand().evaluate(context);
		if (left == null) {
			MagicScriptError.error(getLeftOperand().getSpan().getText() + "[-]操作的值不能为空", getLeftOperand().getSpan());
		}
		Object right = getRightOperand().evaluate(context);
		if (right == null) {
			MagicScriptError.error(getRightOperand().getSpan().getText() + "[-]操作的值不能为空", getRightOperand().getSpan());
		}
		if (left instanceof Double || right instanceof Double) {
			return ((Number) left).doubleValue() - ((Number) right).doubleValue();
		} else if (left instanceof Float || right instanceof Float) {
			return ((Number) left).floatValue() - ((Number) right).floatValue();
		} else if (left instanceof Long || right instanceof Long) {
			return ((Number) left).longValue() - ((Number) right).longValue();
		} else if (left instanceof Integer || right instanceof Integer) {
			return ((Number) left).intValue() - ((Number) right).intValue();
		} else if (left instanceof Short || right instanceof Short) {
			return ((Number) left).shortValue() - ((Number) right).shortValue();
		} else if (left instanceof Byte || right instanceof Byte) {
			return ((Number) left).byteValue() - ((Number) right).byteValue();
		} else {
			MagicScriptError.error("[-]操作的值必须是数值类型，获得的值为：" + left + "，" + right, getSpan());
			return null; // never reached
		}
	}
}
