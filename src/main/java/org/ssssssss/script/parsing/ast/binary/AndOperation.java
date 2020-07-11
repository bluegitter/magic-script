package org.ssssssss.script.parsing.ast.binary;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.BinaryOperation;
import org.ssssssss.script.parsing.ast.Expression;

/**
 * && 操作
 */
public class AndOperation extends BinaryOperation {

	public AndOperation(Expression leftOperand, Span span, Expression rightOperand) {
		super(leftOperand, span, rightOperand);
	}

	@Override
	public Object evaluate(MagicScriptContext context) {
		Object left = getLeftOperand().evaluate(context);
		if (!(left instanceof Boolean)) {	//判断值必须是boolean类型
			MagicScriptError.error("[&&]左侧必须是boolean类型,获得的值为：" + left , getLeftOperand().getSpan());
		}
		if (!(Boolean) left) {
			return false;
		}
		Object right = getRightOperand().evaluate(context);
		if (!(right instanceof Boolean)) {
			MagicScriptError.error("[&&]右侧必须是boolean类型,获得的值为： " + right, getRightOperand().getSpan());
		}
		return (Boolean) left && (Boolean) right;
	}
}
