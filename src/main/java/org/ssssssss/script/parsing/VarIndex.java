package org.ssssssss.script.parsing;

import java.util.Objects;

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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof VarIndex)) return false;
		VarIndex varIndex = (VarIndex) o;
		return index == varIndex.index && reference == varIndex.reference && Objects.equals(name, varIndex.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, index, reference);
	}
}
