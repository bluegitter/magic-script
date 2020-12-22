package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;

/**
 * 节点
 */
public abstract class Node {
    /**
     * 对应的文本
     */
    private final Span span;

	/**
	 * 在Linq中
	 */
	private boolean inLinq;

    public Node(Span span) {
        this.span = span;
    }

    public Span getSpan() {
        return span;
    }

	public boolean isInLinq() {
		return inLinq;
	}

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + span.getText();
    }

    public abstract Object evaluate(MagicScriptContext context, Scope scope);

	public Object evaluate(MagicScriptContext context, Scope scope, boolean inLinq) {
		this.inLinq = inLinq;
		return evaluate(context, scope);
	}

}