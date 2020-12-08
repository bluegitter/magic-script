package org.ssssssss.script.parsing.ast.statement;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.exception.MagicExitException;
import org.ssssssss.script.interpreter.AstInterpreter;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.VarIndex;
import org.ssssssss.script.parsing.ast.Node;

import java.util.List;

public class TryStatement extends Node {
	private final VarIndex exceptionVarNode;
	private final List<Node> tryBlock;
	private final List<Node> catchBlock;
	private final List<Node> finallyBlock;

	public TryStatement(Span span, VarIndex exceptionVarNode, List<Node> tryBlock, List<Node> catchBlock, List<Node> finallyBlock) {
		super(span);
		this.exceptionVarNode = exceptionVarNode;
		this.tryBlock = tryBlock;
		this.catchBlock = catchBlock;
		this.finallyBlock = finallyBlock;
	}


	@Override
	public Object evaluate(MagicScriptContext context, Scope scope) {
		try {
			Object value = AstInterpreter.interpretNodeList(tryBlock, context, scope);
			return value;
		} catch (MagicExitException mee){
			throw mee;
		} catch (Throwable throwable) {
			if (catchBlock != null && catchBlock.size() > 0) {
				if (exceptionVarNode != null) {
					scope.setValue(exceptionVarNode, throwable);
				}
				return AstInterpreter.interpretNodeList(catchBlock, context, scope);
			} else {
				throw throwable;
			}
		} finally {
			if (finallyBlock != null && finallyBlock.size() > 0) {
				Object value = AstInterpreter.interpretNodeList(finallyBlock, context, scope);
				if (value instanceof Return.ReturnValue) {
					return value;
				}
			}
		}
	}
}