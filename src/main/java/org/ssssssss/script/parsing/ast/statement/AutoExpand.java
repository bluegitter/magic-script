package org.ssssssss.script.parsing.ast.statement;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Expression;

public class AutoExpand extends Expression {


    private Expression target;

    private Span fullSpan;

    public AutoExpand(Span span) {
        super(span);
    }
    public AutoExpand(Span span, Expression target) {
        super(span);
        this.target = target;
    }

    @Override
    public Object evaluate(MagicScriptContext context, Scope scope) {
        MagicScriptError.error("只能在 list 或 map 上展开", this.getFullSpan());
        return null;
    }

    public void setTarget(Expression target) {
        this.target = target;
    }

    public Expression getTarget() {
        return target;
    }

    public Span getFullSpan() {
        if (fullSpan == null && this.getTarget() != null) {
            fullSpan = new Span(this.getSpan(), this.getTarget().getSpan());
        }
        return fullSpan;
    }
}
