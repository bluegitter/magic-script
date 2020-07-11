package org.ssssssss.script.parsing.ast.literal;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Literal;

/**
 * boolean常量
 */
public class BooleanLiteral extends Literal {
    private final Boolean value;

    public BooleanLiteral(Span literal) {
        super(literal);
        this.value = Boolean.parseBoolean(literal.getText());
    }

    @Override
    public Object evaluate(MagicScriptContext context) {
        return value;
    }
}