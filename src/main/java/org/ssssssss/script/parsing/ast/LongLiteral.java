package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.parsing.Span;

public class LongLiteral extends Literal {
    private final Long value;

    public LongLiteral(Span literal) {
        super(literal);
        this.value = Long.parseLong(literal.getText().substring(0, literal.getText().length() - 1));
    }

    public Long getValue() {
        return value;
    }

    @Override
    public Object evaluate(MagicScriptContext context) {
        return value;
    }
}