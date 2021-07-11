package org.ssssssss.script.parsing.ast.statement;

import org.ssssssss.script.compile.MagicScriptCompiler;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Expression;

import java.util.List;

public class NewStatement extends Expression {

	private final List<Expression> arguments;

	private final Expression target;

	public NewStatement(Span span, Expression target, List<Expression> arguments) {
		super(span);
		this.target = target;
		this.arguments = arguments;
	}

	@Override
	public void visitMethod(MagicScriptCompiler compiler) {
		target.visitMethod(compiler);
		arguments.forEach(it -> it.visitMethod(compiler));
	}

	@Override
	public void compile(MagicScriptCompiler compiler) {
		compiler.visit(target)	// 访问目标
				.visit(arguments)	// 访问参数
				.call("invoke_new_instance", arguments.size() + 1);	// 执行new操作
	}
}
