package org.ssssssss.script.parsing.ast.literal;

import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Literal;

/**
 * long 常量
 */
public class LongLiteral extends Literal {

    public LongLiteral(Span literal) {
        super(literal);
        try {
            setValue(Long.parseLong(literal.getText().substring(0, literal.getText().length() - 1)));
        } catch (NumberFormatException e) {
            MagicScriptError.error("定义long变量值不合法", literal, e);
        }
    }
}