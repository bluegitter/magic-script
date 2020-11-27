package org.ssssssss.script.parsing.ast.binary;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.functions.ObjectConvertExtension;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.BinaryOperation;
import org.ssssssss.script.parsing.ast.Expression;
import org.ssssssss.script.parsing.ast.VariableSetter;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * += 运算
 */
public class PlusEqualOperation extends BinaryOperation {

	public PlusEqualOperation(Expression leftOperand, Span span, Expression rightOperand) {
		super(leftOperand, span, rightOperand);
	}

	@Override
	public Object evaluate(MagicScriptContext context, Scope scope) {
		Object left = getLeftOperand().evaluate(context, scope);
		if (!(getLeftOperand() instanceof VariableSetter)) {
			MagicScriptError.error(getLeftOperand().getSpan().getText() + " 应该可写", getLeftOperand().getSpan());
		}
		Object right = getRightOperand().evaluate(context, scope);
		// 如果其中左右其中一个是String，则拼接字符串
		if (left instanceof String || right instanceof String) {
			String value = left + Objects.toString(right);
			((VariableSetter) getLeftOperand()).setValue(context, scope, value);
			return value;
		}
		if (right == null) {
			MagicScriptError.error(getRightOperand().getSpan().getText() + " 值为空，不能执行[+=]操作", getRightOperand().getSpan());
		}
		if (left == null) {
			MagicScriptError.error(getLeftOperand().getSpan().getText() + " 值为空，不能执行[+=]操作", getLeftOperand().getSpan());
		}
		Object value = null;
		if (left instanceof Double || right instanceof Double) {
			value = ((Number) left).doubleValue() + ((Number) right).doubleValue();
		} else if (left instanceof Long || right instanceof Long) {
			value = ((Number) left).longValue() + ((Number) right).longValue();
		} else if (left instanceof Integer || right instanceof Integer) {
			value = ((Number) left).intValue() + ((Number) right).intValue();
		} else if (left instanceof BigDecimal || right instanceof BigDecimal) {
			value = ObjectConvertExtension.asDecimal(left).add(ObjectConvertExtension.asDecimal(right));
		} else if (left instanceof Float || right instanceof Float) {
			value = ((Number) left).floatValue() + ((Number) right).floatValue();
		} else if (left instanceof Short || right instanceof Short) {
			value = ((Number) left).shortValue() + ((Number) right).shortValue();
		} else if (left instanceof Byte || right instanceof Byte) {
			value = ((Number) left).byteValue() + ((Number) right).byteValue();
		} else {
			MagicScriptError.error("[+=]操作的值必须是String或数值类型, 获得的值为： " + left + ", " + right, getSpan());
		}
		((VariableSetter) getLeftOperand()).setValue(context, scope, value);
		return value;
	}
}
