package org.ssssssss.script.parsing.ast.statement;

import org.ssssssss.script.MagicModuleLoader;
import org.ssssssss.script.MagicPackageLoader;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.VarIndex;
import org.ssssssss.script.parsing.ast.Expression;
import org.ssssssss.script.parsing.ast.VariableSetter;

public class VariableAccess extends Expression implements VariableSetter {

    private final VarIndex varIndex;

    public VariableAccess(Span name, VarIndex varIndex) {
        super(name);
        this.varIndex = varIndex;
    }

    @Override
    public Object evaluate(MagicScriptContext context, Scope scope) {
        Object value = scope.getValue(varIndex);
        value = value == null ? MagicPackageLoader.findClass(varIndex.getName()) : value;
        return value == null ? MagicModuleLoader.loadModule(varIndex.getName()) : value;
    }

    public VarIndex getVarIndex() {
        return varIndex;
    }

	@Override
	public void setValue(MagicScriptContext context, Scope scope, Object value) {
		scope.setValue(varIndex, value);
	}
}