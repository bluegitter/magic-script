package org.ssssssss.script;

import org.ssssssss.script.exception.MagicScriptException;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.TokenStream;

/**
 * All errors reported by the library go through the static functions of this class.
 */
public class MagicScriptError {

	/**
	 * <p>
	 * Create an error message based on the provided message and stream, highlighting the line on which the error happened. If the
	 * stream has more tokens, the next token will be highlighted. Otherwise the end of the source of the stream will be
	 * highlighted.
	 * </p>
	 *
	 * <p>
	 * Throws a {@link RuntimeException}
	 * </p>
	 */
	public static void error(String message, TokenStream stream) {
		if (stream.hasMore()) {
			error(message, stream.consume().getSpan());
		} else {
			error(message, stream.getPrev().getSpan());
		}
	}

	/**
	 * Create an error message based on the provided message and location, highlighting the location in the line on which the
	 * error happened. Throws a {@link MagicScriptException}
	 **/
	public static void error(String message, Span location, Throwable cause) {
		cause = unwrap(cause);
		if (cause instanceof MagicScriptException) {
			MagicScriptException mse = ((MagicScriptException) cause);
			if(mse.getLocation() == null){
				error(message, location, cause.getCause());
				return;
			}
			throw mse;
		}
		Span.Line line = location.getLine();
		String errorMessage = message + " at Row:";
		errorMessage += line.getLineNumber() + "~" + line.getEndLineNumber() + ",Col:";
		errorMessage += line.getStartCol() + "~" + line.getEndCol() + "\n\n";
		errorMessage += line.getText();
		errorMessage += "\n";
		int errorStart = location.getStart() - line.getStart();
		int errorEnd = errorStart + location.getText().length() - 1;
		for (int i = 0, n = line.getText().length(); i < n; i++) {
			boolean useTab = line.getText().charAt(i) == '\t';
			errorMessage += i >= errorStart && i <= errorEnd ? "^" : useTab ? "\t" : " ";
		}
		if (cause == null) {
			throw new MagicScriptException(errorMessage, message, location);
		} else {
			throw new MagicScriptException(errorMessage, message, cause, location);
		}
	}

	/**
	 * Create an error message based on the provided message and location, highlighting the location in the line on which the
	 * error happened. Throws a {@link MagicScriptException}
	 **/
	public static void error(String message, Span location) {
		error(message, location, null);
	}

	public static Throwable unwrap(Throwable root) {
		Throwable parent = root;
		while (parent != null) {
			if (parent instanceof MagicScriptException) {
				root = parent;
			}
			parent = parent.getCause();
		}
		return root;
	}

}
