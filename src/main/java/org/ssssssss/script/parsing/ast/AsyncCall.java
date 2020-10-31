package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.parsing.Span;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 异步调用
 */
public class AsyncCall extends Expression {

	private final Expression expression;

	public AsyncCall(Span span, Expression expression) {
		super(span);
		this.expression = expression;
	}

	@Override
	public Object evaluate(MagicScriptContext context) {
		List<Map<String, Object>> scopes = context.getScopes();
		return CompletableFuture.supplyAsync(() -> {
			context.setScopes(scopes);
			Object value = expression instanceof LambdaFunction ? ((LambdaFunction) expression).internalCall(context) : expression.evaluate(context);
			return value;
		}, runnable -> new Thread(runnable).start());	// 禁止复用线程。
	}
}
