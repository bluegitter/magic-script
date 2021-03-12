package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicResourceLoader;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class LanguageExpression extends Expression {

	private final String language;

	private final String content;

	public LanguageExpression(Span language, Span content) {
		super(new Span(language, content));
		this.language = language.getText();
		this.content = content.getText();
	}

	@Override
	public Object evaluate(MagicScriptContext context, Scope scope) {
		Map<String, Object> variables = scope.getVariables();
		BiFunction<Map<String, Object>, String, Object> function = MagicResourceLoader.loadScriptLanguage(this.language);
		if(function == null){
			MagicScriptError.error(String.format("language [%s] not found",language), getSpan());
		}
		return (Function<Object[], Object>) objects -> function.apply(variables,content);
	}
}
