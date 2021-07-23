package org.ssssssss.script.runtime.linq;

import org.ssssssss.script.runtime.function.MagicScriptLambdaFunction;

import java.util.List;

public class LinQJoinValue {

	private MagicScriptLambdaFunction condition;

	private List<Object> target;

	private boolean isLeftJoin;

	private String aliasName;

	private int aliasIndex = -1;

	public LinQJoinValue(List<Object> target, int aliasIndex) {
		this.target = target;
		this.aliasIndex = aliasIndex;
	}

	public LinQJoinValue(MagicScriptLambdaFunction condition, List<Object> objects, boolean isLeftJoin, String aliasName, int aliasIndex) {
		this.condition = condition;
		this.target = objects;
		this.isLeftJoin = isLeftJoin;
		this.aliasName = aliasName;
		this.aliasIndex = aliasIndex;
	}

	public MagicScriptLambdaFunction getCondition() {
		return condition;
	}

	public void addValue(Object value){
		target.add(value);
	}

	public int size(){
		return target.size();
	}

	public List<Object> getTarget() {
		return target;
	}

	public boolean isLeftJoin() {
		return isLeftJoin;
	}

	public String getAliasName() {
		return aliasName;
	}

	public int getAliasIndex() {
		return aliasIndex;
	}
}
