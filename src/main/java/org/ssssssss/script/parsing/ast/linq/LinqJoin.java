package org.ssssssss.script.parsing.ast.linq;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.compile.Descriptor;
import org.ssssssss.script.compile.MagicScriptCompiler;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Expression;
import org.ssssssss.script.runtime.function.MagicScriptLambdaFunction;
import org.ssssssss.script.runtime.linq.LinQBuilder;

public class LinqJoin extends Expression {

	private final LinqField target;

	private final Expression condition;

	private final boolean leftJoin;

	private String methodName;

	public LinqJoin(Span span, boolean leftJoin, LinqField target, Expression condition) {
		super(span);
		this.leftJoin = leftJoin;
		this.target = target;
		this.condition = condition;
	}

	@Override
	public void visitMethod(MagicScriptCompiler compiler) {
		// private Object methodName(MagicScriptContext, Object[])
		this.methodName = "linq_join_condition_" + compiler.getFunctionIndex();
		compiler.createMethod(ACC_PRIVATE, methodName, Descriptor.make_descriptor(Object.class, MagicScriptContext.class, Object[].class))
				.load1()    // MagicScriptContext
				.load2()    // 传入的参数
				// 构建参数
				.visitInt(0)
				.intInsn(NEWARRAY, T_INT);    // new int[parameters.size()]
		// 复制变量
		compiler.invoke(INVOKEVIRTUAL, MagicScriptContext.class, "copy", Object[].class, Object[].class, int[].class)
				.store(2)
				.compile(condition)
				.insn(ARETURN)
				.pop();
	}

	public LinqField getTarget() {
		return target;
	}

	@Override
	public void compile(MagicScriptCompiler compiler) {
		compiler.load0()
				.lambda(methodName)
				.visit(target.getExpression())
				.insn(leftJoin ? ICONST_1 : ICONST_0)
				.ldc(target.getAlias())
				.visitInt(target.getVarIndex() == null ? -1 : target.getVarIndex().getIndex())
				.invoke(INVOKEVIRTUAL, LinQBuilder.class, "join", LinQBuilder.class, MagicScriptLambdaFunction.class, Object.class, boolean.class, String.class, int.class);
	}

}
