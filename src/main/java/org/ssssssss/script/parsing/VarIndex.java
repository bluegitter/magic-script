package org.ssssssss.script.parsing;

public class VarIndex {

	private final String name;

	private final int index;

	private final boolean reference;

	VarIndex(String name, int index, boolean reference) {
		this.name = name;
		this.index = index;
		this.reference = reference;
	}

	public String getName() {
		return name;
	}

	public int getIndex() {
		return index;
	}

	public boolean isReference() {
		return reference;
	}
}
