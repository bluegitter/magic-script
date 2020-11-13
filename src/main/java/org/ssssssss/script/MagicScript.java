package org.ssssssss.script;


import org.ssssssss.script.exception.DebugTimeoutException;
import org.ssssssss.script.exception.MagicScriptException;
import org.ssssssss.script.interpreter.AstInterpreter;
import org.ssssssss.script.parsing.Parser;
import org.ssssssss.script.parsing.ast.Node;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import java.util.List;

public class MagicScript extends CompiledScript {

	public static final String CONTEXT_ROOT = "ROOT";
	private final List<Node> nodes;
	private final ScriptEngine scriptEngine;
	private VariableContext variableContext;

	private MagicScript(List<Node> nodes, ScriptEngine scriptEngine, VariableContext variableContext) {
		this.nodes = nodes;
		this.scriptEngine = scriptEngine;
		this.variableContext = variableContext;
	}

	public static MagicScript create(String source, ScriptEngine scriptEngine) {
		VariableContext variableContext = new VariableContext();
		Parser parser = new Parser(variableContext);
		return new MagicScript(parser.parse(source), scriptEngine, variableContext);
	}

	public List<Node> getNodes() {
		return nodes;
	}

	Object execute(MagicScriptContext magicScriptContext) {
		return AstInterpreter.interpret(this, magicScriptContext);
	}


	@Override
	public Object eval(ScriptContext context) {
		Bindings bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);
		if (bindings.containsKey(CONTEXT_ROOT)) {
			Object root = bindings.get(CONTEXT_ROOT);
			if (root instanceof MagicScriptContext) {
				MagicScriptContext rootContext = (MagicScriptContext) root;
				rootContext.putMapIntoContext(MagicScriptEngine.getDefaultImports());
				rootContext.setVariableContext(variableContext.copy());
				if (rootContext instanceof MagicScriptDebugContext) {
					MagicScriptDebugContext debugContext = (MagicScriptDebugContext) rootContext;
					new Thread(() -> {
						try {
							debugContext.start();
							debugContext.setReturnValue(execute(debugContext));
						} catch (Exception e) {
							debugContext.setException(true);
							debugContext.setReturnValue(e);
						}
					}, "magic-script").start();
					try {
						debugContext.await();
					} catch (InterruptedException e) {
						throw new DebugTimeoutException(e);
					}
					return debugContext.isRunning() ? debugContext.getDebugInfo() : debugContext.getReturnValue();
				}
				return execute(rootContext);
			} else {
				throw new MagicScriptException("参数不正确！");
			}
		}
		MagicScriptContext magicScriptContext = new MagicScriptContext();
		magicScriptContext.putMapIntoContext(context.getBindings(ScriptContext.GLOBAL_SCOPE));
		magicScriptContext.putMapIntoContext(context.getBindings(ScriptContext.ENGINE_SCOPE));
		magicScriptContext.setVariableContext(variableContext.copy());
		return execute(magicScriptContext);
	}

	@Override
	public ScriptEngine getEngine() {
		return scriptEngine;
	}
}
