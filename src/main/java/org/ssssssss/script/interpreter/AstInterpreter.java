package org.ssssssss.script.interpreter;

import org.ssssssss.script.MagicScript;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptDebugContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.exception.DebugTimeoutException;
import org.ssssssss.script.exception.MagicExitException;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Node;
import org.ssssssss.script.parsing.ast.statement.Break;
import org.ssssssss.script.parsing.ast.statement.Continue;
import org.ssssssss.script.parsing.ast.statement.Return;
import org.ssssssss.script.reflection.AbstractReflection;

import java.util.List;

/**
 * <p>
 * Interprets a Template given a MagicScriptContext to lookup variable values in and writes the evaluation results to an output
 * stream. Uses the global {@link AbstractReflection} instance as returned by {@link AbstractReflection#getInstance()} to access members and call
 * methods.
 * </p>
 *
 * <p>
 * The interpeter traverses the AST as stored in {@link MagicScript#getNodes()}. the interpeter has a method for each AST node type
 * be written to the output stream.
 * </p>
 **/
public class AstInterpreter {
	public static Object interpret(MagicScript magicScript, MagicScriptContext context, Scope scope) {
		try {
			MagicScriptContext.set(context);
			context.setVarScope(scope);
			Object value = interpretNodeList(magicScript.getNodes(), context, scope);
			if (value instanceof Return.ReturnValue) {
				return ((Return.ReturnValue) value).getValue();
			}
			return null;
		} catch(MagicExitException mee){
			return mee.getExitValue();
		} catch (Throwable t) {
			MagicScriptError.error("执行脚本出错 " + t.getMessage(), magicScript.getNodes().get(0).getSpan(), t); // never reached
		} finally {
			context.removeVarScope();
			MagicScriptContext.remove();
		}
		return null;
	}

	public static Object interpretNodeList(List<Node> nodes, MagicScriptContext context, Scope scope) {
		if (nodes != null) {
			boolean step = false;
			for (Node node : nodes) {
				if (context instanceof MagicScriptDebugContext) {
					MagicScriptDebugContext debugContext = (MagicScriptDebugContext) context;
					Span.Line line = node.getSpan().getLine();
					if (step || debugContext.getBreakpoints().contains(line.getLineNumber())) {
						try {
							if (debugContext.pause(line, scope) == null) {
								debugContext.setReturnValue(null);
								throw new DebugTimeoutException();
							}
							step = debugContext.isStepInto();
							debugContext.setStepInto(false);
						} catch (InterruptedException e) {
							throw new DebugTimeoutException(e);
						}
					}
				}
				Object value = null;
				try {
					value = node.evaluate(context, scope);
				} catch (Exception e) {
					MagicScriptError.error(e.getMessage(), node.getSpan(), e);
				}
				if (value == Break.BREAK_SENTINEL || value == Continue.CONTINUE_SENTINEL || value instanceof Return.ReturnValue) {
					return value;
				}
			}
		}
		return null;
	}
}
