package org.ssssssss.script.parsing.ast.statement;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.exception.MagicExitException;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Expression;
import org.ssssssss.script.parsing.ast.Node;
import org.ssssssss.script.runtime.ExitValue;

import java.util.Arrays;
import java.util.List;

public class Exit extends Node {

	private final List<Expression> expressions;

	public Exit(Span span,List<Expression> expressions) {
		super(span);
		this.expressions = expressions;
	}

	@Override
	public Object evaluate(MagicScriptContext context, Scope scope) {
		if(expressions == null){
			throw new MagicExitException(new ExitValue(new Object[0]));
		}
		Object[] values = new Object[expressions.size()];
		for (int i = 0,len = values.length; i < len; i++) {
			values[i] = expressions.get(i).evaluate(context, scope);
		}
		throw new MagicExitException(new ExitValue(values));
	}

}
