package org.ssssssss.script.parsing.ast.literal;

import org.ssssssss.script.parsing.CharacterStream;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Literal;

/**
 * String 常量
 */
public class StringLiteral extends Literal {

	private final String value;

	public StringLiteral(Span literal) {
		super(literal);
		String text = getSpan().getText();
		String unescapedValue = text.substring(1, text.length() - 1);
		StringBuilder builder = new StringBuilder();

		CharacterStream stream = new CharacterStream(unescapedValue);
		// 处理转义符
		while (stream.hasMore()) {
			if (stream.match("\\\\", true)) {
				builder.append('\\');
			} else if (stream.match("\\n", true)) {
				builder.append('\n');
			} else if (stream.match("\\r", true)) {
				builder.append('\r');
			} else if (stream.match("\\t", true)) {
				builder.append('\t');
			} else if (stream.match("\\\"", true)) {
				builder.append('"');
			} else if (stream.match("\\'", true)) {
				builder.append("'");
			} else {
				builder.append(stream.consume());
			}
		}
		value = builder.toString();
		setValue(value);
	}

	public String getValue() {
		return value;
	}

}