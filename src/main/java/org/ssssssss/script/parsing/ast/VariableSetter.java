package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.parsing.Scope;

public interface VariableSetter {
	public void setValue(MagicScriptContext context, Scope scope, Object value);
}