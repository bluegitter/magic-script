package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.asm.Label;
import org.ssssssss.script.compile.MagicScriptCompiler;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.runtime.handle.OperatorHandle;

public class TernaryOperation extends Expression {
	private final Expression condition;
	private final Expression trueExpression;
	private final Expression falseExpression;

	public TernaryOperation(Expression condition, Expression trueExpression, Expression falseExpression) {
		super(new Span(condition.getSpan(), falseExpression.getSpan()));
		this.condition = condition;
		this.trueExpression = trueExpression;
		this.falseExpression = falseExpression;
	}

	@Override
	public void visitMethod(MagicScriptCompiler compiler) {
		condition.visitMethod(compiler);
		trueExpression.visitMethod(compiler);
		falseExpression.visitMethod(compiler);
	}

	@Override
	public void compile(MagicScriptCompiler compiler) {
		Label end = new Label();
		Label falseValue = new Label();
		compiler.compile(condition)
				.invoke(INVOKESTATIC, OperatorHandle.class, "isTrue", boolean.class, Object.class)
				.jump(IFEQ, falseValue)
				.compile(trueExpression)
				.jump(GOTO, end)
				.label(falseValue)
				.compile(falseExpression)
				.label(end);
	}
}