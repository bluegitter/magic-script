package org.ssssssss.script.functions;

import org.ssssssss.script.annotation.Comment;
import org.ssssssss.script.annotation.Function;
import org.ssssssss.script.parsing.ast.BinaryOperation;

import java.util.Map;
import java.util.OptionalDouble;
import java.util.function.BinaryOperator;

/**
 * 聚合函数
 */
public class AggregationFunctions {

	@Function
	@Comment("聚合函数-count")
	public static int count(Object target) {
		if (target == null) {
			return 0;
		} else if (target instanceof Map) {
			return 1;
		}
		try {
			return StreamExtension.arrayLikeToList(target).size();
		} catch (Exception e) {
			return 1;
		}
	}

	@Function
	@Comment("聚合函数-max")
	public static Object max(Object target) {
		if (target == null) {
			return null;
		} else if (target instanceof Map) {
			return target;
		}
		try {
			return StreamExtension.arrayLikeToList(target).stream()
					.reduce(BinaryOperator.maxBy(BinaryOperation::compare))
					.orElse(null);
		} catch (Exception e) {
			return null;
		}
	}

	@Function
	@Comment("聚合函数-min")
	public static Object min(Object target) {
		if (target == null) {
			return null;
		} else if (target instanceof Map) {
			return target;
		}
		try {
			return StreamExtension.arrayLikeToList(target).stream()
					.reduce(BinaryOperator.minBy(BinaryOperation::compare))
					.orElse(null);
		} catch (Exception e) {
			return null;
		}
	}

	@Function
	@Comment("聚合函数-avg")
	public static Object avg(Object target) {
		if (target == null) {
			return null;
		} else if (target instanceof Map) {
			return target;
		}
		try {
			OptionalDouble average = StreamExtension.arrayLikeToList(target).stream()
					.mapToDouble(v -> ObjectConvertExtension.asDouble(v, Double.NaN))
					.filter(v -> !Double.isNaN(v))
					.average();
			return average.isPresent() ? average.getAsDouble() : null;
		} catch (Exception e) {
			return null;
		}
	}
}
