package org.ssssssss.script.parsing.ast.statement;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.functions.ClassExtension;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.VarIndex;
import org.ssssssss.script.parsing.ast.Expression;

import java.util.List;

public class NewStatement extends Expression {

	private final List<Expression> arguments;

	private final Expression target;

	public NewStatement(Span span, Expression target, List<Expression> arguments) {
		super(span);
		this.target = target;
		this.arguments = arguments;
	}

	@Override
	public Object evaluate(MagicScriptContext context, Scope scope) {
		Object clazz = target.evaluate(context, scope);
		if (clazz instanceof Class) {
			Class<?> cls = (Class<?>) clazz;
			Object[] args = new Object[arguments.size()];
			for (int i = 0; i < args.length; i++) {
				args[i] = arguments.get(i).evaluate(context, scope);
			}
			try {
				return ClassExtension.newInstance(cls, args);
			} catch (Throwable t) {
				MagicScriptError.error(clazz + " can not newInstance.", getSpan(), t);
			}
		} else {
			MagicScriptError.error(clazz + " 不是class类型", getSpan());
		}
		return null;
	}
}
