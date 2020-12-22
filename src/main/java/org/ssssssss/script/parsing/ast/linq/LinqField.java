package org.ssssssss.script.parsing.ast.linq;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.functions.MapExtension;
import org.ssssssss.script.functions.StreamExtension;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.VarIndex;
import org.ssssssss.script.parsing.ast.Expression;
import org.ssssssss.script.parsing.ast.VariableSetter;
import org.ssssssss.script.parsing.ast.statement.MemberAccess;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class LinqField extends Expression implements VariableSetter {

	private final Expression expression;

	private final String aliasName;

	private final VarIndex varIndex;

	private final boolean hasAlias;

	public LinqField(Span span, Expression expression, VarIndex alias) {
		super(span);
		this.expression = expression;
		this.hasAlias = alias != null;
		this.aliasName = hasAlias ? alias.getName() : expression.getSpan().getText();
		this.varIndex = alias;
	}

	public Expression getExpression() {
		return expression;
	}

	@Override
	public Object evaluate(MagicScriptContext context, Scope scope) {
		if (expression instanceof MemberAccess && ((MemberAccess) expression).isWhole()) {
			return ((MemberAccess) expression).getObject().evaluate(context, scope);
		}
		return expression.evaluate(context, scope);
	}


	public List<Object> evaluateList(MagicScriptContext context, Scope scope) {
		Object target = expression.evaluate(context, scope);
		List<Object> objects;
		if (target instanceof Map) {
			objects = (List<Object>) MapExtension.asList((Map<?, ?>) target, (entry) -> Collections.singletonMap(entry[0], entry[1]));
		}
		try {
			objects = StreamExtension.arrayLikeToList(target);
		} catch (Exception e) {
			return Collections.singletonList(target);
		}
		return objects;
	}

	public boolean isHasAlias() {
		return hasAlias;
	}

	public String getAlias() {
		return aliasName;
	}

	@Override
	public void setValue(MagicScriptContext context, Scope scope, Object value) {
		if (hasAlias) {
			scope.setValue(varIndex, value);
		}
	}
}
