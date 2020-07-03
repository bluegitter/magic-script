package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.parsing.Span;

public class ByteLiteral extends Literal {
    private final Byte value;

    public ByteLiteral(Span literal) {
        super(literal);
        this.value = Byte.parseByte(literal.getText().substring(0, literal.getText().length() - 1));
    }

    public Byte getValue() {
        return value;
    }

    @Override
    public Object evaluate(MagicScriptContext context) {
        return value;
    }
}