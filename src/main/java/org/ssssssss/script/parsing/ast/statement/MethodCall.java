package org.ssssssss.script.parsing.ast.statement;

import org.ssssssss.script.compile.MagicScriptCompiler;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Expression;

import java.util.List;

public class MethodCall extends Expression {
	private final MemberAccess method;
	private final List<Expression> arguments;
	private boolean inLinq;

	public MethodCall(Span span, MemberAccess method, List<Expression> arguments, boolean inLinq) {
		super(span);
		this.method = method;
		this.arguments = arguments;
		this.inLinq = inLinq;
	}

	public MemberAccess getMethod() {
		return method;
	}

	public List<Expression> getArguments() {
		return arguments;
	}

	@Override
	public void visitMethod(MagicScriptCompiler compiler) {
		method.visitMethod(compiler);
		arguments.forEach(it -> it.visitMethod(compiler));
	}


	@Override
	public void compile(MagicScriptCompiler compiler) {
		compiler.compile(method.getObject())
				.load1()
				.ldc(method.getName().getText())
				.insn(arguments.stream().anyMatch(it -> it instanceof Spread) ? ICONST_1 : ICONST_0)
				.asBoolean()
				.insn(method.isOptional() ? ICONST_1 : ICONST_0)
				.asBoolean()
				.visit(arguments)
				.call("invoke_method",arguments.size() + 5);
	}
}