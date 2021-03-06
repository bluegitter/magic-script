package org.ssssssss.script.parsing.ast.statement;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Expression;

import java.util.Collection;

/**
 * 展开语法 Spread syntax (...)
 */
public class Spread extends Expression {


    private Expression target;

    public Spread(Span span, Expression target) {
        super(span);
        this.target = target;
    }

    @Override
    public Object evaluate(MagicScriptContext context, Scope scope) {
        MagicScriptError.error("只能在 list/map/lambda调用参数 上展开", super.getSpan());
        return null;
    }

    public void setTarget(Expression target) {
        this.target = target;
    }

    public Expression getTarget() {
        return target;
    }

    public Object[] doSpread(MagicScriptContext context, Scope scope, boolean inLinq) {
        Object targetVal = getTarget().evaluate(context, scope, inLinq);
        if (targetVal instanceof Collection) {
            return ((Collection<?>) targetVal).toArray();
        } else {
            MagicScriptError.error("展开的不是一个集合", super.getSpan());
        }
        return null;
    }

}
