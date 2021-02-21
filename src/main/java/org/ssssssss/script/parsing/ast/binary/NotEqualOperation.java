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
 * !=、!==操作
 */
public class NotEqualOperation extends EqualOperation {

	public NotEqualOperation(Expression leftOperand, Span span, Expression rightOperand, boolean accurate) {
		super(leftOperand, span, rightOperand,accurate);
	}

	@Override
	public Object evaluate(MagicScriptContext context, Scope scope) {
		return !(boolean)super.evaluate(context,scope);
	}
}
