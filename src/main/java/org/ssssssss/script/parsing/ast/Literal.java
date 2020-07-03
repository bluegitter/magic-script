package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.parsing.Span;

public abstract class Literal extends Expression{

	public Literal(Span span) {
		super(span);
	}
}
