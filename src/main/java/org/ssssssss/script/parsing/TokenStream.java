package org.ssssssss.script.parsing;

import org.ssssssss.script.MagicScriptError;

import javax.xml.transform.Source;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Iterates over a list of {@link Token} instances, provides methods to match expected tokens and throw errors in case of a
 * mismatch.
 */
public class TokenStream {
	private final List<Token> tokens;
	private final int end;
	private int index;
	private int makeIndex;

	public TokenStream(List<Token> tokens) {
		this.tokens = tokens;
		this.index = 0;
		this.end = tokens.size();
	}

	/**
	 * Returns whether there are more tokens in the stream.
	 **/
	public boolean hasMore() {
		return index < end;
	}

	public boolean hasNext() {
		return index + 1 < end;
	}

	public boolean hasPrev() {
		return index > 0;
	}

	public int makeIndex() {
		this.makeIndex = index;
		return index;
	}

	public void resetIndex() {
		this.index = this.makeIndex;
	}

	public void resetIndex(int index) {
		this.index = index;
	}

	/**
	 * Consumes the next token and returns it.
	 **/
	public Token consume() {
		if (!hasMore()) {
			throw new RuntimeException("Reached the end of the source.");
		}
		return tokens.get(index++);
	}

	public Token next() {
		if (!hasMore()) {
			throw new RuntimeException("Reached the end of the source.");
		}
		return tokens.get(++index);
	}

	public Token prev() {
		if (index == 0) {
			throw new RuntimeException("Reached the end of the source.");
		}
		return tokens.get(--index);
	}

	public Token getPrev() {
		if (index == 0) {
			throw new RuntimeException("Reached the end of the source.");
		}
		return tokens.get(index - 1);
	}

	public Token expect(TokenType ... types) {
		boolean result = match(true, types);
		if (!result) {
			Token token = index < tokens.size() ? tokens.get(index) : null;
			Span span = token != null ? token.getSpan() : null;
			if (span == null) {
				MagicScriptError.error("Expected '" + Stream.of(types).map(TokenType::getError).collect(Collectors.joining("','")) + "', but reached the end of the source.", this);
			} else {
				MagicScriptError.error("Expected '" + Stream.of(types).map(TokenType::getError).collect(Collectors.joining("','")) + "', but got '" + token.getText() + "'", span);
			}
			return null; // never reached
		} else {
			return tokens.get(index - 1);
		}
	}

	/**
	 * Checks if the next token has the give type and optionally consumes, or throws an error if the next token did not match the
	 * type.
	 */
	public Token expect(TokenType type) {
		boolean result = match(type, true);
		if (!result) {
			Token token = index < tokens.size() ? tokens.get(index) : null;
			Span span = token != null ? token.getSpan() : null;
			if (span == null) {
				MagicScriptError.error("Expected '" + type.getError() + "', but reached the end of the source.", this);
			} else {
				MagicScriptError.error("Expected '" + type.getError() + "', but got '" + token.getText() + "'", span);
			}
			return null; // never reached
		} else {
			return tokens.get(index - 1);
		}
	}

	public Token expect(String text) {
		return expect(text, false);
	}

	/**
	 * Checks if the next token matches the given text and optionally consumes, or throws an error if the next token did not match
	 * the text.
	 */
	public Token expect(String text, boolean ignoreCase) {
		boolean result = match(text, true, ignoreCase);
		if (!result) {
			Token token = index < tokens.size() ? tokens.get(index) : null;
			Span span = token != null ? token.getSpan() : null;
			if (span == null) {
				MagicScriptError.error("Expected '" + text + "', but reached the end of the source.", this);
			} else {
				MagicScriptError.error("Expected '" + text + "', but got '" + token.getText() + "'", span);
			}
			return null; // never reached
		} else {
			return tokens.get(index - 1);
		}
	}

	/**
	 * Matches and optionally consumes the next token in case of a match. Returns whether the token matched.
	 */
	public boolean match(TokenType type, boolean consume) {
		if (index >= end) {
			return false;
		}
		if (tokens.get(index).getType() == type) {
			if (consume) {
				index++;
			}
			return true;
		}
		return false;
	}

	public boolean match(List<String> texts, boolean consume) {
		return match(texts, consume, false);
	}

	public boolean match(List<String> texts, boolean consume, boolean ignoreCase) {
		for (String text : texts) {
			if (match(text, consume, ignoreCase)) {
				return true;
			}
		}
		return false;
	}

	public boolean match(String text, boolean consume, boolean ignoreCase) {
		if (index >= end) {
			return false;
		}
		String matchText = tokens.get(index).getText();
		if (ignoreCase ? matchText.equalsIgnoreCase(text) : matchText.equals(text)) {
			if (consume) {
				index++;
			}
			return true;
		}
		return false;
	}

	/**
	 * Matches and optionally consumes the next token in case of a match. Returns whether the token matched.
	 */
	public boolean match(String text, boolean consume) {
		return match(text, consume, false);
	}

	/**
	 * Matches any of the token types and optionally consumes the next token in case of a match. Returns whether the token
	 * matched.
	 */
	public boolean match(boolean consume, TokenType... types) {
		for (TokenType type : types) {
			if (match(type, consume)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Matches any of the token texts and optionally consumes the next token in case of a match. Returns whether the token
	 * matched.
	 */
	public boolean match(boolean consume, String... tokenTexts) {
		return match(consume, false, tokenTexts);
	}

	public boolean match(boolean consume, boolean ignoreCase, String... tokenTexts) {
		for (String text : tokenTexts) {
			if (match(text, consume, ignoreCase)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the {@link Source} this stream wraps.
	 */
	public String getSource() {
		if (tokens.size() == 0) {
			return null;
		}
		return tokens.get(0).getSpan().getSource();
	}
}
