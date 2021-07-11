package org.ssssssss.script.parsing.ast.statement;

import org.ssssssss.script.compile.MagicScriptCompiler;
import org.ssssssss.script.exception.MagicExitException;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Expression;
import org.ssssssss.script.parsing.ast.Node;

import java.util.Arrays;
import java.util.List;

public class Exit extends Node {

	private final List<Expression> expressions;

	public Exit(Span span,List<Expression> expressions) {
		super(span);
		this.expressions = expressions;
	}

	@Override
	public void visitMethod(MagicScriptCompiler compiler) {
		expressions.forEach(it -> it.visitMethod(compiler));
	}

	@Override
	public void compile(MagicScriptCompiler compiler) {
		compiler.typeInsn(NEW,MagicExitException.class)
				.insn(DUP)
				.typeInsn(NEW, Value.class)
				.insn(DUP);
		if(expressions == null){
			compiler.invoke(INVOKESPECIAL, Value.class,"<init>", void.class);
		}else{
			compiler.visitInt(expressions.size())
					.typeInsn(ANEWARRAY, Object.class);
			for (int i = 0, size = expressions.size(); i < size; i++) {
				compiler.insn(DUP)
						.visitInt(i)
						.visit(expressions.get(i))
						.insn(AASTORE);
			}
			compiler.invoke(INVOKESPECIAL, Value.class,"<init>", void.class, Object[].class);
		}
		compiler.invoke(INVOKESPECIAL, MagicExitException.class,"<init>", void.class, Value.class)
				.insn(ATHROW);
	}

	public static class Value{

		private Object[] values;

		public Value() {
			this(new Object[0]);
		}

		public Value(Object[] values) {
			this.values = values;
		}

		public Object[] getValues() {
			return values;
		}

		public int getLength(){
			return values.length;
		}

		@Override
		public String toString() {
			return "Value{" +
					"values=" + Arrays.toString(values) +
					'}';
		}
	}
}
