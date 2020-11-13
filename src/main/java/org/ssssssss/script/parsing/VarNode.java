package org.ssssssss.script.parsing;

public class VarNode {

	private String name;

	private Object value;

	private boolean reference = false;

	VarNode(String name, boolean reference) {
		this.name = name;
		this.reference = reference;
	}

	VarNode(String name, Object value, boolean reference) {
		this.name = name;
		this.value = value;
		this.reference = reference;
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	boolean isReference() {
		return reference;
	}

	VarNode copy() {
		return new VarNode(this.name, value, true);
	}
}