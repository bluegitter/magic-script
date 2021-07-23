package org.ssssssss.script.parsing;

import java.util.Arrays;

/**
 * Token类型
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
	RightCurly("}", "{"),
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
	// 1.5.0 start
	BitAnd("&", "&"),
	BitOr("|", "|"),
	BitNot("~", "~"),
	LShift("<<", "<<"),
	RShift(">>", ">>"),
	Rshift2(">>>", ">>>"),

	XorEqual("^=", "^="),
	BitAndEqual("&=", "&="),
	BitOrEqual("|=", "|="),
	LShiftEqual("<<=", "<<="),
	RShiftEqual(">>=", ">>="),
	RShift2Equal(">>>=", ">>>="),
	// 1.5.0 end
	SqlAnd("and", "and", true),
	SqlOr("or", "or", true),
	SqlNotEqual("<>", "<>", true),

	QuestionMark("?", "?"),
	DoubleQuote("\"", "\""),
	TripleQuote("\"\"\"", "\"\"\""),
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
	Comment("comment"),
	Identifier("an identifier");
	// @on

	private static final TokenType[] values;

	static {
		values = TokenType.values();
		// 根据字符长度排序
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

	public static TokenType[] getSortedValues() {
		return values;
	}

	public boolean isInLinq() {
		return inLinq;
	}

	public String getLiteral() {
		return literal;
	}


	public String getError() {
		return error;
	}
}
