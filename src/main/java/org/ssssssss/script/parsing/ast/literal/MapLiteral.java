package org.ssssssss.script.parsing.ast.literal;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.Token;
import org.ssssssss.script.parsing.TokenType;
import org.ssssssss.script.parsing.ast.Expression;
import org.ssssssss.script.parsing.ast.Literal;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * map常量
 */
public class MapLiteral extends Literal {
	private final List<Token> keys;
	private final List<Expression> values;

	public MapLiteral(Span span, List<Token> keys, List<Expression> values) {
		super(span);
		this.keys = keys;
		this.values = values;
	}

	@Override
	public Object evaluate(MagicScriptContext context, Scope scope) {
		Map<String, Object> map = new LinkedHashMap<>();
		for (int i = 0, n = keys.size(); i < n; i++) {
			Token tokenKey = keys.get(i);
			String key = tokenKey.getSpan().getText();
			if (tokenKey.getType() == TokenType.StringLiteral) {
				key = (String) new StringLiteral(tokenKey.getSpan()).evaluate(context, scope);
			} else if (key != null && key.startsWith("$")) {    //如果key是$开头的，则认为是动态key值
				key = key.substring(1);
				if (!key.startsWith("$")) {    //如果是$$开头的变量，则认为是普通key..
					Object objKey = context.get(key);
					if (objKey != null) {
						key = objKey.toString();
					} else {
						MagicScriptError.error("map的key值不能为空", tokenKey.getSpan());
					}
				}
			}
			map.put(key, values.get(i).evaluate(context, scope));
		}
		return map;
	}
}