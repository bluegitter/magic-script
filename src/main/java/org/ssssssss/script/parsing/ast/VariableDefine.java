package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.VarIndex;

public class VariableDefine extends Node {

    private Expression right;

	private VarIndex varIndex;

	public VariableDefine(Span span, VarIndex varIndex, Expression right) {
        super(span);
		this.varIndex = varIndex;
        this.right = right;
    }

    @Override
	public Object evaluate(MagicScriptContext context, Scope scope) {
		scope.setValue(varIndex, right.evaluate(context, scope));
        return null;
    }
}
