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
	private final int tryVarCount;
	private final int catchVarCount;
	private final int finallyVarCount;

	public TryStatement(Span span, VarIndex exceptionVarNode, List<Node> tryBlock, List<Node> catchBlock, List<Node> finallyBlock,int tryVarCount,int catchVarCount,int finallyVarCount) {
		super(span);
		this.exceptionVarNode = exceptionVarNode;
		this.tryBlock = tryBlock;
		this.catchBlock = catchBlock;
		this.finallyBlock = finallyBlock;
		this.tryVarCount = tryVarCount;
		this.catchVarCount = catchVarCount;
		this.finallyVarCount = finallyVarCount;

	}


	@Override
	public Object evaluate(MagicScriptContext context, Scope scope) {
		try {
			Object value = AstInterpreter.interpretNodeList(tryBlock, context, scope.create(tryVarCount));
			return value;
		} catch (MagicExitException mee){
			throw mee;
		} catch (Throwable throwable) {
			if (catchBlock != null && catchBlock.size() > 0) {
				Scope catchScope = scope.create(catchVarCount);
				if (exceptionVarNode != null) {
					catchScope.setValue(exceptionVarNode, throwable);
				}
				return AstInterpreter.interpretNodeList(catchBlock, context, catchScope);
			} else {
				throw throwable;
			}
		} finally {
			if (finallyBlock != null && finallyBlock.size() > 0) {
				Object value = AstInterpreter.interpretNodeList(finallyBlock, context, scope.create(finallyVarCount));
				if (value instanceof Return.ReturnValue) {
					return value;
				}
			}
		}
	}
}