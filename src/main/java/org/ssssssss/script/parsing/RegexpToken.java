package org.ssssssss.script.parsing;

public class RegexpToken extends Token {

    public RegexpToken(TokenType type, Span span, int regFlag) {
        super(type, span);
        this.flag = regFlag;
    }
    private int flag;

    public int getFlag() {
        return flag;
    }
}
