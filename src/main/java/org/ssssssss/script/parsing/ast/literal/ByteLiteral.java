package org.ssssssss.script.parsing.ast.literal;

import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Literal;

/**
 * byte常量
 */
public class ByteLiteral extends Literal {

    public ByteLiteral(Span literal) {
        super(literal);
        try {
            setValue(Byte.parseByte(literal.getText().substring(0, literal.getText().length() - 1)));
        } catch (NumberFormatException e) {
            MagicScriptError.error("定义byte变量值不合法", literal, e);
        }
    }
}