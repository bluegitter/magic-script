package org.ssssssss.script.runtime.linq;

import org.ssssssss.script.runtime.function.MagicScriptLambdaFunction;

public class LinQOrder {

	private MagicScriptLambdaFunction function;

	private int order;

	public LinQOrder(MagicScriptLambdaFunction function, int order) {
		this.function = function;
		this.order = order;
	}

	public MagicScriptLambdaFunction getFunction() {
		return function;
	}

	public int getOrder() {
		return order;
	}
}
