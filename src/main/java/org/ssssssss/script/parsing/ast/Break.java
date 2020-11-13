package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;

/**
 * break 语句
 */
public class Break extends Node {
	public static final Object BREAK_SENTINEL = new Object();

	public Break(Span span) {
		super(span);
	}

	@Override
	public Object evaluate(MagicScriptContext context, Scope scope) {
		return BREAK_SENTINEL;
	}
}