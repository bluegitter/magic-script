package org.ssssssss.script.parsing.ast.literal;

import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Literal;

/**
 * float常量
 */
public class FloatLiteral extends Literal {

    public FloatLiteral(Span literal) {
        super(literal);
        try {
            setValue(Float.parseFloat(literal.getText().substring(0, literal.getText().length() - 1)));
        } catch (NumberFormatException e) {
            MagicScriptError.error("定义float变量值不合法", literal, e);
        }
    }
}