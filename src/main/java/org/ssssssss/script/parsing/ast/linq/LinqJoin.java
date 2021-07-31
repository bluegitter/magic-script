package org.ssssssss.script.parsing.ast.linq;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Expression;
import org.ssssssss.script.parsing.ast.literal.BooleanLiteral;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LinqJoin extends Expression {

	private final LinqField target;

	private final Expression condition;

	private final boolean leftJoin;

	private List<Object> cachedValue;

	public LinqJoin(Span span, boolean leftJoin, LinqField target, Expression condition) {
		super(span);
		this.leftJoin = leftJoin;
		this.target = target;
		this.condition = condition;
	}

	public boolean isLeftJoin() {
		return leftJoin;
	}

	public LinqField getTarget() {
		return target;
	}

	public void setCachedValue(List<Object> cachedValue) {
		this.cachedValue = cachedValue;
	}

	@Override
	public List<Object> evaluate(MagicScriptContext context, Scope scope) {
		List<Object> result = leftJoin ? new ArrayList<>() : null;
		for (Object object : cachedValue) {
			target.setValue(context, scope, object);
			if (BooleanLiteral.isTrue(condition.evaluate(context, scope))) {
				if (isLeftJoin()) {
					result.add(object);
				} else {
					result = Collections.singletonList(object);
					target.setValue(context, scope, result);
					return result;
				}
			}
		}
		List<Object> value = isLeftJoin() ? result : Collections.emptyList();
		target.setValue(context, scope, value);
		return value;
	}
}
