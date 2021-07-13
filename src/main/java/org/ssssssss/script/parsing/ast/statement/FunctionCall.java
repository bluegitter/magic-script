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
	public List<Span> visitSpan() {
		return mergeSpans(function, arguments);
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
		compiler.visit(function)	// 访问函数
				.load1()	// 参数 MagicScriptContext
				.ldc(getFunction().getSpan().getText())	// 函数名
				.insn(arguments.stream().anyMatch(it -> it instanceof Spread) ? ICONST_1 : ICONST_0)
				.asBoolean()	// 是否有扩展参数(...xxx)
				.insn(ICONST_0)	// 不可以可空调用 ?.
				.asBoolean()
				.visit(arguments)	// 访问参数
				.call("invoke_method", arguments.size() + 5);	// 调用函数
	}
}