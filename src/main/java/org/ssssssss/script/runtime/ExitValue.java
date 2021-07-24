package org.ssssssss.script.runtime;

public class ExitValue {

	private final Object[] values;

	public ExitValue(Object[] values) {
		this.values = values;
	}

	public Object[] getValues() {
		return values;
	}

	public int getLength(){
		return values.length;
	}
}
