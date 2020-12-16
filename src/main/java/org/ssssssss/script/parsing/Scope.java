package org.ssssssss.script.parsing;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class Scope {

	/**
	 * 父作用域
	 */
	private Scope parent;

	/**
	 * 当前作用域的变量
	 */
	private VarNode[] variables;

	private static ThreadLocal<Scope> TEMP_SCOPE = new InheritableThreadLocal<>();

	public static void setTempScope(Scope scope) {
		TEMP_SCOPE.set(scope);
	}

	public static void removeTempScope() {
		TEMP_SCOPE.remove();
	}

	public static Scope getTempScope() {
		return TEMP_SCOPE.get();
	}

	public Scope(Scope parent, int count) {
		this.parent = parent;
		this.variables = new VarNode[count];
	}

	public Scope(Map<String, Object> variables) {
		if (variables != null && !variables.isEmpty()) {
			this.variables = variables.entrySet().stream().map(entry -> new VarNode(entry.getKey(), entry.getValue(), false)).toArray(VarNode[]::new);
		} else {
			this.variables = new VarNode[0];
		}
	}

	public Scope(int count) {
		this(null, count);
	}

	/**
	 * 根据变量名获取值
	 */
	public Object getValue(String name) {
		VarNode varNode = getVarNode(name);
		return varNode == null ? null : varNode.getValue();
	}

	public Map<String, Object> getVariables() {
		Map<String, Object> vars = new HashMap<>();
		Stack<VarNode> stack = new Stack<>();
		Scope scope = this;
		do {
			for (int i = 0, len = scope.variables.length; i < len; i++) {
				VarNode node = scope.variables[i];
				if (node != null) {
					stack.push(node);
				}
			}
			scope = scope.parent;
		} while (scope != null);
		while (!stack.isEmpty()) {
			VarNode node = stack.pop();
			vars.put(node.getName(), node.getValue());
		}
		return vars;
	}

	/**
	 * 根据变量名查找节点
	 */
	private VarNode getVarNode(String name) {
		for (int index = variables.length - 1; index >= 0; index--) {
			VarNode node = variables[index];
			if (node != null && node.getName().equals(name)) {
				return node;
			}
		}
		return parent != null ? parent.getVarNode(name) : null;
	}

	/**
	 * 获取值
	 */
	public Object getValue(VarIndex varIndex) {
		return getVarNode(varIndex).getValue();
	}

	private VarNode getVarNode(VarIndex varIndex) {
		VarNode varNode = variables[varIndex.getIndex()];
		if (varNode == null) {
			if (parent != null) {
				varNode = parent.getVarNode(varIndex.getName());
			}
			if (varNode == null) {
				varNode = new VarNode(varIndex.getName(), varIndex.isReference());
			} else if (!varIndex.isReference()) {
				varNode = varNode.copy();
			}
			variables[varIndex.getIndex()] = varNode;
		}
		return varNode;
	}

	/**
	 * 设置值
	 */
	public Object setValue(VarIndex varIndex, Object value) {
		getVarNode(varIndex).setValue(value);
		return value;
	}

	public int find(VarIndex varIndex) {
		for (int index = variables.length; index >= 0; index--) {
			VarNode varNode = getVarNode(varIndex);
			if (varIndex.getName().equals(varNode.getName())) {
				return index;
			}
		}
		return -1;
	}

	/**
	 * 创建新的作用域
	 */
	public Scope create(int count) {
		return new Scope(this, count);
	}

}
