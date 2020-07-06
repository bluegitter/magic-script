package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.interpreter.AstInterpreter;
import org.ssssssss.script.parsing.Span;

import java.util.List;

public class TryStatement extends Node {
	private final String exceptionName;
	private final List<Node> tryBlock;
	private final List<Node> catchBlock;
	private final List<Node> finallyBlock;

	public TryStatement(Span span, String exceptionName, List<Node> tryBlock, List<Node> catchBlock, List<Node> finallyBlock) {
		super(span);
		this.exceptionName = exceptionName;
		this.tryBlock = tryBlock;
		this.catchBlock = catchBlock;
		this.finallyBlock = finallyBlock;
	}


	@Override
	public Object evaluate(MagicScriptContext context) {
		try {
		    context.push();
            Object value = AstInterpreter.interpretNodeList(tryBlock, context);
            context.pop();
            return value;
		} catch (Throwable throwable) {
            context.push();
			if (catchBlock != null && catchBlock.size() > 0) {
                if (exceptionName != null) {
                    context.set(exceptionName, throwable);
                }
                Object value = AstInterpreter.interpretNodeList(catchBlock, context);
                context.pop();
                return value;
			}else{
                context.pop();
			    throw throwable;
            }
		} finally {
            if(finallyBlock != null && finallyBlock.size() >0){
                context.push();
                Object value = AstInterpreter.interpretNodeList(finallyBlock, context);
                context.pop();
                if(value == Return.RETURN_SENTINEL){
                	return value;
				}
            }
		}
	}
}