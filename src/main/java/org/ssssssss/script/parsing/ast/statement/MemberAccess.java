package org.ssssssss.script.parsing.ast.statement;

import org.ssssssss.script.compile.MagicScriptCompiler;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Expression;
import org.ssssssss.script.parsing.ast.VariableSetter;

import java.util.List;

public class MemberAccess extends Expression implements VariableSetter {
	private final Expression object;
	private final Span name;
	private final boolean optional;
	private final boolean whole;

	public MemberAccess(Expression object, boolean optional, Span name, boolean whole) {
		super(name);
		this.object = object;
		this.name = name;
		this.optional = optional;
		this.whole = whole;
	}

	public boolean isWhole() {
		return whole;
	}

	public boolean isOptional() {
		return optional;
	}

	public Expression getObject() {
		return object;
	}

	public Span getName() {
		return name;
	}

	@Override
	public List<Span> visitSpan() {
		return mergeSpans(object, name);
	}

	@Override
	public void visitMethod(MagicScriptCompiler compiler) {
		object.visitMethod(compiler);
	}

	@Override
	public void compile(MagicScriptCompiler compiler) {
		compiler.visit(object)    // 访问目标对象
				.ldc(name.getText())    // 成员名
				.insn(optional ? ICONST_1 : ICONST_0)    // 是否可空调用 ?.
				.asBoolean()
				.call("member_access", 3);
	}

	public void compileLinq(MagicScriptCompiler compiler) {
		compiler.visit(object)    // 访问目标对象
				.ldc(name.getText())    // 成员名
				.insn(optional ? ICONST_1 : ICONST_0)    // 是否可空调用 ?.
				.asBoolean()
				.insn(ICONST_1)    // 是否可空调用 ?.
				.asBoolean()
				.call("member_access", 4);
	}

	@Override
	public void compile_visit_variable(MagicScriptCompiler compiler) {
		compiler.compile(object).ldc(name.getText());
	}
}