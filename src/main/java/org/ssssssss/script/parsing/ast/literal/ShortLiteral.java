package org.ssssssss.script.parsing.ast.literal;

import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Literal;

/**
 * short 常量
 */
public class ShortLiteral extends Literal {

    public ShortLiteral(Span literal) {
        super(literal);
        try {
            setValue(Short.parseShort(literal.getText().substring(0, literal.getText().length() - 1)));
        } catch (NumberFormatException e) {
            MagicScriptError.error("定义short变量值不合法", literal, e);
        }
    }
}