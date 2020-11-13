package org.ssssssss.script;

import java.util.Objects;

public class VarNode {

	private String name;

	private Object value;

	private int scopeIndex;

	private int varIndex;

	private int parentScopeIndex = -1;

	public VarNode() {
	}

	public VarNode(int parentScopeIndex) {
		this.parentScopeIndex = parentScopeIndex;
	}

	VarNode(String name) {
		this.name = name;
	}

	VarNode(String name, Object value, int scopeIndex, int varIndex) {
		this.name = name;
		this.value = value;
		this.scopeIndex = scopeIndex;
		this.varIndex = varIndex;
	}

	int getParentScopeIndex() {
		return parentScopeIndex;
	}

	public String getName() {
		return name;
	}

	int getScopeIndex() {
		return scopeIndex;
	}

	void setScopeIndex(int scopeIndex) {
		this.scopeIndex = scopeIndex;
	}

	int getVarIndex() {
		return varIndex;
	}

	void setVarIndex(int varIndex) {
		this.varIndex = varIndex;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Object getValue(MagicScriptContext context) {
		return context.getValue(this);
	}

	public void setValue(MagicScriptContext context, Object value) {
		context.setValue(this, value);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		VarNode node = (VarNode) o;
		return Objects.equals(name, node.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	public VarNode copy() {
		return copy(false);
	}

	public VarNode copy(boolean containsValue) {
		VarNode node = new VarNode(this.name);
		node.varIndex = this.varIndex;
		node.scopeIndex = this.scopeIndex;
		node.parentScopeIndex = this.parentScopeIndex;
		if (containsValue) {
			node.value = this.value;
		}
		return node;
	}

}