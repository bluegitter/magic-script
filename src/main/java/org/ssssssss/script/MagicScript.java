package org.ssssssss.script;


import org.ssssssss.script.compile.MagicScriptCompileException;
import org.ssssssss.script.compile.MagicScriptCompiler;
import org.ssssssss.script.exception.MagicScriptException;
import org.ssssssss.script.parsing.Parser;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.VarIndex;
import org.ssssssss.script.parsing.ast.Expression;
import org.ssssssss.script.parsing.ast.Node;
import org.ssssssss.script.parsing.ast.statement.Import;
import org.ssssssss.script.parsing.ast.statement.Return;
import org.ssssssss.script.runtime.MagicScriptClassLoader;
import org.ssssssss.script.runtime.MagicScriptRuntime;

import java.sql.*;
import java.util.*;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MagicScript extends CompiledScript {

	private static final MagicScriptClassLoader classLoader = new MagicScriptClassLoader();
	public static final String CONTEXT_ROOT = "ROOT";
	private final List<Node> nodes;
	private final ScriptEngine scriptEngine;
	private final Set<VarIndex> varIndices;
	private final List<Span> spans;
	private MagicScriptRuntime runtime;

	private MagicScript(List<Node> nodes, List<Span> spans, Set<VarIndex> varIndices, ScriptEngine scriptEngine) {
		this.nodes = nodes;
		this.spans = spans;
		this.varIndices = varIndices;
		this.scriptEngine = scriptEngine;
	}

	public static MagicScript create(String source, ScriptEngine scriptEngine) {
		Parser parser = new Parser();
		List<Node> nodes = parser.parse(source);
		Set<VarIndex> varIndices = parser.getVarIndices();
		return new MagicScript(nodes, Node.mergeSpans(nodes), varIndices, scriptEngine);
	}

	public Span getSpan(int index) {
		return spans.get(index);
	}

	Object execute(MagicScriptContext magicScriptContext) {
		return compile().execute(magicScriptContext);
	}

	public MagicScriptRuntime compile() throws MagicScriptCompileException {
		if(runtime != null){
			return runtime;
		}
		MagicScriptCompiler compiler = new MagicScriptCompiler(this.varIndices, this.spans);
		if(nodes.size() == 1 && nodes.get(0) instanceof Expression){
			Node node = nodes.get(0);
			compiler.loadVars();
			compiler.compile(new Return(node.getSpan(), node));
		}else{
			Map<Boolean, List<Node>> nodeMap = nodes.stream().collect(Collectors.partitioningBy(it -> it instanceof Import && ((Import) it).isImportPackage()));
			nodes.forEach(node -> node.visitMethod(compiler));
			compiler.compile(nodeMap.get(Boolean.TRUE));	// 先编译 import "xxx.xxx.x.*"
			compiler.loadVars();
			compiler.compile(nodeMap.get(Boolean.FALSE));
		}
		try {
			Class<MagicScriptRuntime> clazz = classLoader.load(compiler.getClassName(), compiler.bytecode());
			Constructor<MagicScriptRuntime> constructor = clazz.getConstructor();
			this.runtime = constructor.newInstance();
			runtime.setVarNames(varIndices.stream().map(VarIndex::getName).toArray(String[]::new));
			runtime.setSpans(this.spans);
			return runtime;
		} catch (Exception e) {
			throw new MagicScriptCompileException(e);
		}
	}


	@Override
	public Object eval(ScriptContext context) {
		Bindings bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);
		if (bindings.containsKey(CONTEXT_ROOT)) {
			Object root = bindings.get(CONTEXT_ROOT);
			if (root instanceof MagicScriptContext) {
				MagicScriptContext rootContext = (MagicScriptContext) root;
				rootContext.putMapIntoContext(MagicScriptEngine.getDefaultImports());
				return execute(rootContext);
			} else {
				throw new MagicScriptException("参数不正确！");
			}
		}
		MagicScriptContext magicScriptContext = new MagicScriptContext();
		magicScriptContext.putMapIntoContext(context.getBindings(ScriptContext.GLOBAL_SCOPE));
		magicScriptContext.putMapIntoContext(context.getBindings(ScriptContext.ENGINE_SCOPE));
		return execute(magicScriptContext);
	}

	@Override
	public ScriptEngine getEngine() {
		return scriptEngine;
	}
}
