package org.ssssssss.script.parsing.ast.binary;

import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.compile.MagicScriptCompiler;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.BinaryOperation;
import org.ssssssss.script.parsing.ast.Expression;
import org.ssssssss.script.parsing.ast.VariableSetter;
import org.ssssssss.script.parsing.ast.statement.VariableAccess;

import java.util.Collections;
import java.util.List;

/**
 * = 操作
 */
public class AssigmentOperation extends BinaryOperation {

	public AssigmentOperation(Expression leftOperand, Span span, Expression rightOperand) {
		super(leftOperand, span, rightOperand);
	}

	@Override
	public List<Span> visitSpan() {
		if(getLeftOperand() instanceof VariableAccess) {
			return super.visitSpan();
		}else{
			return Collections.singletonList(getSpan());
		}
	}

	@Override
	public void compile(MagicScriptCompiler compiler) {
		if(getLeftOperand() instanceof VariableAccess){
			compiler.pre_store(((VariableAccess) getLeftOperand()).getVarIndex())
					.compile(getRightOperand());
			if(getRightOperand() instanceof AssigmentOperation){
				compiler.visit(((AssigmentOperation)getRightOperand()).getLeftOperand());
			}
			compiler.store();
		}else if (getLeftOperand() instanceof VariableSetter) {
			((VariableSetter)getLeftOperand()).compile_visit_variable(compiler);
			compiler.compile(getRightOperand()).call("set_variable_value",3);
		}else{
			MagicScriptError.error("赋值目标应为变量", getLeftOperand().getSpan());
		}
	}
}
