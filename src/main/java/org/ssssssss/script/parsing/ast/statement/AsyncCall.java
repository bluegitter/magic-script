package org.ssssssss.script.parsing.ast.statement;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.compile.MagicScriptCompiler;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.VarIndex;
import org.ssssssss.script.parsing.ast.Expression;
import org.ssssssss.script.runtime.function.MagicScriptLambdaFunction;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 异步调用
 */
public class AsyncCall extends Expression {

	private final LambdaFunction expression;

	/**
	 * 默认线程池大小(CPU核心数 * 2)
	 */
	private final static int size = Runtime.getRuntime().availableProcessors() * 2;

	private static ThreadPoolExecutor threadPoolExecutor = setThreadPoolExecutorSize(size);

	public AsyncCall(Span span, Expression expression) {
		super(span);
		if(expression instanceof LambdaFunction){
			this.expression = (LambdaFunction) expression;
		}else{
			this.expression = new LambdaFunction(span, Collections.emptyList(), Collections.singletonList(new Return(span, expression)));
		}
		this.expression.setAsync(true);
	}

	public static ThreadPoolExecutor setThreadPoolExecutorSize(int size) {
		if (size > 0) {
			threadPoolExecutor = new ThreadPoolExecutor(size, size, 0L, TimeUnit.MILLISECONDS,
					new ArrayBlockingQueue<>(size * 2), new AsyncThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());
		}
		return threadPoolExecutor;

	}

	@Override
	public void visitMethod(MagicScriptCompiler compiler) {
		expression.visitMethod(compiler);
	}

	public static FutureTask<Object> execute(MagicScriptLambdaFunction function, MagicScriptContext context, Object[] args) {
		FutureTask<Object> futureTask = new FutureTask<>(() -> {
			Object value = function.apply(context, args);
			context.restore();
			return value;
		});
		//	判断当前是否在线程池中，如果是的话，则直接运行，防止线程嵌套造成的"死锁"
		if (Thread.currentThread().getThreadGroup() == AsyncThreadFactory.ASYNC_THREAD_GROUP) {
			futureTask.run();
		} else {
			threadPoolExecutor.submit(futureTask);
		}
		return futureTask;
	}

	@Override
	public void compile(MagicScriptCompiler compiler) {
		List<VarIndex> parameters = expression.getParameters();
		compiler.compile(expression)
				.load1();
		parameters.forEach(compiler::load);
		compiler.call("call_async", parameters.size() + 2);

	}

	static class AsyncThreadFactory implements ThreadFactory {

		private final AtomicLong threadNumber = new AtomicLong(1);

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
