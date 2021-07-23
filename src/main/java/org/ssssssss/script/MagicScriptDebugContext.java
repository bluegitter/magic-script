package org.ssssssss.script;

import org.ssssssss.script.parsing.Span;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class MagicScriptDebugContext extends MagicScriptContext {

	private static final Map<String, MagicScriptDebugContext> contextMap = new ConcurrentHashMap<>();
	private final BlockingQueue<String> producer = new LinkedBlockingQueue<>();
	private final BlockingQueue<String> consumer = new LinkedBlockingQueue<>();
	public List<Integer> breakpoints;
	private String id = UUID.randomUUID().toString().replace("-", "");
	private Consumer<Map<String, Object>> callback;

	private Span.Line line;

	private int timeout = 60;

	private boolean stepInto = false;

	public MagicScriptDebugContext(List<Integer> breakpoints) {
		this.breakpoints = breakpoints;
	}

	public static MagicScriptDebugContext getDebugContext(String id) {
		return contextMap.get(id);
	}

	public void setCallback(Consumer<Map<String, Object>> callback) {
		this.callback = callback;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public List<Integer> getBreakpoints() {
		return breakpoints;
	}

	public void setBreakpoints(List<Integer> breakpoints) {
		this.breakpoints = breakpoints;
	}

	public synchronized String pause(Span.Line line) throws InterruptedException {
		this.line = line;
		consumer.offer(this.id);
		callback.accept(getDebugInfo());
		return producer.poll(timeout, TimeUnit.SECONDS);
	}

	public void await() throws InterruptedException {
		consumer.take();
	}

	public void singal() throws InterruptedException {
		producer.offer(this.id);
		await();
	}

	public boolean isStepInto() {
		return stepInto;
	}

	public void setStepInto(boolean stepInto) {
		this.stepInto = stepInto;
	}

	public Map<String, Object> getDebugInfo() {
		List<Map<String, Object>> varList = new ArrayList<>();
		Set<Map.Entry<String, Object>> entries = getVariables().entrySet();
		for (Map.Entry<String, Object> entry : entries) {
			Object value = entry.getValue();
			Map<String, Object> variable = new HashMap<>();
			variable.put("name", entry.getKey());
			if (value != null) {
				variable.put("value", getValue(value));
				variable.put("type", value.getClass());
			} else {
				variable.put("value", "null");
			}
			varList.add(variable);
		}
		varList.sort((o1, o2) -> {
			Object k1 = o1.get("name");
			Object k2 = o2.get("name");
			if (k1 == null) {
				return -1;
			}
			if (k2 == null) {
				return 1;
			}
			return k1.toString().compareTo(k2.toString());
		});
		Map<String, Object> info = new HashMap<>();
		info.put("variables", varList);
		info.put("range", Arrays.asList(line.getLineNumber(), line.getStartCol(), line.getEndLineNumber(), line.getEndCol()));
		return info;
	}

	public Span.Line getLine() {
		return line;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		String oldId = this.id;
		this.id = id;
		contextMap.put(this.id, this);
		contextMap.remove(oldId);
	}

	private Object getValue(Object object) {
		try {
			return object.toString();
		} catch (Exception ex) {
			if (object instanceof Cloneable) {
				try {
					return object.getClass().getMethod("clone").invoke(object).toString();
				} catch (Exception ignored) {
				}
			}
			return "can't get value";
		}
	}
}
