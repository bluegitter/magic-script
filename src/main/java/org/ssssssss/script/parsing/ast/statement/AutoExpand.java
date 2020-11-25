package org.ssssssss.script.parsing.ast.statement;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Expression;

public class AutoExpand extends Expression {


    private Expression target;

    public AutoExpand(Span span) {
        super(span);
    }

    @Override
    public Object evaluate(MagicScriptContext context, Scope scope) {
        return this.target;
    }

    public void setTarget(Expression target) {
        this.target = target;
    }

    public Expression getTarget() {
        return target;
    }
}
