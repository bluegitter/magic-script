package org.ssssssss.script.parsing.ast.statement;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.functions.ObjectConvertExtension;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Expression;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class ClassConverter extends Expression {

	private final Expression target;

	private final String convert;

	private final List<Expression> arguments;

	private final static Map<String, BiFunction<Object, Object[], Object>> converters = new HashMap<>();


	static {
		register("int", BigDecimal::intValue);
		register("double", BigDecimal::doubleValue);
		register("long", BigDecimal::longValue);
		register("byte", BigDecimal::byteValue);
		register("float", BigDecimal::floatValue);
		register("short", BigDecimal::shortValue);
		register("string", (target, params) -> process(target::toString, params));
		register("date", (target, params) -> {
			try {
				if (params.length == 0) {
					throw new IllegalArgumentException("::date需要日期格式，如::date('yyyy-mm-dd')");
				}
				return ObjectConvertExtension.asDate(target, params[0].toString());
			} catch (Exception e) {
				return null;
			}
		});
	}

	private static void register(String target, Function<BigDecimal, Object> converter) {
		register(target, (value, params) -> {
			try {
				return converter.apply(ObjectConvertExtension.asDecimal(value));
			} catch (Exception e) {
				return params.length > 0 ? params[0] : null;
			}
		});
	}

	private static Object process(Supplier<Object> callback, Object[] params) {
		try {
			return callback.get();
		} catch (Exception e) {
			return params.length > 0 ? params[0] : null;
		}
	}

	public ClassConverter(Span span, String convert, Expression target, List<Expression> arguments) {
		super(span);
		this.convert = convert;
		this.target = target;
		this.arguments = arguments;
	}

	@Override
	public Object evaluate(MagicScriptContext context, Scope scope) {
		BiFunction<Object, Object[], Object> function = converters.get(convert);
		if (function == null) {
			MagicScriptError.error(String.format("找不到转换器[%s]", convert), getSpan());
		}
		return function.apply(target.evaluate(context, scope), arguments.stream().map(it -> it.evaluate(context, scope)).toArray());
	}

	public static void register(String target, BiFunction<Object, Object[], Object> converter) {
		converters.put(target, converter);
	}
}
