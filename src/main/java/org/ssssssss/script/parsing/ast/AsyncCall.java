package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.VarNode;
import org.ssssssss.script.parsing.Span;

import java.util.List;
import java.util.concurrent.*;
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

	private static ThreadPoolExecutor threadPoolExecutor = setThreadPoolExecutorSize(size);

	public AsyncCall(Span span, Expression expression) {
		super(span);
		this.expression = expression;
	}

	public static ThreadPoolExecutor setThreadPoolExecutorSize(int size) {
		if (size > 0) {
			return new ThreadPoolExecutor(size, size, 0L, TimeUnit.MILLISECONDS,
					new ArrayBlockingQueue<>(size * 2), new AsyncThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());
		}
		return threadPoolExecutor;

	}

	@Override
	public Object evaluate(MagicScriptContext context) {
		Object[] args = null;
		if (expression instanceof LambdaFunction) {
			LambdaFunction lambdaFunction = (LambdaFunction) expression;
			List<VarNode> parameters = lambdaFunction.getParameters();
			args = parameters.stream().map(item -> {
				Object value = context.findAndGet(item);
				item.setValue(context, value);
				return value;
			}).toArray();
		}
		Object[] finalArgs = args;
		Integer threadId = context.copyThreadVariables();
		FutureTask<Object> futureTask = new FutureTask<>(() -> {
			try {
				context.pushThread(threadId);
				MagicScriptContext.set(context);
				if (expression instanceof LambdaFunction) {
					return ((LambdaFunction) expression).evaluate(context, finalArgs);
				}
				return expression.evaluate(context);
			} finally {
				context.popThread(threadId);
				context.deleteThreadVariables(threadId);
				MagicScriptContext.remove();
			}
		});
		//	判断当前是否在线程池中，如果是的话，没直接运行，防止线程嵌套造成的"死锁"
		if (Thread.currentThread().getThreadGroup() == AsyncThreadFactory.ASYNC_THREAD_GROUP) {
			futureTask.run();
		} else {
			threadPoolExecutor.submit(futureTask);
		}

		return futureTask;
	}

	static class AsyncThreadFactory implements ThreadFactory {

		private final AtomicInteger threadNumber = new AtomicInteger(1);

		private static final ThreadGroup ASYNC_THREAD_GROUP = new ThreadGroup("magic-async-group");
		private final String namePrefix = "magic-async-";

		public Thread newThread(Runnable r) {
			Thread t = new Thread(ASYNC_THREAD_GROUP, r, namePrefix + threadNumber.getAndIncrement(), 0);
			if (t.isDaemon())
				t.setDaemon(false);
			if (t.getPriority() != Thread.NORM_PRIORITY)
				t.setPriority(Thread.NORM_PRIORITY);
			return t;
		}
	}
}
