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

/**
 * /= 运算
 */
public class ForwardSlashEqualOperation extends BinaryOperation {

	public ForwardSlashEqualOperation(Expression leftOperand, Span span, Expression rightOperand) {
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
		if (right == null) {
			MagicScriptError.error(getRightOperand().getSpan().getText() + " 值为空，不能执行[/=]操作", getRightOperand().getSpan());
		}
		Object value = null;
		if (left instanceof Integer || right instanceof Integer) {
			return ((Number) left).intValue() / ((Number) right).intValue();
		} else if (left instanceof Double || right instanceof Double) {
			value = ((Number) left).doubleValue() / ((Number) right).doubleValue();
		} else if (left instanceof Long || right instanceof Long) {
			value = ((Number) left).longValue() / ((Number) right).longValue();
		} else if (left instanceof BigDecimal || right instanceof BigDecimal) {
			value = ObjectConvertExtension.asDecimal(left).divide(ObjectConvertExtension.asDecimal(right));
		} else if (left instanceof Float || right instanceof Float) {
			value = ((Number) left).floatValue() / ((Number) right).floatValue();
		} else if (left instanceof Short || right instanceof Short) {
			value = ((Number) left).shortValue() / ((Number) right).shortValue();
		} else if (left instanceof Byte || right instanceof Byte) {
			value = ((Number) left).byteValue() / ((Number) right).byteValue();
		} else {
			MagicScriptError.error("[/=]操作的值必须是数值类型, 获得的值为： " + left + ", " + right, getSpan());
		}
		((VariableSetter) getLeftOperand()).setValue(context, scope, value);
		return value;
	}
}
