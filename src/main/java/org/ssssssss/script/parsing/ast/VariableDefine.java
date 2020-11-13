package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.VarNode;
import org.ssssssss.script.parsing.Span;

public class VariableDefine extends Node {

    private Expression right;

	private VarNode varNode;

	public VariableDefine(Span span, VarNode varNode, Expression right) {
        super(span);
		this.varNode = varNode;
        this.right = right;
    }

    @Override
    public Object evaluate(MagicScriptContext context) {
		varNode.setValue(context, right.evaluate(context));
        return null;
    }
}
