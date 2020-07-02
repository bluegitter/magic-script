package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.functions.ClassExtension;
import org.ssssssss.script.parsing.Span;

import java.util.List;

public class NewStatement extends Expression{

	private List<Expression> arguments;

	private String target;

	public NewStatement(Span span,String target, List<Expression> arguments) {
		super(span);
		this.target = target;
		this.arguments = arguments;
	}

	@Override
	public Object evaluate(MagicScriptContext context) {
		Object clazz = context.get(target);
		if(clazz instanceof Class){
			Class<?> cls = (Class<?>) clazz;
			Object[] args = new Object[arguments.size()];
			for (int i = 0; i < args.length; i++) {
				args[i] = arguments.get(i).evaluate(context);
			}
			try {
				return ClassExtension.newInstance(cls,args);
			} catch (Throwable t) {
				MagicScriptError.error("class "+target+" can not newInstance." ,getSpan() ,t);
			}
		}else{
			MagicScriptError.error("class "+target+" not found" ,getSpan());
		}
		return null;
	}
}
