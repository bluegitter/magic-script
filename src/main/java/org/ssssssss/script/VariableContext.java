package org.ssssssss.script;

import java.util.*;
import java.util.stream.Collectors;

public class VariableContext implements Cloneable {

	private List<List<VarNode>> variables = new ArrayList<>(0);

	private Stack<Integer> scope = new Stack<>();

	private int currentScope = 0;

	private int runtimeScope = 0;

	public VariableContext() {
		this.variables.add(createEmptyStack(-1));
	}

	/**
	 * 创建一个空的
	 *
	 * @param parentScope
	 * @return
	 */
	private List<VarNode> createEmptyStack(int parentScope) {
		List<VarNode> list = new ArrayList<>(1);
		list.add(new VarNode(parentScope));
		return list;
	}

	/**
	 * 添加一个变量（可共享）
	 */
	public VarNode add(String name) {
		VarNode node = new VarNode(name);
		int[] pos = find(node);
		node = variables.get(pos[0]).get(pos[1]);
		node.setScopeIndex(pos[0]);
		node.setVarIndex(pos[1]);
		return node;
	}


	/**
	 * 强制新增一个变量（用于作用域切换）
	 */
	public VarNode forceAdd(String name) {
		if (currentScope == 0) {
			throw new NullPointerException(name);
		}
		VarNode varNode = new VarNode(name);
		int scopeIndex = currentScope;
		List<VarNode> current = variables.get(scopeIndex);
		varNode.setScopeIndex(scopeIndex);
		varNode.setVarIndex(current.size());
		current.add(varNode);
		return varNode;
	}

	/**
	 * 获取当前作用栈变量信息
	 */
	Map<String, Object> getCurrentVariables(MagicScriptContext context) {
		int scopeIndex = runtimeScope;
		Map<String, Object> vars = new LinkedHashMap<>();
		do {
			List<VarNode> nodes = variables.get(scopeIndex);
			for (int i = nodes.size() - 1; i > 0; i--) {
				VarNode varNode = nodes.get(i);
				vars.put(varNode.getName(), context.getValue(varNode));
			}
			scopeIndex = nodes.get(0).getParentScopeIndex();
		} while (scopeIndex != -1);
		return vars;
	}

	/**
	 * 从顶部作用域中获取值
	 */
	public VarNode get(String name) {
		return variables.get(0).stream().filter(it -> Objects.equals(name, it.getName())).findFirst().orElse(null);
	}

	/**
	 * 获取变量值
	 */
	protected Object getValue(VarNode varNode) {
		return variables.get(varNode.getScopeIndex()).get(varNode.getVarIndex()).getValue();
	}

	/**
	 * 向上寻找变量并获取值
	 */
	public Object findAndGet(VarNode varNode) {
		int currentScope = variables.get(varNode.getScopeIndex()).get(0).getParentScopeIndex();
		int scopeIndex = currentScope;
		List<VarNode> current = variables.get(currentScope);
		int index = current.indexOf(varNode);
		if (index == -1) {    //当前作用域中有，则直接返回
			List<VarNode> parent = current;
			while ((scopeIndex = parent.get(0).getParentScopeIndex()) != -1) {    // 向上寻找是否存在同名变量
				parent = variables.get(scopeIndex);
				if ((index = parent.indexOf(varNode)) != -1) {
					break;
				}
			}
		}
		return index == -1 ? null : variables.get(scopeIndex).get(index).getValue();
	}

	/**
	 * 获取变量值
	 */
	public Object get(int scope, int index) {
		this.runtimeScope = scope;
		if (variables.size() > scope) {
			List<VarNode> objects = variables.get(scope);
			if (objects.size() > index) {
				return objects.get(index).getValue();
			}
		}
		return null;
	}

	/**
	 * 设置变量值
	 */
	public void set(int scope, int index, Object value) {
		this.runtimeScope = scope;
		if (variables.size() > scope) {
			List<VarNode> objects = variables.get(scope);
			if (objects.size() > index) {
				objects.get(index).setValue(value);
			}
		}
	}

	/**
	 * 查找变量位置
	 */
	private int[] find(VarNode node) {

		List<VarNode> current = variables.get(currentScope);
		int index = current.lastIndexOf(node);
		if (index != -1) {    //当前作用域中有，则直接返回
			return new int[]{currentScope, index};
		}
		int scopeIndex;
		current.add(node);
		List<VarNode> parent = current;
		while ((scopeIndex = parent.get(0).getParentScopeIndex()) != -1) {    // 向上寻找是否存在同名变量
			parent = variables.get(scopeIndex);
			if ((index = parent.lastIndexOf(node)) != -1) {
				return new int[]{scopeIndex, index};
			}
		}
		return new int[]{currentScope, current.size() - 1};
	}

	/**
	 * 设置当前作用域
	 */
	void setCurrentScope(int currentScope) {
		this.currentScope = currentScope;
	}

	/**
	 * 获取运行时作用域
	 */
	int getRuntimeScope() {
		return this.runtimeScope;
	}


	/**
	 * 增加一个作用域
	 */
	public void push() {
		variables.add(createEmptyStack(currentScope));
		scope.push(currentScope);
		currentScope = variables.size() - 1;
	}

	/**
	 * 减少一个作用域
	 */
	public void pop() {
		currentScope = scope.pop();
	}

	/**
	 * 添加变量至作用域中
	 */
	public void add(Map<String, Object> map) {
		if (map != null) {
			if (variables.size() == 0) {
				variables.add(new ArrayList<>());
			}
			List<VarNode> nodeList = variables.get(0);
			map.forEach((key, value) -> {
				nodeList.add(new VarNode(key, value, 0, nodeList.size() - 1));
			});
		}
	}

	/**
	 * 复制变量上下文（不复制值）
	 */
	public VariableContext copy() {
		return copy(false);
	}

	/**
	 * 复制变量上下文
	 */
	public VariableContext copy(boolean containsValue) {
		VariableContext context = new VariableContext();
		context.variables = variables.stream().map(list -> list.stream().map(it -> it.copy(containsValue)).collect(Collectors.toList())).collect(Collectors.toList());
		context.runtimeScope = runtimeScope;
		return context;
	}
}
