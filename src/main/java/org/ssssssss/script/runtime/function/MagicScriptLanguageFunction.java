package org.ssssssss.script.runtime.function;

import org.ssssssss.script.MagicResourceLoader;
import org.ssssssss.script.MagicScriptContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class MagicScriptLanguageFunction implements MagicScriptLambdaFunction{

	private final BiFunction<Map<String, Object>, String, Object> function;

	private final String content;

	public MagicScriptLanguageFunction(String language, String content) {
		function = MagicResourceLoader.loadScriptLanguage(language);
		this.content = content;
	}

	@Override
	public Object apply(MagicScriptContext context, Object[] args) {
		return function.apply(context.getVariables(), this.content);
	}
}
