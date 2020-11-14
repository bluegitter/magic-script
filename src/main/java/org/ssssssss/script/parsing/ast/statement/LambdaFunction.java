package org.ssssssss.script.parsing.ast.statement;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.interpreter.AstInterpreter;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.VarIndex;
import org.ssssssss.script.parsing.ast.Expression;
import org.ssssssss.script.parsing.ast.Node;

import java.util.List;
import java.util.function.Function;

public class LambdaFunction extends Expression {
	private List<VarIndex> parameters;
	private List<Node> childNodes;
	private int varCount;

	public LambdaFunction(Span span, List<VarIndex> parameters, int varCount, List<Node> childNodes) {
		super(span);
		this.parameters = parameters;
		this.childNodes = childNodes;
		this.varCount = varCount;
	}

	@Override
	public Object evaluate(MagicScriptContext context, Scope scope) {
		return (Function<Object[], Object>) args -> evaluate(context, scope.create(varCount), args);
	}

	public List<VarIndex> getParameters() {
		return parameters;
	}

	public int getVarCount() {
		return varCount;
	}

	Object evaluate(MagicScriptContext context, Scope scope, Object[] args) {
		Object value;
		for (int i = 0; i < parameters.size() && i < args.length; i++) {
			scope.setValue(parameters.get(i), args[i]);
		}
		value = AstInterpreter.interpretNodeList(childNodes, context, scope);
		if (value instanceof Return.ReturnValue) {
			value = ((Return.ReturnValue) value).getValue();
		}
		return value;
	}
}
