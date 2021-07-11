package org.ssssssss.script.runtime;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.parsing.Span;

import java.util.List;

public abstract class MagicScriptRuntime {

	private String[] varNames;

	private List<Span> spans;

	public abstract Object execute(MagicScriptContext context);

	public String[] getVarNames() {
		return varNames;
	}

	public void setVarNames(String[] varNames) {
		this.varNames = varNames;
	}

	public void setSpans(List<Span> spans) {
		this.spans = spans;
	}

	public Span getSpan(int index){
		return spans.get(index);
	}
}
