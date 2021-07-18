package org.ssssssss.script.parsing.ast.literal;

import org.ssssssss.script.compile.MagicScriptCompiler;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Expression;
import org.ssssssss.script.parsing.ast.Literal;
import org.ssssssss.script.parsing.ast.statement.Spread;

import java.util.List;

/**
 * map常量
 */
public class MapLiteral extends Literal {
	private final List<Expression> keys;
	private final List<Expression> values;

	public MapLiteral(Span span, List<Expression> keys, List<Expression> values) {
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
			Expression key = keys.get(i);
			Expression expression = values.get(i);
			if (expression instanceof Spread) {
				compiler.visit(expression);
				count++;
			} else {
				boolean dynamicKey = false;
				if (key instanceof StringLiteral && !((StringLiteral) key).isTemplateString() && key.getSpan().getText().startsWith("$")) {
					dynamicKey = !key.getSpan().getText().substring(1).startsWith("$");//如果是$$开头的变量，则认为是普通key..
				}
				if (dynamicKey) {
					compiler.load(key.getSpan().getText().substring(1));
				} else {
					compiler.visit(key);
				}
				compiler.visit(expression);
				count += 2;
			}
		}
		compiler.call("newLinkedHashMap", count + 1);
	}
}