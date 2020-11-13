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

    public Node(Span span) {
        this.span = span;
    }

    public Span getSpan() {
        return span;
    }

    @Override
    public String toString() {
        return span.getText();
    }

    /**
     *
     * @param context
     * @return
     */
    public abstract Object evaluate(MagicScriptContext context, Scope scope);

}