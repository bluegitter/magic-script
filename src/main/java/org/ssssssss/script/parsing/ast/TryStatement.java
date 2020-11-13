package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.VarNode;
import org.ssssssss.script.interpreter.AstInterpreter;
import org.ssssssss.script.parsing.Span;

import java.util.List;

public class TryStatement extends Node {
	private final VarNode exceptionVarNode;
	private final List<Node> tryBlock;
	private final List<Node> catchBlock;
	private final List<Node> finallyBlock;

	public TryStatement(Span span, VarNode exceptionVarNode, List<Node> tryBlock, List<Node> catchBlock, List<Node> finallyBlock) {
		super(span);
		this.exceptionVarNode = exceptionVarNode;
		this.tryBlock = tryBlock;
		this.catchBlock = catchBlock;
		this.finallyBlock = finallyBlock;
	}


	@Override
	public Object evaluate(MagicScriptContext context) {
		try {
            Object value = AstInterpreter.interpretNodeList(tryBlock, context);
            return value;
		} catch (Throwable throwable) {
			if (catchBlock != null && catchBlock.size() > 0) {
				if (exceptionVarNode != null) {
					exceptionVarNode.setValue(context, throwable);
                }
				return AstInterpreter.interpretNodeList(catchBlock, context);
			}else{
			    throw throwable;
            }
		} finally {
            if(finallyBlock != null && finallyBlock.size() >0){
                Object value = AstInterpreter.interpretNodeList(finallyBlock, context);
				if (value instanceof Return.ReturnValue) {
                	return value;
				}
            }
		}
	}
}