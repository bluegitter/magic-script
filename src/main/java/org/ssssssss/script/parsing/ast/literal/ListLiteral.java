package org.ssssssss.script.parsing.ast.literal;

import org.ssssssss.script.compile.MagicScriptCompiler;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Expression;
import org.ssssssss.script.parsing.ast.Literal;
import org.ssssssss.script.parsing.ast.statement.Spread;

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
	public void compile(MagicScriptCompiler compiler) {
		int size = values.size();
		if (size == 0) {
			compiler.typeInsn(NEW, ArrayList.class)
					.insn(DUP)
					.invoke(INVOKESPECIAL, ArrayList.class, "<init>", void.class);
		} else {
			compiler.insn(values.stream().anyMatch(it -> it instanceof Spread) ? ICONST_1 : ICONST_0)
					.asBoolean()
					.visit(values)
					.call("newArrayList", size + 1);
		}
	}
}