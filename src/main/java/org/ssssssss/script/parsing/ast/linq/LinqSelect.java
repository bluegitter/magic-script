package org.ssssssss.script.parsing.ast.linq;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.compile.MagicScriptCompiler;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Expression;
import org.ssssssss.script.runtime.function.MagicScriptLambdaFunction;
import org.ssssssss.script.runtime.linq.LinQBuilder;

import java.util.List;

public class LinqSelect extends Expression {

	private final List<LinqField> fields;

	private final LinqField from;

	private final List<LinqJoin> joins;

	private final LinqExpression where;

	private final List<LinqField> groups;

	private final LinqExpression having;

	private final List<LinqOrder> orders;


	public LinqSelect(Span span, List<LinqField> fields, LinqField from, List<LinqJoin> joins, LinqExpression where, List<LinqField> groups, LinqExpression having, List<LinqOrder> orders) {
		super(span);
		this.fields = fields;
		this.from = from;
		this.joins = joins;
		this.where = where;
		this.groups = groups;
		this.having = having;
		this.orders = orders;
	}

	@Override
	public void visitMethod(MagicScriptCompiler compiler) {
		fields.forEach(it -> it.visitMethod(compiler));
		joins.forEach(it -> it.visitMethod(compiler));
		groups.forEach(it -> it.visitMethod(compiler));
		orders.forEach(it -> it.visitMethod(compiler));
		if(where != null){
			where.visitMethod(compiler);
		}
		if(having != null){
			having.visitMethod(compiler);
		}
	}

	@Override
	public void compile(MagicScriptCompiler compiler) {
		compiler.load1()
				.invoke(INVOKESTATIC, LinQBuilder.class, "create", LinQBuilder.class, MagicScriptContext.class)
				.visit(from.getExpression())
				.visitInt(from.getVarIndex().getIndex())
				.invoke(INVOKEVIRTUAL, LinQBuilder.class, "from", LinQBuilder.class, Object.class, int.class);
		if(where != null){
			compiler.visit(where)
					.invoke(INVOKEVIRTUAL, LinQBuilder.class, "where", LinQBuilder.class, MagicScriptLambdaFunction.class);
		}
		if(having != null){
			compiler.visit(where)
					.invoke(INVOKEVIRTUAL, LinQBuilder.class, "having", LinQBuilder.class, MagicScriptLambdaFunction.class);
		}
		groups.forEach(group -> compiler.visit(group)
				.invoke(INVOKEVIRTUAL, LinQBuilder.class, "group", LinQBuilder.class, MagicScriptLambdaFunction.class));
		joins.forEach(compiler::visit);
		fields.forEach(field-> compiler.visit(field)
				.ldc(field.getAlias())
				.visitInt(field.getVarIndex() == null ? -1 : field.getVarIndex().getIndex())
				.invoke(INVOKEVIRTUAL, LinQBuilder.class, "select", LinQBuilder.class, MagicScriptLambdaFunction.class, String.class, int.class));
		orders.forEach(order -> compiler.visit(order)
				.visitInt(order.getOrder())
				.invoke(INVOKEVIRTUAL, LinQBuilder.class, "order", LinQBuilder.class, MagicScriptLambdaFunction.class, int.class));
		compiler.invoke(INVOKEVIRTUAL, LinQBuilder.class, "execute", Object.class);
	}
}
