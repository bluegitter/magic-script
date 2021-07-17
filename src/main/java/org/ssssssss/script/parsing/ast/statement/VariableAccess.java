package org.ssssssss.script.parsing.ast.statement;

import org.ssssssss.script.MagicResourceLoader;
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
		value = value == null ? context.getImportClass(varIndex.getName()) : value;
        value = value == null ? MagicResourceLoader.findClass(varIndex.getName()) : value;
        return value == null ? MagicResourceLoader.loadModule(varIndex.getName()) : value;
    }

    public VarIndex getVarIndex() {
        return varIndex;
    }

	@Override
	public void setValue(MagicScriptContext context, Scope scope, Object value) {
		scope.setValue(varIndex, value);
	}
}