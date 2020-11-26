package org.ssssssss.script.parsing.ast.literal;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Expression;
import org.ssssssss.script.parsing.ast.Literal;
import org.ssssssss.script.parsing.ast.statement.AutoExpand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
			Expression expression = values.get(i);
			if (expression instanceof AutoExpand) {
				AutoExpand autoExpand = (AutoExpand) expression;
				Object res = autoExpand.getTarget().evaluate(context, scope);
				if (res == null) {
					// 其实是因为该变量未定义
				} else if (res instanceof Collection) {
					list.addAll(((Collection) res));
				} else if (res instanceof Map) {
					MagicScriptError.error("不能在list中展开map", autoExpand.getFullSpan());
				} else {
					MagicScriptError.error("不能展开的类型", autoExpand.getFullSpan());
				}
			} else {
				list.add(expression.evaluate(context, scope));
			}
		}
		return list;
	}
}