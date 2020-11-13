package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.VarNode;
import org.ssssssss.script.interpreter.AstInterpreter;
import org.ssssssss.script.parsing.Span;

import java.util.List;
import java.util.function.Function;

public class LambdaFunction extends Expression {
	private List<VarNode> parameters;
	private List<Node> childNodes;

	public LambdaFunction(Span span, List<VarNode> parameters, List<Node> childNodes) {
		super(span);
		this.parameters = parameters;
		this.childNodes = childNodes;
	}

	@Override
	public Object evaluate(MagicScriptContext context) {
		return (Function<Object[], Object>) args -> evaluate(context, args);
	}

	public List<VarNode> getParameters() {
		return parameters;
	}

	Object evaluate(MagicScriptContext context, Object[] args) {
		Object value;
		for (int i = 0; i < parameters.size() && i < args.length; i++) {
			parameters.get(i).setValue(context, args[i]);
		}
		value = AstInterpreter.interpretNodeList(childNodes, context);
		if (value instanceof Return.ReturnValue) {
			value = ((Return.ReturnValue) value).getValue();
		}
		return value;
	}
}
