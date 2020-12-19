package org.ssssssss.script.parsing.ast.statement;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.interpreter.AstInterpreter;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Expression;
import org.ssssssss.script.parsing.ast.Node;
import org.ssssssss.script.parsing.ast.literal.BooleanLiteral;

import java.util.List;

public class IfStatement extends Node {
	private final Expression condition;
	private final List<Node> trueBlock;
	private final List<IfStatement> elseIfs;
	private final List<Node> falseBlock;
	private final int trueVarCount;
	private final int falseVarCount;

	public IfStatement(Span span, Expression condition, List<Node> trueBlock, List<IfStatement> elseIfs, List<Node> falseBlock,int trueVarCount,int falseVarCount) {
		super(span);
		this.condition = condition;
		this.trueBlock = trueBlock;
		this.elseIfs = elseIfs;
		this.falseBlock = falseBlock;
		this.trueVarCount = trueVarCount;
		this.falseVarCount = falseVarCount;
	}

	public Expression getCondition() {
		return condition;
	}

	public List<Node> getTrueBlock() {
		return trueBlock;
	}

	public List<IfStatement> getElseIfs() {
		return elseIfs;
	}

	public List<Node> getFalseBlock() {
		return falseBlock;
	}

	@Override
	public Object evaluate(MagicScriptContext context, Scope scope) {
		Object condition = getCondition().evaluate(context, scope);
		if (BooleanLiteral.isTrue(condition)) {
			return AstInterpreter.interpretNodeList(getTrueBlock(), context, scope.create(trueVarCount));
		}

		if (getElseIfs().size() > 0) {
			for (IfStatement elseIf : getElseIfs()) {
				condition = elseIf.getCondition().evaluate(context, scope);
				if (BooleanLiteral.isTrue(condition)) {
					return AstInterpreter.interpretNodeList(elseIf.getTrueBlock(), context, scope.create(elseIf.trueVarCount));
				}
			}
		}

		if (getFalseBlock().size() > 0) {
			return AstInterpreter.interpretNodeList(getFalseBlock(), context, scope.create(falseVarCount));
		}
		return null;
	}
}