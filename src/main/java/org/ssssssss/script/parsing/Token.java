package org.ssssssss.script.parsing;

public class Token {

    private final TokenType type;

    private final Span span;

    private TokenStream tokenStream;

    public Token(TokenType type, Span span) {
        this.type = type;
        this.span = span;
    }

    public Token(TokenType type, Span span, TokenStream tokenStream) {
        this.type = type;
        this.span = span;
        this.tokenStream = tokenStream;
    }

    public TokenType getType() {
        return type;
    }

    public Span getSpan() {
        return span;
    }

    public String getText() {
        return span.getText();
    }

    @Override
    public String toString() {
        return "Token [type=" + type + ", span=" + span + "]";
    }

    public TokenStream getTokenStream() {
        return tokenStream;
    }
}
