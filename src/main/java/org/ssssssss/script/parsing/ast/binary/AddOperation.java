package org.ssssssss.script.parsing.ast.binary;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.functions.ObjectConvertExtension;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.BinaryOperation;
import org.ssssssss.script.parsing.ast.Expression;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * + 运算
 */
public class AddOperation extends BinaryOperation {

	public AddOperation(Expression leftOperand, Span span, Expression rightOperand) {
		super(leftOperand, span, rightOperand);
	}

	@Override
	public Object evaluate(MagicScriptContext context, Scope scope) {
		Object left = getLeftOperand().evaluate(context, scope);
		Object right = getRightOperand().evaluate(context, scope);
		// 如果其中左右其中一个是String，则拼接字符串
		if (left instanceof String || right instanceof String) {
			return left + Objects.toString(right);
		}
		if (right == null) {
			MagicScriptError.error(getRightOperand().getSpan().getText() + " 值为空，不能执行[+]操作", getRightOperand().getSpan());
		}
		if (left == null) {
			MagicScriptError.error(getLeftOperand().getSpan().getText() + " 值为空，不能执行[+]操作", getLeftOperand().getSpan());
		}
		Object value = add(left, right);
		if (value == null) {
			MagicScriptError.error("[+]操作的值必须是String或数值类型, 获得的值为： " + left + ", " + right, getSpan());
		}
		return value;
	}

	public static Object add(Object left, Object right) {
		if (left instanceof BigDecimal || right instanceof BigDecimal) {
			return ObjectConvertExtension.asDecimal(left).add(ObjectConvertExtension.asDecimal(right));
		}
		if (left instanceof Double || right instanceof Double) {
			return ((Number) left).doubleValue() + ((Number) right).doubleValue();
		}
		if (left instanceof Float || right instanceof Float) {
			return ((Number) left).floatValue() + ((Number) right).floatValue();
		}
		if (left instanceof Long || right instanceof Long) {
			return ((Number) left).longValue() + ((Number) right).longValue();
		}
		if (left instanceof Integer || right instanceof Integer) {
			return ((Number) left).intValue() + ((Number) right).intValue();
		}
		if (left instanceof Short || right instanceof Short) {
			return ((Number) left).shortValue() + ((Number) right).shortValue();
		}
		if (left instanceof Byte || right instanceof Byte) {
			return ((Number) left).byteValue() + ((Number) right).byteValue();
		}
		return null;
	}
}
