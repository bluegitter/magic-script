package org.ssssssss.script.parsing.ast.linq;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.BinaryOperation;
import org.ssssssss.script.parsing.ast.Expression;
import org.ssssssss.script.parsing.ast.literal.BooleanLiteral;

import java.util.*;
import java.util.stream.Collectors;

public class LinqSelect extends Expression {

	private final List<LinqField> fields;

	private final LinqField from;

	private final List<LinqJoin> joins;

	private final Expression where;

	private final List<LinqField> groups;

	private final Expression having;

	private final List<LinqOrder> orders;


	public LinqSelect(Span span, List<LinqField> fields, LinqField from, List<LinqJoin> joins, Expression where, List<LinqField> groups, Expression having, List<LinqOrder> orders) {
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
	public Object evaluate(MagicScriptContext context, Scope scope) {
		List<Object> objects = from.evaluateList(context, scope);
		List<SelectValue> result = new ArrayList<>();
		Collection<Record> records = new ArrayList<>();
		for (Object object : objects) {
			from.setValue(context, scope, object);
			List<List<Object>> joinValues = new ArrayList<>();
			// 处理关联
			for (LinqJoin join : joins) {
				joinValues.add(join.evaluate(context, scope));
			}
			if (joins.isEmpty()) {
				joinValues.add(Collections.singletonList(object));
			}
			// 处理where
			if (where != null) {
				int maxSize = joinValues.stream().mapToInt(List::size).sum();
				for (int v = 0, size = joinValues.size(); v < size; v++) {
					List<Object> values = joinValues.get(v);
					if (!values.isEmpty()) {
						LinqJoin join = joins.isEmpty() ? null : joins.get(v);
						for (int i = 0; i < maxSize; i++) {
							Object value = values.get(Math.min(values.size() - 1, i));
							if (join != null) {
								join.getTarget().setValue(context, scope, value);
							}
							if (BooleanLiteral.isTrue(where.evaluate(context, scope))) {
								records.add(new Record(object, join, join == null ? null : value));
							}
						}
					}
				}
			} else if (!joins.isEmpty()) {
				for (int i = 0, size = joins.size(); i < size; i++) {
					List<Object> values = joinValues.get(i);
					if (!values.isEmpty()) {
						records.add(new Record(object, joins.get(i), values.get(0)));
					}
				}
			} else {
				records.add(new Record(object));
			}
		}

		//  group
		if (!groups.isEmpty()) {
			Map<List<Object>, List<Record>> group = new LinkedHashMap<>();
			for (Record record : records) {
				from.setValue(context, scope, record.value);
				if (record.join != null) {
					record.join.getTarget().setValue(context, scope, record.joinValue);
				}
				List<Object> keys = groups.stream().map(field -> field.evaluate(context, scope)).collect(Collectors.toList());
				List<Record> groupRecords = group.computeIfAbsent(keys, k -> new ArrayList<>());
				groupRecords.add(record);
			}
			records = new ArrayList<>();
			for (Map.Entry<List<Object>, List<Record>> entry : group.entrySet()) {
				List<Record> value = entry.getValue();
				Record record = value.get(0);
				List<Object> values = new ArrayList<>(value.size());
				List<Object> joinValues = new ArrayList<>(value.size());
				for (Record item : value) {
					values.add(item.value);
					if (item.join != null) {
						item.join.getTarget().setValue(context, scope, item.joinValue);
						joinValues.add(item.joinValue);
					}
				}
				boolean valid = having == null;
				if (!valid) {
					from.setValue(context, scope, values);
					valid = BooleanLiteral.isTrue(having.evaluate(context, scope));
				}
				if (valid) {
					record.value = values;
					record.joinValue = joinValues;
					records.add(record);
				}
			}
		}

		// 处理 select
		for (Record record : records) {
			Map<String, Object> row = new LinkedHashMap<>(fields.size());
			from.setValue(context, scope, record.value);
			if (record.join != null) {
				record.join.getTarget().setValue(context, scope, record.joinValue);
			}
			for (LinqField field : fields) {
				Object item;
				if (field.getExpression() instanceof WholeLiteral) {    // *的特殊处理
					// TODO 检查类型，不是Map的需要转换为Map
					Map map = (Map) record.value;
					map = map == null ? new LinkedHashMap() : map;
					if (record.joinValue != null) {
						map.putAll((Map) record.joinValue);
					}
					item = map;
				} else {    // 获取单个字段
					item = field.evaluate(context, scope);
				}
				if (item instanceof Map) {
					row.putAll((Map) item);
				} else {
					row.put(field.getAlias(), item);
				}
			}
			List<OrderValue> orderValues = new ArrayList<>();
			if (!orders.isEmpty()) {
				for (LinqOrder order : orders) {
					orderValues.add(new OrderValue(order.evaluate(context, scope), order.getOrder()));
				}
			}
			result.add(new SelectValue(row, orderValues));
		}
		return result.stream().sorted().map(SelectValue::getValue).collect(Collectors.toList());
	}

	static class Record {

		private Object value;

		private LinqJoin join;

		private Object joinValue;

		public Record(Object value) {
			this.value = value;
		}

		public Record(Object value, LinqJoin join, Object joinValue) {
			this.value = value;
			this.join = join;
			this.joinValue = joinValue;
		}

		@Override
		public String toString() {
			return "Record{" +
					"value=" + value +
					", join=" + join +
					", joinValue=" + joinValue +
					'}';
		}
	}

	static class SelectValue implements Comparable<SelectValue> {

		Map<String, Object> value;

		List<OrderValue> orderValues;

		boolean hasOrder;

		public SelectValue(Map<String, Object> value, List<OrderValue> orderValues) {
			this.value = value;
			this.orderValues = orderValues;
			this.hasOrder = !orderValues.isEmpty();
		}

		public Map<String, Object> getValue() {
			return value;
		}

		@Override
		public int compareTo(SelectValue o2) {
			if (!hasOrder) {
				return 0;
			}
			for (int i = 0, size = orderValues.size(); i < size; i++) {
				OrderValue ov1 = orderValues.get(i);
				OrderValue ov2 = o2.orderValues.get(i);
				int compareValue = BinaryOperation.compare(ov1.getValue(), ov2.getValue());
				if (compareValue != 0) {
					return compareValue * ov1.getOrder();
				}
			}
			return 0;
		}
	}

	static class OrderValue {
		private Object value;
		private int order;

		public OrderValue(Object value, int order) {
			this.value = value;
			this.order = order;
		}

		public Object getValue() {
			return value;
		}

		public int getOrder() {
			return order;
		}
	}
}
