package org.ssssssss.script.parsing.ast.statement;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.exception.MagicExitException;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Expression;
import org.ssssssss.script.parsing.ast.Node;
import org.ssssssss.script.parsing.ast.literal.BooleanLiteral;
import org.ssssssss.script.runtime.ExitValue;

import java.util.List;

/**
 * assert expr : expr[,expr][,expr][,expr][,expr]
 */
public class Assert extends Node {

	private final Expression condition;

	private final List<Expression> expressions;

	public Assert(Span span, Expression condition, List<Expression> expressions) {
		super(span);
		this.condition = condition;
		this.expressions = expressions;
	}

	@Override
	public Object evaluate(MagicScriptContext context, Scope scope) {
		Object value = condition.evaluate(context, scope);
		if (!BooleanLiteral.isTrue(value)) {
			Object[] values = new Object[expressions.size()];
			for (int i = 0, len = values.length; i < len; i++) {
				values[i] = expressions.get(i).evaluate(context, scope);
			}
			throw new MagicExitException(new ExitValue(values));
		}
		return value;
	}
}
