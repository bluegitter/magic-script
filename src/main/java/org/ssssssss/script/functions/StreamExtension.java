package org.ssssssss.script.functions;

import org.ssssssss.script.annotation.Comment;
import org.ssssssss.script.exception.MagicScriptException;
import org.ssssssss.script.parsing.ast.BinaryOperation;
import org.ssssssss.script.parsing.ast.literal.BooleanLiteral;
import org.ssssssss.script.reflection.JavaReflection;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class StreamExtension {

	private static Object toOriginType(Object target, List<Object> results) {
		if (target instanceof Collection) {
			return results;
		} else if (target.getClass().isArray()) {
			return results.toArray();
		} else if (target instanceof Iterator) {
			return results;
		} else if (target instanceof Enumeration) {
			return results;
		}
		return null;
	}

	/**
	 * 将对象转为List
	 */
	@Comment("将对象转为List")
	public static List<Object> arrayLikeToList(Object arrayLike) {
		if (arrayLike != null && arrayLike.getClass().isArray()) {
			int len = Array.getLength(arrayLike);
			List<Object> value = new ArrayList<>();
			for (int i = 0; i < len; i++) {
				value.add(Array.get(arrayLike, i));
			}
			return value;
		} else if (arrayLike instanceof Collection) {
			return new ArrayList<>((Collection<?>) arrayLike);
		} else if (arrayLike.getClass().isArray()) {
			List<Object> list = new ArrayList<>(Array.getLength(arrayLike));
			IntStream.range(0, Array.getLength(arrayLike)).forEach(i -> list.add(Array.get(arrayLike, i)));
			return list;
		} else if (arrayLike instanceof Iterator) {
			List<Object> list = new ArrayList<>();
			Iterator<Object> it = (Iterator<Object>) arrayLike;
			it.forEachRemaining(list::add);
			return list;
		} else if (arrayLike instanceof Enumeration) {
			Enumeration<Object> en = (Enumeration<Object>) arrayLike;
			return Collections.list(en);
		}
		throw new MagicScriptException("不支持的类型:" + arrayLike.getClass());
	}

	/**
	 * map 函数
	 *
	 * @param function 回调函数
	 */
	@Comment("将集合进行转换，并返回新集合")
	public static Object map(Object target, @Comment("转换函数，如提取属性(item)=>item.xxx") Function<Object[], Object> function) {
		List<Object> objects = arrayLikeToList(target);
		List<Object> results = new ArrayList<>(objects.size());
		for (int i = 0, len = objects.size(); i < len; i++) {
			Object object = objects.get(i);
			results.add(function.apply(new Object[]{object, i, len}));
		}
		return toOriginType(target, results);
	}

	/**
	 * 对List进行过滤
	 *
	 * @param function 回调函数
	 */
	@Comment("将集合进行过滤，并返回新集合")
	public static Object filter(Object target, @Comment("过滤条件，如(item)=>item.xxx == 1") Function<Object[], Object> function) {
		List<Object> objects = arrayLikeToList(target);
		List<Object> results = new ArrayList<>(objects.size());
		for (int i = 0, len = objects.size(); i < len; i++) {
			Object object = objects.get(i);
			if (Objects.equals(true, function.apply(new Object[]{object, i, len}))) {
				results.add(object);
			}
		}
		return toOriginType(target, results);
	}

	/**
	 * 循环List
	 *
	 * @param function 回调函数
	 */
	@Comment("将集合进行循环操作，并返回新集合")
	public static Object each(Object target, @Comment("循环函数，如循环添加属性(item)=>{item.xxx = 'newVal'}") Function<Object[], Object> function) {
		List<Object> objects = arrayLikeToList(target);
		List<Object> results = new ArrayList<>(objects.size());
		for (int i = 0, len = objects.size(); i < len; i++) {
			Object object = objects.get(i);
			function.apply(new Object[]{object, i, len});
			results.add(object);
		}
		return toOriginType(target, results);
	}

	/**
	 * 排序
	 */
	@Comment("将集合进行排序，并返回新集合")
	public static Object sort(Object target, @Comment("排序函数，如从大到小(a,b)=>a-b") Function<Object[], Object> function) {
		List<Object> objects = arrayLikeToList(target);
		objects.sort((o1, o2) -> ObjectConvertExtension.asInt(function.apply(new Object[]{o1, o2}), 0));
		return toOriginType(target, objects);
	}

	/**
	 * 反转
	 */
	@Comment("将集合进行反转操作")
	public static Object reserve(Object target) {
		List<Object> objects = arrayLikeToList(target);
		Collections.reverse(objects);
		return toOriginType(target, objects);
	}

	/**
	 * 将list打乱
	 */
	@Comment("将集合的顺序打乱")
	public static Object shuffle(Object target) {
		List<Object> objects = arrayLikeToList(target);
		Collections.shuffle(objects);
		return toOriginType(target, objects);
	}

	/**
	 * 将list拼接起来
	 */
	@Comment("将集合使用`,`拼接起来")
	public static String join(Object target) {
		return join(target, ",");
	}

	/**
	 * 将list拼接起来
	 */
	@Comment("将集合使用连接符拼接起来")
	public static String join(Object target, @Comment("拼接符，如`,`") String separator) {
		List<Object> objects = arrayLikeToList(target);
		StringBuilder sb = new StringBuilder();
		for (int i = 0, len = objects.size(); i < len; i++) {
			sb.append(objects.get(i));
			if (i + 1 < len) {
				sb.append(separator);
			}
		}
		return sb.toString();
	}

	/**
	 * 取最大值
	 */
	@Comment("取出集合最大值，如果找不到返回null")
	public static Object max(Object target) {
		return arrayLikeToList(target).stream()
				.filter(Objects::nonNull)
				.max(BinaryOperation::compare)
				.orElse(null);
	}

	/**
	 * 取最小值
	 */
	@Comment("取出集合最小值，如果找不到返回null")
	public static Object min(Object target) {
		return arrayLikeToList(target).stream()
				.filter(Objects::nonNull)
				.min(BinaryOperation::compare)
				.orElse(null);
	}

	/**
	 * 取平均值
	 */
	@Comment("取出集合平均值，如果无法计算返回null")
	public static Double avg(Object target) {
		OptionalDouble average = arrayLikeToList(target).stream()
				.filter(v -> v instanceof Number)
				.mapToDouble(value -> ((Number) value).doubleValue())
				.average();
		return average.isPresent() ? average.getAsDouble() : null;
	}

	/**
	 * 累计求和
	 */
	@Comment("对集合进行累加操作")
	public static Number sum(Object target) {
		return arrayLikeToList(target).stream()
				.filter(value -> value instanceof Number)
				.mapToDouble(value -> ((Number) value).doubleValue())
				.sum();
	}

	/**
	 * 分组
	 *
	 * @param condition 分组条件
	 */
	@Comment("对集合进行分组")
	public static Map<Object, List<Object>> group(Object target, @Comment("分组条件，如item=>item.xxx + '_' + item.yyy") Function<Object[], Object> condition) {
		return arrayLikeToList(target).stream()
				.collect(Collectors.groupingBy(item -> condition.apply(Stream.of(item).toArray())));
	}

	/**
	 * 分组
	 *
	 * @param condition 分组条件
	 * @param mapping   结果映射
	 */
	@Comment("对集合进行分组并转换")
	public static Map<Object, Object> group(Object target, @Comment("分组条件，如item=>item.xxx + '_' + item.yyy") Function<Object[], Object> condition,
											@Comment("转换函数，如分组求和(list)=>list.sum()") Function<Object[], Object> mapping) {
		return arrayLikeToList(target).stream()
				.collect(Collectors.groupingBy(item -> condition.apply(Stream.of(item).toArray()),
						Collectors.collectingAndThen(Collectors.toList(), list -> mapping.apply(Stream.of(list).toArray()))
						)
				);
	}

	/**
	 * 合并两个集合，类似sql join 操作
	 *
	 * @param source    左表
	 * @param target    右表
	 * @param condition 条件
	 */
	@Comment("将两个集合关联起来")
	public static List<Object> join(Object source, @Comment("另一个集合") Object target, @Comment("关联条件，如:(left,right)=>left.xxx = right.xxx") Function<Object[], Object> condition) {
		return join(source, target, condition, (args) -> {
			Object left = args[0];
			Object right = args[1];
			HashMap<Object, Object> map = new HashMap<>();
			if (left instanceof Map) {
				map.putAll((Map) left);
			}
			if (right instanceof Map) {
				map.putAll((Map) right);
			}
			return map;
		});
	}

	/**
	 * 合并两个集合，类似 sql join 操作
	 *
	 * @param source    左表
	 * @param target    右表
	 * @param condition 条件
	 * @param mapping   映射
	 */
	@Comment("将两个集合关联并转换")
	public static List<Object> join(Object source, @Comment("另一个集合") Object target,
									@Comment("关联条件，如:(left,right)=>left.xxx == right.xxx") Function<Object[], Object> condition,
									@Comment("映射函数，如:(left,right)=>{xxx : left.xxx, yyy : right.yyy}") Function<Object[], Object> mapping) {
		if (target == null) {
			return null;
		}
		List<Object> targetList = arrayLikeToList(target);
		return arrayLikeToList(source).stream()
				// 将匹配结果进行映射
				.map(left -> mapping.apply(
						Stream.of(left,
								targetList.stream()
										// 匹配条件
										.filter(right -> Objects.equals(true, condition.apply(Stream.of(left, right).toArray())))
										// 只取第一条
										.findFirst()
										.orElse(null)
						).toArray()
				)).collect(Collectors.toList());
	}

	@Comment("将集合转为JavaBean")
	public static Object asBean(Object source, @Comment("目标类型") Class<?> target) {
		return asBean(source, target, false);
	}

	@Comment("截取集合")
	public static Object skip(Object source, @Comment("跳过的数量") int value) {
		return toOriginType(source, arrayLikeToList(source).stream().skip(value).collect(Collectors.toList()));
	}

	@Comment("限制集合数量")
	public static Object limit(Object source, @Comment("跳过的数量") int value) {
		return toOriginType(source, arrayLikeToList(source).stream().limit(value).collect(Collectors.toList()));
	}

	@Comment("判断集合是否都满足条件")
	public static boolean every(Object source, @Comment("判断条件") Function<Object[], Object> condition) {
		List<Object> objects = arrayLikeToList(source);
		for (int i = 0, size = objects.size(); i < size; i++) {
			if (!BooleanLiteral.isTrue(condition.apply(new Object[]{objects.get(i), i}))) {
				return false;
			}
		}
		return true;
	}

	@Comment("判断集合中是否至少有一个元素满足条件")
	public static boolean some(Object source, @Comment("判断条件") Function<Object[], Object> condition) {
		List<Object> objects = arrayLikeToList(source);
		for (int i = 0, size = objects.size(); i < size; i++) {
			if (BooleanLiteral.isTrue(condition.apply(new Object[]{objects.get(i), i}))) {
				return true;
			}
		}
		return false;
	}

	@Comment("找到集合中第一个不为null的元素")
	public static Object findNotNull(Object source) {
		List<Object> objects = arrayLikeToList(source);
		for (Object object : objects) {
			if (object != null) {
				return object;
			}
		}
		return null;
	}

	@Comment("循环集合通过给定的计算函数返回一个新值")
	public static Object reduce(Object source, @Comment("处理函数，如累加计算：(val,item)=>val + item") Function<Object[], Object> reduceFunction) {
		List<Object> objects = arrayLikeToList(source);
		if (objects.isEmpty()) {
			return null;
		}
		int size = objects.size();
		if (size == 1) {
			return objects.get(0);
		}
		Object result = objects.get(0);
		for (int i = 1; i < size; i++) {
			result = reduceFunction.apply(new Object[]{result, objects.get(i)});
		}
		return result;
	}

	@Comment("将集合转为JavaBean")
	public static Object asBean(Object source, @Comment("目标类型") Class<?> target, @Comment("是否是数组") boolean isArray) {
		Class<?> sourceClass = source.getClass();
		List<Object> objects = arrayLikeToList(source);
		int size = objects.size();
		boolean isCollection = false;
		boolean isMap = false;
		boolean isOrigin = false;
		boolean targetIsArray = false;
		Class<?> innerClass = null;
		if (size > 0) {
			Object first = objects.get(0);
			innerClass = first.getClass();
			isCollection = ObjectTypeConditionExtension.isCollection(innerClass);
			isMap = ObjectTypeConditionExtension.isMap(innerClass);
			isOrigin = ObjectTypeConditionExtension.is(first, target);
			isOrigin = isOrigin || JavaReflection.isPrimitiveAssignableFrom(first.getClass(), target);
			targetIsArray = ObjectTypeConditionExtension.isArray(target);
		}
		if (isArray) {
			Object result = Array.newInstance(target, objects.size());
			for (int i = 0; i < size; i++) {
				Object value = objects.get(i);
				if (innerClass.isArray()) {
					Array.set(result, i, asBean(value, innerClass.getComponentType(), true));
				} else if (targetIsArray) {
					Array.set(result, i, asBean(value, target.getComponentType(), true));
				} else if (isCollection) {
					Array.set(result, i, asBean(value, innerClass, false));
				} else if (isMap) {
					Array.set(result, i, MapExtension.asBean((Map<String, Object>) value, innerClass));
				} else if (isOrigin) {
					Array.set(result, i, value);
				}
			}
			return result;
		}
		Collection<Object> collection;
		if (List.class.isAssignableFrom(sourceClass)) {
			collection = new ArrayList<>(size);
		} else if (Set.class.isAssignableFrom(sourceClass)) {
			collection = new HashSet<>(size);
		} else {
			throw new MagicScriptException("不支持的类型:" + sourceClass);
		}
		for (int i = 0; i < size; i++) {
			Object value = objects.get(i);
			if (isCollection) {
				collection.add(asBean(value, target));
			} else if (isMap) {
				collection.add(MapExtension.asBean((Map<?, ?>) value, target));
			} else if (isOrigin) {
				collection.add(value);
			} else {
				break;
			}
		}
		return collection;
	}
}
