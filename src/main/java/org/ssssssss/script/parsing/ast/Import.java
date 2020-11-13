package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicModuleLoader;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.VarNode;
import org.ssssssss.script.exception.ModuleNotFoundException;
import org.ssssssss.script.parsing.Span;

public class Import extends Node {

	private String packageName;

	private VarNode varNode;

	private boolean module;

	public Import(Span span, String packageName, VarNode varNode, boolean module) {
		super(span);
		this.packageName = packageName;
		this.varNode = varNode;
		this.module = module;
	}

	@Override
	public Object evaluate(MagicScriptContext context) {
        if (this.module) {
            Object target = MagicModuleLoader.loadModule(packageName);
            if (target == null) {
				throw new ModuleNotFoundException(String.format("module [%s] not found.", this.packageName), getSpan());
            }
			varNode.setValue(context, target);
        }else{
            Object target = MagicModuleLoader.loadClass(packageName);
            if (target == null) {
				throw new ModuleNotFoundException(String.format("class [%s] not found.", this.packageName), getSpan());
            }
			varNode.setValue(context, target);
        }
		return null;
	}
}
