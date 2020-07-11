package org.ssssssss.script.parsing.ast.literal;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Literal;

/**
 * long 常量
 */
public class LongLiteral extends Literal {
    private Long value;

    public LongLiteral(Span literal) {
        super(literal);
        try {
            this.value = Long.parseLong(literal.getText().substring(0, literal.getText().length() - 1));
        } catch (NumberFormatException e) {
            MagicScriptError.error("定义long变量值不合法", literal, e);
        }
    }

    @Override
    public Object evaluate(MagicScriptContext context) {
        return value;
    }
}