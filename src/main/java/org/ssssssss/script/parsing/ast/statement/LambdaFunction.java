package org.ssssssss.script.parsing.ast.statement;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.compile.Descriptor;
import org.ssssssss.script.compile.MagicScriptCompiler;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.VarIndex;
import org.ssssssss.script.parsing.ast.Expression;
import org.ssssssss.script.parsing.ast.Node;

import java.util.List;

public class LambdaFunction extends Expression {
	private final List<VarIndex> parameters;
	private final List<Node> childNodes;
	private String methodName;

	private boolean async;

	public LambdaFunction(Span span, List<VarIndex> parameters, List<Node> childNodes) {
		super(span);
		this.parameters = parameters;
		this.childNodes = childNodes;
	}

	@Override
	public void visitMethod(MagicScriptCompiler compiler) {
		childNodes.forEach(it -> it.visitMethod(compiler));
		this.methodName = (async ? "async_": "") + "lambda_" + compiler.getFunctionIndex();
		compiler.createMethod(ACC_PRIVATE, methodName, Descriptor.make_descriptor(Object.class, MagicScriptContext.class,Object[].class))
				.load1()
				.load2()
				// 构建参数
				.visitInt(parameters.size())
				.intInsn(NEWARRAY, T_INT);
		for (int i = 0; i < parameters.size(); i++) {
			compiler.insn(DUP)
					.visitInt(i)
					.visitInt(parameters.get(i).getIndex())
					.insn(IASTORE);
		}
		compiler.invoke(INVOKEVIRTUAL, MagicScriptContext.class, "copy", Object[].class, Object[].class, int[].class)
				.store(2)
				.compile(childNodes)
				.pop();
	}

	public void setAsync(boolean async) {
		this.async = async;
	}

	public List<VarIndex> getParameters() {
		return parameters;
	}

	private void compileMethod(MagicScriptCompiler compiler){
		compiler.load0()
				.lambda(methodName);
	}

	@Override
	public void compile(MagicScriptCompiler compiler) {
		compileMethod(compiler);
	}
}
