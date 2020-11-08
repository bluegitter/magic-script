package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.parsing.Span;

import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 异步调用
 */
public class AsyncCall extends Expression {

	private final Expression expression;

	/**
	 * 默认线程池大小(CPU核心数 * 2)
	 */
	private final static int size = Runtime.getRuntime().availableProcessors() * 2;

	private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(size, size, 0L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<>(), new AsyncThreadFactory(), new ThreadPoolExecutor.AbortPolicy());

	public AsyncCall(Span span, Expression expression) {
		super(span);
		this.expression = expression;
	}

	public static void setThreadPoolExecutorSize(int size){
		if(size > 0){
			threadPoolExecutor.setCorePoolSize(size);
			threadPoolExecutor.setMaximumPoolSize(size);
		}
	}

	@Override
	public Object evaluate(MagicScriptContext context) {
		List<Map<String, Object>> scopes = context.getScopes();
		return threadPoolExecutor.submit(() -> {
			try {
				MagicScriptContext.set(context);
				context.setScopes(scopes);
				return expression instanceof LambdaFunction ? ((LambdaFunction) expression).internalCall(context) : expression.evaluate(context);
			} finally {
				MagicScriptContext.remove();
			}
		});
	}

	static class AsyncThreadFactory implements ThreadFactory {
		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;

		AsyncThreadFactory() {
			SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
			namePrefix = "magic-async-";
		}

		public Thread newThread(Runnable r) {
			Thread t = new Thread(group, r,namePrefix + threadNumber.getAndIncrement(),0);
			if (t.isDaemon())
				t.setDaemon(false);
			if (t.getPriority() != Thread.NORM_PRIORITY)
				t.setPriority(Thread.NORM_PRIORITY);
			return t;
		}
	}
}
