package org.ssssssss.script.parsing.ast.statement;

import org.ssssssss.script.compile.MagicScriptCompiler;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.VarIndex;
import org.ssssssss.script.parsing.ast.Expression;
import org.ssssssss.script.parsing.ast.Node;

import java.util.Collections;
import java.util.List;

public class VariableDefine extends Node {

	private final Expression right;

	private final VarIndex varIndex;

	public VariableDefine(Span span, VarIndex varIndex, Expression right) {
		super(span);
		this.varIndex = varIndex;
		this.right = right;
	}

	@Override
	public List<Span> visitSpan() {
		if (right == null) {
			return Collections.emptyList();
		}
		return right.visitSpan();
	}

	@Override
	public void visitMethod(MagicScriptCompiler compiler) {
		if (right != null) {
			right.visitMethod(compiler);
		}
	}

	@Override
	public void compile(MagicScriptCompiler compiler) {
		compiler.pre_store(varIndex)
				.visit(right)    // 读取变量值
				.store();    // 保存变量
	}

}
