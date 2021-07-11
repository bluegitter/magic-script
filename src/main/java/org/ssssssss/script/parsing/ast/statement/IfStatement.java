package org.ssssssss.script.parsing.ast.statement;

import org.ssssssss.script.asm.Label;
import org.ssssssss.script.compile.MagicScriptCompiler;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Expression;
import org.ssssssss.script.parsing.ast.Node;
import org.ssssssss.script.runtime.handle.OperatorHandle;

import java.util.List;

public class IfStatement extends Node {
	private final Expression condition;
	private final List<Node> trueBlock;
	private final List<IfStatement> elseIfs;
	private final List<Node> falseBlock;

	public IfStatement(Span span, Expression condition, List<Node> trueBlock, List<IfStatement> elseIfs, List<Node> falseBlock) {
		super(span);
		this.condition = condition;
		this.trueBlock = trueBlock;
		this.elseIfs = elseIfs;
		this.falseBlock = falseBlock;
	}

	@Override
	public void visitMethod(MagicScriptCompiler compiler) {
		condition.visitMethod(compiler);
		trueBlock.forEach(it -> it.visitMethod(compiler));
		elseIfs.forEach(it -> it.visitMethod(compiler));
		falseBlock.forEach(it -> it.visitMethod(compiler));
	}

	@Override
	public void compile(MagicScriptCompiler compiler) {
		Label end = new Label();
		Label next = new Label();
		compiler.compile(condition)
				.invoke(INVOKESTATIC, OperatorHandle.class, "isTrue", boolean.class, Object.class)
				.jump(IFEQ, next)
				.compile(trueBlock)
				.jump(GOTO, end);
		for (IfStatement elseIf : elseIfs) {
			compiler.label(next)
					.compile(elseIf.condition)
					.invoke(INVOKESTATIC, OperatorHandle.class, "isTrue", boolean.class, Object.class);
			next = new Label();
			compiler.jump(IFEQ, next)
					.compile(elseIf.trueBlock)
					.jump(GOTO, end);
		}
		compiler.label(next);
		if (!falseBlock.isEmpty()) {
			compiler.compile(falseBlock);
		}
		compiler.label(end);
	}


}