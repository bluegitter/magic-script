package org.ssssssss.script.parsing.ast.statement;

import org.ssssssss.script.MagicResourceLoader;
import org.ssssssss.script.compile.MagicScriptCompiler;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.VarIndex;
import org.ssssssss.script.parsing.ast.Node;

public class Import extends Node {

	private String packageName;

	private final VarIndex varIndex;

	private final boolean module;

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
	public void compile(MagicScriptCompiler compiler) {
		String methodName = "loadClass";
		if (this.module) {
			methodName = "loadModule";
		} else if (this.function) {
			methodName = "loadFunction";
		}
		compiler.pre_store(varIndex)
				.ldc(packageName)
				.invoke(INVOKESTATIC, MagicResourceLoader.class, methodName, Object.class, String.class)
				.store();
	}
}
