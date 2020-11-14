package org.ssssssss.script.parsing.ast.binary;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.BinaryOperation;
import org.ssssssss.script.parsing.ast.Expression;
import org.ssssssss.script.parsing.ast.VariableAccess;
import org.ssssssss.script.parsing.ast.VariableSetter;

/**
 * = 操作
 */
public class AssigmentOperation extends BinaryOperation {

	public AssigmentOperation(Expression leftOperand, Span span, Expression rightOperand) {
		super(leftOperand, span, rightOperand);
	}

	@Override
	public Object evaluate(MagicScriptContext context, Scope scope) {
		if (getLeftOperand() instanceof VariableSetter) {
			VariableSetter variableSetter = (VariableSetter) getLeftOperand();
			Object value = getRightOperand().evaluate(context, scope);
			variableSetter.setValue(context, scope, value);
			return value;
		}
		if (!(getLeftOperand() instanceof VariableAccess)) {
			MagicScriptError.error("Can only assign to top-level variables in context.", getLeftOperand().getSpan());
		}
		Object value = getRightOperand().evaluate(context, scope);
		scope.setValue(((VariableAccess) getLeftOperand()).getVarIndex(), value);
		return value;
	}
}
