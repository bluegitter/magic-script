package org.ssssssss.script.runtime.linq;

public class Record {

	private Object value;

	private Object joinValue;

	private int joinIndex = -1;

	public Record(Object value) {
		this.value = value;
	}

	public Record(Object value, Object joinValue, int joinIndex) {
		this.value = value;
		this.joinValue = joinValue;
		this.joinIndex = joinIndex;
	}

	public int getJoinIndex() {
		return joinIndex;
	}

	public Object getValue() {
		return value;
	}

	public Object getJoinValue() {
		return joinValue;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public void setJoinValue(Object joinValue) {
		this.joinValue = joinValue;
	}
}
