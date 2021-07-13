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
	public List<Span> visitSpan() {
		return mergeSpans(method, arguments);
	}

	@Override
	public void visitMethod(MagicScriptCompiler compiler) {
		method.visitMethod(compiler);
		arguments.forEach(it -> it.visitMethod(compiler));
	}


	@Override
	public void compile(MagicScriptCompiler compiler) {
		compiler.visit(method.getObject())	// 访问目标对象
				.load1()	// MagicScriptContext
				.ldc(method.getName().getText())	// 方法名
				.insn(arguments.stream().anyMatch(it -> it instanceof Spread) ? ICONST_1 : ICONST_0)	// 是否是 (...xxx)
				.asBoolean()
				.insn(method.isOptional() ? ICONST_1 : ICONST_0)	// 是否允许可空调用
				.asBoolean()
				.visit(arguments)
				.call("invoke_method",arguments.size() + 5);	// 调用方法
	}
}