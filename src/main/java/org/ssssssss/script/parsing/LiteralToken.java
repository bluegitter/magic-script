package org.ssssssss.script.parsing;

public class LiteralToken extends Token {
	public LiteralToken(TokenType type, Span span) {
		super(type, span);
	}

	public LiteralToken(TokenType type, Span span, TokenStream tokenStream) {
		super(type, span, tokenStream);
	}
}
