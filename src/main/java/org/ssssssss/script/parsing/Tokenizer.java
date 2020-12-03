package org.ssssssss.script.parsing;

import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.exception.StringLiteralException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


public class Tokenizer {

	public static TokenStream tokenize(String source) {
		CharacterStream stream = new CharacterStream(source, 0, source.length());
		List<Token> tokens = new ArrayList<Token>();
		int leftCount = 0;
		int rightCount = 0;
		outer:
		while (stream.hasMore()) {
			stream.skipWhiteSpace();
			if (stream.match("//", true)) {    //注释
				stream.skipLine();
				continue;
			}
			if (stream.match("/*", true)) {    //多行注释
				stream.skipUntil("*/");
				continue;
			}
			if (stream.matchDigit(false)) {
				TokenType type = TokenType.IntegerLiteral;
				stream.startSpan();
				while (stream.matchDigit(true)) {
					;
				}
				if (stream.match(TokenType.Period.getLiteral(), true)) {
					type = TokenType.DoubleLiteral;
					while (stream.matchDigit(true)) {
						;
					}
				}
				if (stream.match("b", true) || stream.match("B", true)) {
					if (type == TokenType.DoubleLiteral) {
						MagicScriptError.error("Byte literal can not have a decimal point.", stream.endSpan());
					}
					type = TokenType.ByteLiteral;
				} else if (stream.match("s", true) || stream.match("S", true)) {
					if (type == TokenType.DoubleLiteral) {
						MagicScriptError.error("Short literal can not have a decimal point.", stream.endSpan());
					}
					type = TokenType.ShortLiteral;
				} else if (stream.match("l", true) || stream.match("L", true)) {
					if (type == TokenType.DoubleLiteral) {
						MagicScriptError.error("Long literal can not have a decimal point.", stream.endSpan());
					}
					type = TokenType.LongLiteral;
				} else if (stream.match("f", true) || stream.match("F", true)) {
					type = TokenType.FloatLiteral;
				} else if (stream.match("d", true) || stream.match("D", true)) {
					type = TokenType.DoubleLiteral;
				} else if (stream.match("m", true) || stream.match("M", true)) {
					type = TokenType.DecimalLiteral;
				}
				Span numberSpan = stream.endSpan();
				tokens.add(new LiteralToken(type, numberSpan));
				continue;
			}

			// String literal
			if (stream.match(TokenType.SingleQuote.getLiteral(), true)) {
				stream.startSpan();
				boolean matchedEndQuote = false;
				while (stream.hasMore()) {
					// Note: escape sequences like \n are parsed in StringLiteral
					if (stream.match("\\", true)) {
						stream.consume();
						continue;
					}
					if (stream.match(TokenType.SingleQuote.getLiteral(), true)) {
						matchedEndQuote = true;
						break;
					}
					char ch = stream.consume();
					if (ch == '\r' || ch == '\n') {
						MagicScriptError.error("''定义的字符串不能换行", stream.endSpan(), new StringLiteralException());
					}
				}
				if (!matchedEndQuote) {
					MagicScriptError.error("字符串没有结束符\'", stream.endSpan(), new StringLiteralException());
				}
				Span stringSpan = stream.endSpan();
				stringSpan = stream.getSpan(stringSpan.getStart() - 1, stringSpan.getEnd());
				tokens.add(new LiteralToken(TokenType.StringLiteral, stringSpan));
				continue;
			}

			// String literal
			if (stream.match("\"\"\"", true)) {
				stream.startSpan();
				boolean matchedEndQuote = false;
				while (stream.hasMore()) {
					// Note: escape sequences like \n are parsed in StringLiteral
					if (stream.match("\\", true)) {
						stream.consume();
						continue;
					}
					if (stream.match("\"\"\"", true)) {
						matchedEndQuote = true;
						break;
					}
					stream.consume();
				}
				if (!matchedEndQuote) {
					MagicScriptError.error("多行字符串没有结束符\"\"\"", stream.endSpan(), new StringLiteralException());
				}
				Span stringSpan = stream.endSpan();
				stringSpan = stream.getSpan(stringSpan.getStart() - 1, stringSpan.getEnd() - 2);
				tokens.add(new LiteralToken(TokenType.StringLiteral, stringSpan));
				continue;
			}

			// String literal
			if (stream.match(TokenType.DoubleQuote.getLiteral(), true)) {
				stream.startSpan();
				boolean matchedEndQuote = false;
				while (stream.hasMore()) {
					// Note: escape sequences like \n are parsed in StringLiteral
					if (stream.match("\\", true)) {
						stream.consume();
						continue;
					}
					if (stream.match(TokenType.DoubleQuote.getLiteral(), true)) {
						matchedEndQuote = true;
						break;
					}
					char ch = stream.consume();
					if (ch == '\r' || ch == '\n') {
						MagicScriptError.error("\"\"定义的字符串不能换行", stream.endSpan(), new StringLiteralException());
					}
				}
				if (!matchedEndQuote) {
					MagicScriptError.error("字符串没有结束符\"", stream.endSpan(), new StringLiteralException());
				}
				Span stringSpan = stream.endSpan();
				stringSpan = stream.getSpan(stringSpan.getStart() - 1, stringSpan.getEnd());
				tokens.add(new LiteralToken(TokenType.StringLiteral, stringSpan));
				continue;
			}
			// regexp
			if (regexpToken(stream, tokens)) {
				continue;
			}
			// Identifier, keyword, boolean literal, or null literal
			if (stream.matchIdentifierStart(true)) {
				stream.startSpan();
				while (stream.matchIdentifierPart(true)) {
					;
				}
				Span identifierSpan = stream.endSpan();
				identifierSpan = stream.getSpan(identifierSpan.getStart() - 1, identifierSpan.getEnd());
				if ("true".equals(identifierSpan.getText()) || "false".equals(identifierSpan.getText())) {
					tokens.add(new LiteralToken(TokenType.BooleanLiteral, identifierSpan));
				} else if ("null".equals(identifierSpan.getText())) {
					tokens.add(new LiteralToken(TokenType.NullLiteral, identifierSpan));
				} else {
					tokens.add(new Token(TokenType.Identifier, identifierSpan));
				}
				continue;
			}
			// Simple tokens
			for (TokenType t : TokenType.getSortedValues()) {
				if (t.getLiteral() != null) {
					if (stream.match(t.getLiteral(), true)) {
						if (t == TokenType.LeftCurly) {
							leftCount++;
						}
						tokens.add(new Token(t, stream.getSpan(stream.getPosition() - t.getLiteral().length(), stream.getPosition())));
						continue outer;
					}
				}
			}
			if (leftCount != rightCount && stream.match("}", true)) {
				rightCount++;
				tokens.add(new Token(TokenType.RightCurly, stream.getSpan(stream.getPosition() - 1, stream.getPosition())));
				continue outer;
			}
			if (stream.hasMore()) {
				MagicScriptError.error("Unknown token", stream.getSpan(stream.getPosition(), stream.getPosition() + 1));
			}
		}
		return new TokenStream(tokens);
	}

	private static boolean regexpToken(CharacterStream stream, List<Token> tokens) {
		if(tokens.size() > 0){
			Token token = tokens.get(tokens.size() - 1);
			if(token instanceof LiteralToken || token.getType() == TokenType.Identifier){
				return false;
			}
		}
		if (stream.match("/", false)) {
			int mark = stream.getPosition();
			stream.consume();
			stream.startSpan();
			boolean matchedEndQuote = false;
			int deep = 0;
			int expFlag = 0;
			int maybeMissForwardSlash = 0;
			int maybeMissForwardSlashEnd = 0;
			while (stream.hasMore()) {
				// Note: escape sequences like \n are parsed in StringLiteral
				if (stream.match("\\", true)) {
					stream.consume();
					continue;
				}
				if (stream.match("[", false)) {
					deep++;
					maybeMissForwardSlash = stream.getPosition();
				} else if (deep > 0 && stream.match("]", false)) {
					deep--;
				} else if (stream.match(TokenType.ForwardSlash.getLiteral(), true)) {
					if (deep == 0) {
						if (stream.match("g", true)) {
							expFlag |= 1;
						}
						if (stream.match("i", true)) {
							expFlag |= Pattern.CASE_INSENSITIVE;
						}
						if (stream.match("m", true)) {
							expFlag |= Pattern.MULTILINE;
						}
						if (stream.match("s", true)) {
							expFlag |= Pattern.DOTALL;
						}
						if (stream.match("u", true)) {
							expFlag |= Pattern.UNICODE_CHARACTER_CLASS;
						}
						if (stream.match("y", true)) {
							expFlag |= 16;
						}
						matchedEndQuote = true;
						break;
					} else {
						maybeMissForwardSlashEnd = stream.getPosition();
					}
				}
				char ch = stream.consume();
				if (ch == '\r' || ch == '\n') {
					stream.reset(mark);
					return false;
				}
			}
			if (deep != 0) {
				MagicScriptError.error("Missing ']'", stream.getSpan(maybeMissForwardSlash, maybeMissForwardSlashEnd - 1));
			}
			if (!matchedEndQuote) {
				stream.reset(mark);
				return false;
			}
			Span regexpSpan = stream.endSpan();
			regexpSpan = stream.getSpan(regexpSpan.getStart() - 1, regexpSpan.getEnd());
			tokens.add(new RegexpToken(TokenType.RegexpLiteral, regexpSpan, expFlag));
			return true;
		}
		return false;
	}
}
