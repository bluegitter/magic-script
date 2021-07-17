package org.ssssssss.script.runtime.linq;

public class OrderValue {
	private Object value;
	private int order;

	public OrderValue(Object value, int order) {
		this.value = value;
		this.order = order;
	}

	public Object getValue() {
		return value;
	}

	public int getOrder() {
		return order;
	}
}