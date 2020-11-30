package org.ssssssss.script.parsing.ast.literal;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.parsing.RegexpToken;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Literal;

import java.util.regex.Pattern;

/**
 * 正则常量
 */
public class RegexpLiteral extends Literal {

    public RegexpLiteral(Span span, Object value) {
        super(span);
        int flag = ((RegexpToken) value).getFlag();
        int i = flag & Pattern.CASE_INSENSITIVE;
        int m = flag & Pattern.MULTILINE;
        int s = flag & Pattern.DOTALL;
        int u = flag & Pattern.UNICODE_CHARACTER_CLASS;

        int f = 0;
        f |= i;
        f |= m;
        f |= s;
        f |= u;
        setValue(Pattern.compile(span.getText()
                .replaceAll("^/", "")
                .replaceAll("/[gismuy]*?$", ""), f));
    }

}
