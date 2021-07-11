package org.ssssssss.script.runtime.function;

import org.ssssssss.script.MagicScriptContext;

@FunctionalInterface
public interface MagicScriptLambdaFunction {

	Object apply(MagicScriptContext context, Object[] args);
}
