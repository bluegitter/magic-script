package org.ssssssss.script.parsing;

import java.util.Arrays;

/**
 * Enumeration of token types. A token type consists of a representation for error messages, and may optionally specify a literal
 * to be used by the {@link CharacterStream} to recognize the token. Token types are sorted by their literal length to easy
 * matching of token types with common prefixes, e.g. "<" and "<=". Token types with longer literals are matched first.
 */
public enum TokenType {
	// @off
	Spread("...", "..."),
	Period(".", "."),
	QuestionPeriod("?.", "?."),
	Comma(",", ","),
	Semicolon(";", ";"),
	Colon(":", ":"),
	Plus("+", "+"),
	Minus("-", "-"),
	Asterisk("*", "*"),
	ForwardSlash("/", "/"),
	PostSlash("\\", "\\"),
	Percentage("%", "%"),
	LeftParantheses("(", ")"),
	RightParantheses(")", ")"),
	LeftBracket("[", "["),
	RightBracket("]", "]"),
	LeftCurly("{", "{"),
	RightCurly("}"), // special treatment!
	Less("<", "<"),
	Greater(">", ">"),
	LessEqual("<=", "<="),
	GreaterEqual(">=", ">="),
	Equal("==", "=="),
	NotEqual("!=", "!="),
	Assignment("=", "="),
	// 1.3.0
	PlusPlus("++", "++"),
	MinusMinus("--", "--"),
	PlusEqual("+=", "+="),
	MinusEqual("-=", "-="),
	AsteriskEqual("*=", "*="),
	ForwardSlashEqual("/=", "/="),
	PercentEqual("%=", "%="),
	// 1.3.0 end
	// 1.3.9
	ColonColon("::", "::"),
	EqualEqualEqual("===", "==="),
	NotEqualEqual("!==", "!=="),
	// 1.3.9 end
	And("&&", "&&"),
	Or("||", "||"),
	Xor("^", "^"),
	Not("!", "!"),

	SqlAnd("and", "and", true),
	SqlOr("or", "or", true),
	SqlNotEqual("<>", "<>", true),

	Questionmark("?", "?"),
	DoubleQuote("\"", "\""),
	SingleQuote("'", "'"),
	Lambda("=>"),
	RegexpLiteral("a regexp"),
	BooleanLiteral("true or false"),
	DoubleLiteral("a double floating point number"),
	DecimalLiteral("a decimal point number"),
	FloatLiteral("a floating point number"),
	LongLiteral("a long integer number"),
	IntegerLiteral("an integer number"),
	ShortLiteral("a short integer number"),
	ByteLiteral("a byte integer number"),
	CharacterLiteral("a character"),
	StringLiteral("a string"),
	NullLiteral("null"),
	Language("language"),
	Identifier("an identifier");
	// @on

	private static TokenType[] values;

	static {
		values = TokenType.values();
		Arrays.sort(values, (o1, o2) -> {
			if (o1.literal == null && o2.literal == null) {
				return 0;
			}
			if (o1.literal == null && o2.literal != null) {
				return 1;
			}
			if (o1.literal != null && o2.literal == null) {
				return -1;
			}
			return o2.literal.length() - o1.literal.length();
		});
	}

	private final String literal;
	private final String error;
	private final boolean inLinq;

	TokenType(String error) {
		this(null, error, false);
	}

	TokenType(String literal, String error) {
		this(literal, error, false);
	}

	TokenType(String literal, String error, boolean inLinq) {
		this.literal = literal;
		this.error = error;
		this.inLinq = inLinq;
	}

	public boolean isInLinq() {
		return inLinq;
    }

    public static TokenType[] getSortedValues() {
		return values;
	}


	public String getLiteral() {
		return literal;
	}


	public String getError() {
		return error;
	}
}
