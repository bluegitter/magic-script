package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.parsing.Span;

public class ShortLiteral extends Literal {
    private Short value;

    public ShortLiteral(Span literal) {
        super(literal);
        try {
            this.value = Short.parseShort(literal.getText().substring(0, literal.getText().length() - 1));
        } catch (NumberFormatException e) {
            MagicScriptError.error("定义short变量值不合法", literal, e);
        }
    }

    public Short getValue() {
        return value;
    }

    @Override
    public Object evaluate(MagicScriptContext context) {
        return value;
    }
}