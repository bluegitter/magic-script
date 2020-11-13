package org.ssssssss.script.parsing.ast.literal;

import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Literal;

/**
 * null 常量
 */
public class NullLiteral extends Literal {
    public NullLiteral(Span span) {
        super(span);
    }
}