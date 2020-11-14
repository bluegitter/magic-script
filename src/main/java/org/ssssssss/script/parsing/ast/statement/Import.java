package org.ssssssss.script.parsing.ast.statement;

import org.ssssssss.script.MagicModuleLoader;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.exception.ModuleNotFoundException;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.VarIndex;
import org.ssssssss.script.parsing.ast.Node;

public class Import extends Node {

	private String packageName;

	private VarIndex varIndex;

	private boolean module;

	public Import(Span span, String packageName, VarIndex varIndex, boolean module) {
		super(span);
		this.packageName = packageName;
		this.varIndex = varIndex;
		this.module = module;
	}

	@Override
	public Object evaluate(MagicScriptContext context, Scope scope) {
        if (this.module) {
            Object target = MagicModuleLoader.loadModule(packageName);
            if (target == null) {
				throw new ModuleNotFoundException(String.format("module [%s] not found.", this.packageName), getSpan());
            }
			scope.setValue(varIndex, target);
        }else{
            Object target = MagicModuleLoader.loadClass(packageName);
            if (target == null) {
				throw new ModuleNotFoundException(String.format("class [%s] not found.", this.packageName), getSpan());
            }
			scope.setValue(varIndex, target);
        }
		return null;
	}
}
