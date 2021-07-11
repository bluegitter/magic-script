package org.ssssssss.script.parsing.ast.statement;

import org.ssssssss.script.compile.MagicScriptCompiler;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Expression;

import java.util.List;

public class FunctionCall extends Expression {
	private final Expression function;
	private final List<Expression> arguments;

	private final boolean inLinq;

	public FunctionCall(Span span, Expression function, List<Expression> arguments, boolean inLinq) {
		super(span);
		this.function = function;
		this.arguments = arguments;
		this.inLinq = inLinq;
	}

	@Override
	public void visitMethod(MagicScriptCompiler compiler) {
		function.visitMethod(compiler);
		arguments.forEach(it -> it.visitMethod(compiler));
	}

	public Expression getFunction() {
		return function;
	}

	public List<Expression> getArguments() {
		return arguments;
	}

	@Override
	public void compile(MagicScriptCompiler compiler) {
		compiler.compile(function)
				.load1()
				.ldc(getFunction().getSpan().getText())
				.insn(arguments.stream().anyMatch(it -> it instanceof Spread) ? ICONST_1 : ICONST_0)
				.asBoolean()
				.insn(ICONST_0)
				.asBoolean()
				.visit(arguments)
				.call("invoke_method", arguments.size() + 5);
	}
}