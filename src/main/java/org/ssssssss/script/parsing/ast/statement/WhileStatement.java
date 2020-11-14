package org.ssssssss.script.parsing.ast.statement;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.interpreter.AstInterpreter;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Expression;
import org.ssssssss.script.parsing.ast.Node;
import org.ssssssss.script.parsing.ast.literal.BooleanLiteral;

import java.util.List;

public class WhileStatement extends Node {

	private final Expression condition;
	private final List<Node> trueBlock;

	public WhileStatement(Span span, Expression condition, List<Node> trueBlock) {
		super(span);
		this.condition = condition;
		this.trueBlock = trueBlock;
	}

	@Override
	public Object evaluate(MagicScriptContext context, Scope scope) {
		while (BooleanLiteral.isTrue(condition.evaluate(context, scope))) {
			Object breakOrContinueOrReturn = AstInterpreter.interpretNodeList(trueBlock, context, scope);
			if (breakOrContinueOrReturn == Break.BREAK_SENTINEL) {
				break;
			}
			if (breakOrContinueOrReturn instanceof Return.ReturnValue) {
				return breakOrContinueOrReturn;
			}
		}
		return null;
	}
}
