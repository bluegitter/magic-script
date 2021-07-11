package org.ssssssss.script.parsing.ast.literal;

import org.ssssssss.script.compile.MagicScriptCompiler;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.Token;
import org.ssssssss.script.parsing.TokenType;
import org.ssssssss.script.parsing.ast.Expression;
import org.ssssssss.script.parsing.ast.Literal;
import org.ssssssss.script.parsing.ast.statement.Spread;

import java.util.List;

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
	public void compile(MagicScriptCompiler compiler) {
		int size = keys.size();
		int count = 0;
		compiler.insn(values.stream().anyMatch(it -> it instanceof Spread) ? ICONST_1 : ICONST_0)
				.asBoolean();
		for (int i = 0; i < size; i++) {
			Token tokenKey = keys.get(i);
			boolean dynamicKey = false;
			String key = tokenKey.getSpan().getText();
			if (key != null && key.startsWith("$")) {
				key = key.substring(1);
				dynamicKey = !key.startsWith("$");//如果是$$开头的变量，则认为是普通key..
			}
			Expression expression = values.get(i);
			if (expression instanceof Spread) {
				compiler.visit(expression);
				count++;
			} else {
				if (dynamicKey) {
					compiler.load(key);
				} else {
					if(tokenKey.getType() == TokenType.StringLiteral){
						key = new StringLiteral(tokenKey.getSpan()).getValue();
					}
					compiler.ldc(key);
				}
				compiler.visit(expression);
				count += 2;
			}
		}
		compiler.call("newLinkedHashMap", count + 1);
	}
}