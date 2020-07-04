package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.parsing.Span;

public class DoubleLiteral extends Literal {
    private Double value;

    public DoubleLiteral(Span literal) {
        super(literal);
        try {
            this.value = Double.parseDouble(literal.getText().substring(0, literal.getText().length() - 1));
        } catch (NumberFormatException e) {
            MagicScriptError.error("定义double变量值不合法", literal, e);
        }
    }

    public Double getValue() {
        return value;
    }

    @Override
    public Object evaluate(MagicScriptContext context) {
        return value;
    }
}