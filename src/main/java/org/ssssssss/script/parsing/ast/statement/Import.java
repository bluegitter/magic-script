package org.ssssssss.script.parsing.ast.statement;

import org.ssssssss.script.MagicResourceLoader;
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

	private boolean function;

	public Import(Span span, String packageName, VarIndex varIndex, boolean module) {
		super(span);
		this.packageName = packageName;
		this.varIndex = varIndex;
		this.module = module;
		if (!module && packageName.startsWith("@")) {
			function = true;
			this.packageName = packageName.substring(1);
		}
	}

	@Override
	public Object evaluate(MagicScriptContext context, Scope scope) {
		Object target;
		if (this.module) {
			target = MagicResourceLoader.loadModule(packageName);
			if (target == null) {
				throw new ModuleNotFoundException(String.format("module [%s] not found.", this.packageName), getSpan());
			}
		} else if (this.function) {
			target = MagicResourceLoader.loadFunction(packageName);
			if (target == null) {
				throw new ModuleNotFoundException(String.format("function [%s] not found.", this.packageName), getSpan());
			}
		} else {
			target = MagicResourceLoader.loadClass(packageName);
			if (target == null) {
				throw new ModuleNotFoundException(String.format("class [%s] not found.", this.packageName), getSpan());
			}
		}
		scope.setValue(varIndex, target);
		return null;
	}

	public boolean isFunction() {
		return function;
	}
}
