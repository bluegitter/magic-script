package org.ssssssss.script.parsing.ast.literal;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Expression;
import org.ssssssss.script.parsing.ast.Literal;

import java.util.ArrayList;
import java.util.List;

/**
 * List常量
 */
public class ListLiteral extends Literal {

	public final List<Expression> values;

	public ListLiteral(Span span, List<Expression> values) {
		super(span);
		this.values = values;
	}

	@Override
	public Object evaluate(MagicScriptContext context, Scope scope) {
		List<Object> list = new ArrayList<>();
		for (int i = 0, n = values.size(); i < n; i++) {
			list.add(values.get(i).evaluate(context, scope));
		}
		return list;
	}
}