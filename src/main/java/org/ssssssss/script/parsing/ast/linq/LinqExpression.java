package org.ssssssss.script.parsing.ast.linq;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.compile.Descriptor;
import org.ssssssss.script.compile.MagicScriptCompiler;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Expression;
import org.ssssssss.script.parsing.ast.statement.MemberAccess;

public class LinqExpression extends Expression {

	private final Expression expression;

	private String methodName;

	public LinqExpression(Expression expression) {
		this(expression.getSpan(), expression);
	}

	public LinqExpression(Span span, Expression expression) {
		super(span);
		this.expression = expression;
	}

	@Override
	public void visitMethod(MagicScriptCompiler compiler) {
		expression.visitMethod(compiler);
		if(!(expression instanceof WholeLiteral)){
			this.methodName = "linq_expression_" + compiler.getFunctionIndex();
			compiler.createMethod(ACC_PRIVATE, methodName, Descriptor.make_descriptor(Object.class, MagicScriptContext.class, Object[].class))
					.load1()    // MagicScriptContext
					.load2()    // 传入的参数
					// 构建参数
					.visitInt(0)
					.intInsn(NEWARRAY, T_INT);    // new int[parameters.size()]
			// 复制变量
			compiler.invoke(INVOKEVIRTUAL, MagicScriptContext.class, "copy", Object[].class, Object[].class, int[].class)
					.store(2)
					.compile(expression instanceof MemberAccess && ((MemberAccess) expression).isWhole() ? ((MemberAccess) expression).getObject() : expression)
					.insn(ARETURN)
					.pop();
		}
	}

	@Override
	public void compile(MagicScriptCompiler compiler) {
		if(methodName != null){
			compiler.load0()
					.lambda(methodName);
		}else{
			compiler.insn(ACONST_NULL);
		}
	}

	public Expression getExpression() {
		return expression;
	}
}
