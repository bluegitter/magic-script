package org.ssssssss.script;


import org.ssssssss.script.compile.MagicScriptCompileException;
import org.ssssssss.script.compile.MagicScriptCompiler;
import org.ssssssss.script.exception.MagicExitException;
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

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MagicScript extends CompiledScript {

	public static final String CONTEXT_ROOT = "ROOT";

	public static final String DEBUG_MARK = "!# DEBUG\r\n";

	private static final MagicScriptClassLoader classLoader = new MagicScriptClassLoader();
	/**
	 * 所有语句
	 */
	private final List<Node> nodes;

	private final ScriptEngine scriptEngine;

	/**
	 * 存放所有变量定义
	 */
	private final Set<VarIndex> varIndices;

	private final List<Span> spans;

	/**
	 * 编译后的类
	 */
	private MagicScriptRuntime runtime;

	private boolean debug = false;

	private MagicScript(List<Node> nodes, List<Span> spans, Set<VarIndex> varIndices, ScriptEngine scriptEngine, boolean debug) {
		this.nodes = nodes;
		this.spans = spans;
		this.varIndices = varIndices;
		this.scriptEngine = scriptEngine;
		this.debug = debug;
	}

	/**
	 * 创建MagicScript
	 */
	public static MagicScript create(String source, ScriptEngine scriptEngine) {
		return create(false, source, scriptEngine);
	}

	/**
	 * 创建MagicScript
	 */
	public static MagicScript create(boolean expression, String source, ScriptEngine scriptEngine) {
		Parser parser = new Parser();
		boolean debug = source.startsWith(DEBUG_MARK);
		source = debug ? source.substring(DEBUG_MARK.length()) : source;
		List<Node> nodes = parser.parse(expression ? "return " + source : source);
		Set<VarIndex> varIndices = parser.getVarIndices();
		return new MagicScript(nodes, Node.mergeSpans(nodes), varIndices, scriptEngine, debug);
	}

	/**
	 * 根据编号获得Span
	 */
	public Span getSpan(int index) {
		return spans.get(index);
	}

	Object execute(MagicScriptContext magicScriptContext) {
		try {
			return compile().execute(magicScriptContext);
		} catch (MagicExitException e) {
			return e.getExitValue();
		} catch (Throwable t) {
			MagicScriptError.transfer(runtime, t);
		}
		return null;
	}

	/**
	 * 编译
	 */
	public MagicScriptRuntime compile() throws MagicScriptCompileException {
		if (runtime != null) {
			return runtime;
		}
		MagicScriptCompiler compiler = new MagicScriptCompiler(this.varIndices, this.spans, this.debug);
		// 如果只是一个表达式
		if (nodes.size() == 1 && nodes.get(0) instanceof Expression) {
			Node node = nodes.get(0);
			compiler.loadVars();
			compiler.compile(new Return(node.getSpan(), node));
		} else {
			// 根据是否有 import "xxx.xx.xx.*" 来分组
			Map<Boolean, List<Node>> nodeMap = nodes.stream().collect(Collectors.partitioningBy(it -> it instanceof Import && ((Import) it).isImportPackage()));
			// 编译需要的方法
			nodes.forEach(node -> node.visitMethod(compiler));
			compiler.compile(nodeMap.get(Boolean.TRUE));    // 先编译 import "xxx.xxx.x.*"
			// 加载变量信息
			compiler.loadVars();
			// 编译其它语句
			compiler.compile(nodeMap.get(Boolean.FALSE));
		}
		try {
			Class<MagicScriptRuntime> clazz = classLoader.load(compiler.getClassName(), compiler.bytecode());
			Constructor<MagicScriptRuntime> constructor = clazz.getConstructor();
			this.runtime = constructor.newInstance();    // 创建运行时实例
			// 设置变量名字
			runtime.setVarNames(varIndices.stream().map(VarIndex::getName).toArray(String[]::new));
			// 设置所有Span
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
