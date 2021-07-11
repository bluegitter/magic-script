package org.ssssssss.script.parsing.ast.statement;

import org.ssssssss.script.compile.MagicScriptCompiler;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Node;

public class Return extends Node {

	private final Node returnValue;

	public Return(Span span, Node returnValue) {
		super(span);
		this.returnValue = returnValue;
	}

	@Override
	public void visitMethod(MagicScriptCompiler compiler) {
		returnValue.visitMethod(compiler);
	}

	@Override
	public void compile(MagicScriptCompiler compiler) {
		if(returnValue == null){
			if(compiler.finallyBlock() != null){
				compiler.compile(compiler.getFinallyBlock());
			}
			compiler.insn(ACONST_NULL);
		}else{
			compiler.visit(returnValue);
			if(compiler.finallyBlock() != null){
				compiler.store(3)	// 保存返回结果
						.compile(compiler.getFinallyBlock())	// 执行 finally
						.load3();	// 加载返回结果
			}
		}
		compiler.insn(ARETURN);	 // 返回
	}
}