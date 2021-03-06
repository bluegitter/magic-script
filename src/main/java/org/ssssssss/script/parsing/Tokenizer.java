package org.ssssssss.script.parsing;

import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.exception.StringLiteralException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


public class Tokenizer {

	public static TokenStream tokenize(String source) {
		return tokenize(source, false);
	}

	public static TokenStream tokenize(String source, boolean matchComment) {
		CharacterStream stream = new CharacterStream(source, 0, source.length());
		List<Token> tokens = new ArrayList<>();
		tokenizer(stream, tokens, matchComment, null);
		return new TokenStream(tokens);
	}

	private static List<Token> tokenizer(CharacterStream stream, List<Token> tokens, boolean matchComment, String except) {
		int leftCount = 0;
		int rightCount = 0;
		outer:
		while (stream.hasMore()) {
			stream.skipWhiteSpace();
			stream.startSpan();
			// // /* */
			if (tokenizerComment(stream, tokens, matchComment)) {
				continue;
			}
			// int double long float byte decimal
			if (tokenizerNumber(stream, tokens)) {
				continue;
			}
			// template string
//			if (tokenizerTemplateString(stream, tokens, matchComment)) {
//				continue;
//			}
			// '' """ """ ""
			if (tokenizerString(stream, TokenType.SingleQuote, tokens) || tokenizerString(stream, TokenType.TripleQuote, tokens) || tokenizerString(stream, TokenType.DoubleQuote, tokens)) {
				continue;
			}

			// regexp
			if (regexpToken(stream, tokens)) {
				continue;
			}
			// ``` ```
			if (tokenizerLanguage(stream, tokens)) {
				continue;
			}
			// Identifier, keyword, boolean literal, or null literal
			if (tokenizerIdentifier(stream, tokens)) {
				continue;
			}
			// lambda
			if (stream.match("=>", true) || stream.match("->", true)) {
				tokens.add(new Token(TokenType.Lambda, stream.getSpan(stream.getPosition() - 2, stream.getPosition())));
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
		return tokens;
	}

	private static boolean tokenizerLanguage(CharacterStream stream, List<Token> tokens) {
		// TODO exception
		if (stream.match("```", true)) {
			stream.startSpan();
			if (stream.matchIdentifierStart(true)) {
				while (stream.matchIdentifierPart(true)) {
					;
				}
				Span language = stream.endSpan();
				tokens.add(new Token(TokenType.Language, language));
				stream.startSpan();
				if (!stream.skipUntil("```")) {
					MagicScriptError.error("```?????????```??????", stream.endSpan(), new StringLiteralException());
				}
				tokens.add(new Token(TokenType.Language, stream.endSpan(-3)));
				return true;
			} else {
				MagicScriptError.error("```???????????????????????????", stream.endSpan(), new StringLiteralException());
			}
		}
		return false;
	}

	private static boolean tokenizerTemplateString(CharacterStream stream, List<Token> tokens, boolean matchComment) {
		if (stream.match("`", true)) {
			int start = stream.getPosition();
			boolean matchedEndQuote = false;
			List<Token> subTokens = new ArrayList<>();
			while (stream.hasMore()) {
				if (stream.match("\\", true)) {
					stream.consume();
					continue;
				}
				if (stream.match("`", true)) {
					matchedEndQuote = true;
					break;
				}
				if (stream.match("${", true)) {
					int end = stream.getPosition();
					subTokens.add(new LiteralToken(TokenType.StringLiteral, stream.endSpan(start, end)));
					start = end;
					subTokens.addAll(tokenizer(stream, new ArrayList<>(), matchComment, "}"));
				}
			}
			if (!matchedEndQuote) {
				MagicScriptError.error("??????????????????????????????`", stream.endSpan(), new StringLiteralException());
			}
			Span stringSpan = stream.endSpan(start, stream.getPosition());
			stringSpan = stream.getSpan(stringSpan.getStart() - 1, stringSpan.getEnd());
			// tokens.add(new TemplateStringToken(stringSpan, subTokens));
			return true;
		}
		return false;
	}

	private static boolean tokenizerIdentifier(CharacterStream stream, List<Token> tokens) {
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
			} else if (TokenType.SqlAnd.getLiteral().equals(identifierSpan.getText())) {
				tokens.add(new Token(TokenType.SqlAnd, identifierSpan));
			} else if (TokenType.SqlOr.getLiteral().equals(identifierSpan.getText())) {
				tokens.add(new Token(TokenType.SqlOr, identifierSpan));
			} else {
				tokens.add(new Token(TokenType.Identifier, identifierSpan));
			}
			return true;
		}
		return false;
	}

	private static boolean tokenizerComment(CharacterStream stream, List<Token> tokens, boolean matchComment) {
		if (stream.match("//", true)) {    //??????
			stream.skipLine();
			if (matchComment) {
				tokens.add(new Token(TokenType.Comment, stream.endSpan()));
			}
			return true;
		}
		stream.startSpan();
		if (stream.match("/*", true)) {    //????????????
			stream.skipUntil("*/");
			if (matchComment) {
				tokens.add(new Token(TokenType.Comment, stream.endSpan()));
			}
			return true;
		}
		return false;
	}

	private static boolean tokenizerNumber(CharacterStream stream, List<Token> tokens) {
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
			return true;
		}
		return false;
	}

	private static boolean tokenizerString(CharacterStream stream, TokenType tokenType, List<Token> tokens) {
		// String literal
		if (stream.match(tokenType.getLiteral(), true)) {
			stream.startSpan();
			boolean matchedEndQuote = false;
			while (stream.hasMore()) {
				// Note: escape sequences like \n are parsed in StringLiteral
				if (stream.match("\\", true)) {
					stream.consume();
					continue;
				}
				if (stream.match(tokenType.getLiteral(), true)) {
					matchedEndQuote = true;
					break;
				}
				char ch = stream.consume();
				if (tokenType != TokenType.TripleQuote && (ch == '\r' || ch == '\n')) {
					MagicScriptError.error(tokenType.getError() + tokenType.getError() + "??????????????????????????????", stream.endSpan(), new StringLiteralException());
				}
			}
			if (!matchedEndQuote) {
				MagicScriptError.error("????????????????????????" + tokenType.getError(), stream.endSpan(), new StringLiteralException());
			}
			Span stringSpan = stream.endSpan();
			stringSpan = stream.getSpan(stringSpan.getStart(), stringSpan.getEnd() - tokenType.getLiteral().length());
			tokens.add(new LiteralToken(TokenType.StringLiteral, stringSpan));
			return true;
		}
		return false;
	}

	private static boolean regexpToken(CharacterStream stream, List<Token> tokens) {
		if (tokens.size() > 0) {
			Token token = tokens.get(tokens.size() - 1);
			if (token instanceof LiteralToken || token.getType() == TokenType.Identifier) {
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
