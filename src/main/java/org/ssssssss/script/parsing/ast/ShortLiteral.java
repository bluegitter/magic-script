package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.parsing.Span;

public class ShortLiteral extends Literal {
    private final Short value;

    public ShortLiteral(Span literal) {
        super(literal);
        this.value = Short.parseShort(literal.getText().substring(0, literal.getText().length() - 1));
    }

    public Short getValue() {
        return value;
    }

    @Override
    public Object evaluate(MagicScriptContext context) {
        return value;
    }
}