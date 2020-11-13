package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicModuleLoader;
import org.ssssssss.script.MagicPackageLoader;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.VarNode;
import org.ssssssss.script.parsing.Span;

public class VariableAccess extends Expression {

    private final VarNode varNode;

    public VariableAccess(Span name, VarNode varNode) {
        super(name);
        this.varNode = varNode;
    }

    public VarNode getVarNode() {
        return varNode;
    }

    @Override
    public Object evaluate(MagicScriptContext context) {
        return varNode.getValue(context);
    }

}