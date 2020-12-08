package org.ssssssss.script.exception;

import org.ssssssss.script.parsing.ast.statement.Exit;

public class MagicExitException extends RuntimeException{

	private final Exit.Value exitValue;

	public MagicExitException(Exit.Value exitValue) {
		this.exitValue = exitValue;
	}

	public Exit.Value getExitValue() {
		return exitValue;
	}
}
