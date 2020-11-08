package org.ssssssss.script;

import org.ssssssss.script.parsing.Span;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MagicScriptDebugContext extends MagicScriptContext {

	private String id = UUID.randomUUID().toString().replace("-", "");

	private static Map<String, MagicScriptDebugContext> contextMap = new ConcurrentHashMap<>();

	public List<Integer> breakpoints;

	private BlockingQueue<String> producer = new LinkedBlockingQueue<>();

	private BlockingQueue<String> consumer = new LinkedBlockingQueue<>();

	private Object returnValue;

	private Span.Line line;

	private boolean running = true;

	private boolean exception = false;

	private int timeout = 60;

	private Runnable complete;

	private Runnable start;

	private boolean stepInto = false;

	private List<Map<String, Object>> cachedScopes;

	public MagicScriptDebugContext(){
		super();
	}

	public void setId(String id) {
		String oldId = this.id;
		this.id = id;
		contextMap.put(this.id, this);
		contextMap.remove(oldId);
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

	public String pause(Span.Line line) throws InterruptedException {
		cachedScopes = getScopes();
		this.line = line;
		consumer.offer(this.id);
		return producer.poll(timeout, TimeUnit.SECONDS);
	}

	public void await() throws InterruptedException {
		consumer.take();
		resetScopes();
	}

	public void singal() throws InterruptedException {
		resetScopes();
		producer.offer(this.id);
		await();
	}

	public Object getReturnValue() {
		return returnValue;
	}

	public void onComplete(Runnable complete){
		this.complete = complete;
	}

	public void start(){
		if(this.start != null){
			this.start.run();
		}
	}

	public void onStart(Runnable start){
		this.start = start;
	}

	public void setReturnValue(Object returnValue) {
		this.running = false;
		this.returnValue = returnValue;
		contextMap.remove(this.id);
		consumer.offer(this.id);
		if(complete != null){
			complete.run();
		}
	}

	public boolean isStepInto() {
		return stepInto;
	}

	public void setStepInto(boolean stepInto) {
		this.stepInto = stepInto;
	}

	public boolean isRunning() {
		return running;
	}

	public Map<String, Object> getDebugInfo() {
		List<Map<String, Object>> varList = new ArrayList<>();
		Set<Map.Entry<String, Object>> entries = super.getVariables().entrySet();
		for (Map.Entry<String, Object> entry : entries) {
			Object value = entry.getValue();
			Map<String, Object> variable = new HashMap<>();
			variable.put("name", entry.getKey());
			if (value != null) {
				variable.put("value", getValue(value));
				variable.put("type", value.getClass());
			}else{
				variable.put("value", "null");
			}
			varList.add(variable);
		}
		varList.sort((o1, o2) -> {
			Object k1 = o1.get("name");
			Object k2 = o2.get("name");
			if(k1 == null){
				return -1;
			}
			if(k2 == null){
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

	public boolean isException() {
		return exception;
	}

	public void setException(boolean exception) {
		this.exception = exception;
	}

	private void resetScopes() {
		setScopes(cachedScopes);
	}

	public static MagicScriptDebugContext getDebugContext(String id) {
		return contextMap.get(id);
	}

	private Object getValue(Object object){
		try {
			return object.toString();
		} catch (Exception ex) {
			if(object instanceof Cloneable){
				try {
					return object.getClass().getMethod("clone").invoke(object).toString();
				} catch (Exception ignored) {}
			}
			return "can't get value";
		}
	}
}
