package org.ssssssss.script.runtime.linq;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.functions.MapExtension;
import org.ssssssss.script.functions.StreamExtension;
import org.ssssssss.script.runtime.function.MagicScriptLambdaFunction;
import org.ssssssss.script.runtime.handle.OperatorHandle;

import java.util.*;
import java.util.stream.Collectors;

public class LinQBuilder {

	private final MagicScriptContext context;

	private static final Object[] EMPTY_PARAMETER = new Object[0];

	private List<Object> fromObjects;

	private int fromAliasIndex;

	private final List<SelectField> selects = new ArrayList<>();

	private MagicScriptLambdaFunction where;

	private MagicScriptLambdaFunction having;

	private final List<MagicScriptLambdaFunction> groups = new ArrayList<>();

	private final List<LinQJoinValue> joins = new ArrayList<>();

	private final List<LinQOrder> orders = new ArrayList<>();

	private LinQBuilder(MagicScriptContext context) {
		this.context = context;
	}

	public static LinQBuilder create(MagicScriptContext context) {
		return new LinQBuilder(context);
	}

	public LinQBuilder where(MagicScriptLambdaFunction condition) {
		where = condition;
		return this;
	}

	public LinQBuilder from(Object object, int aliasIndex) {
		this.fromAliasIndex = aliasIndex;
		this.fromObjects = convertToList(object);
		return this;
	}

	private List<Object> convertToList(Object object) {
		if (object instanceof Map) {
			return (List<Object>) MapExtension.asList((Map<?, ?>) object, (entry) -> Collections.singletonMap(entry[0], entry[1]));
		} else {
			try {
				return StreamExtension.arrayLikeToList(object);
			} catch (Exception e) {
				return Collections.singletonList(object);
			}
		}
	}

	public LinQBuilder group(MagicScriptLambdaFunction function) {
		groups.add(function);
		return this;
	}

	public LinQBuilder join(MagicScriptLambdaFunction condition, Object object, boolean isLeftJoin, String aliasName, int aliasIndex) {
		joins.add(new LinQJoinValue(condition, convertToList(object), isLeftJoin, aliasName, aliasIndex));
		return this;
	}

	public LinQBuilder having(MagicScriptLambdaFunction condition) {
		having = condition;
		return this;
	}

	public LinQBuilder order(MagicScriptLambdaFunction function, int order) {
		this.orders.add(new LinQOrder(function, order));
		return this;
	}

	public LinQBuilder select(MagicScriptLambdaFunction function, String aliasName, int aliasIndex) {
		selects.add(new SelectField(function, aliasName, aliasIndex));
		return this;
	}

	public Object execute() {
		List<Record> records = new ArrayList<>();
		for (Object object : fromObjects) {
			context.setVarValue(fromAliasIndex, object);
			// 处理 join + where
			processWhere(processJoin(object), records, object);
		}
		// 处理 group + having
		records = processGroup(records);
		// 处理 select
		List<SelectValue> result = processSelect(records);

		return result.stream().sorted().map(SelectValue::getValue).collect(Collectors.toList());
	}

	private void processRow(Object item, Map<String, Object> row, SelectField field){
		if (item instanceof Map) {
			row.putAll((Map<String, Object>) item);
		} else {
			row.put(field.getAliasName(), item);
		}
	}

	private List<SelectValue> processSelect(List<Record> records) {
		List<SelectValue> result = new ArrayList<>();
		int fieldSize = selects.size();
		for (Record record : records) {
			Map<String, Object> row = new LinkedHashMap<>(fieldSize);
			context.setVarValue(fromAliasIndex, record.getValue());
			if (record.getJoinIndex() != -1) {
				context.setVarValue(record.getJoinIndex(), record.getJoinValue());
			}
			for (SelectField field : selects) {
				MagicScriptLambdaFunction function = field.getFunction();
				if(function == null){
					processRow(record.getValue(), row, field);
					if(record.getJoinValue() != null){
						processRow(record.getJoinValue(), row, field);
					}
				}else{
					Object item = function.apply(context, EMPTY_PARAMETER);
					processRow(item, row, field);
				}

			}
			List<OrderValue> orderValues = new ArrayList<>();
			if (!orders.isEmpty()) {
				for (LinQOrder order : orders) {
					orderValues.add(new OrderValue(order.getFunction().apply(context, EMPTY_PARAMETER), order.getOrder()));
				}
			}
			result.add(new SelectValue(row, orderValues));
		}
		return result;
	}

	private List<Record> processGroup(List<Record> records) {
		if (!groups.isEmpty()) {
			Map<List<Object>, List<Record>> group = new LinkedHashMap<>();
			for (Record record : records) {
				context.setVarValue(fromAliasIndex, record.getValue());
				if (record.getJoinIndex() != -1) {
					context.setVarValue(record.getJoinIndex(), record.getJoinValue());
				}
				List<Object> keys = groups.stream().map(field -> field.apply(context, EMPTY_PARAMETER)).collect(Collectors.toList());
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
					values.add(item.getValue());
					if (record.getJoinIndex() != -1) {
						context.setVarValue(record.getJoinIndex(), record.getJoinValue());
						joinValues.add(item.getJoinValue());
					}
				}
				boolean valid = having == null;
				if (!valid) {
					context.setVarValue(fromAliasIndex, record.getValue());
					valid = OperatorHandle.isTrue(having.apply(context, EMPTY_PARAMETER));
				}
				if (valid) {
					record.setValue(values);
					record.setJoinValue(joinValues);
					records.add(record);
				}
			}
		}
		return records;
	}

	private void processWhere(List<LinQJoinValue> joinValues, List<Record> records, Object object) {
		if (where != null) {
			int maxSize = joinValues.stream().mapToInt(LinQJoinValue::size).sum();
			for (LinQJoinValue joinValue : joinValues) {
				List<Object> values = joinValue.getTarget();
				if (values.size() > 0) {
					context.setVarValue(joinValue.getAliasIndex(), values);
					for (int i = 0; i < maxSize; i++) {
						Object value = values.get(Math.min(values.size() - 1, i));
						if (OperatorHandle.isTrue(where.apply(context, EMPTY_PARAMETER))) {
							records.add(new Record(object, value, joinValue.getAliasIndex()));
						}
					}
				}
			}
		} else if (!joins.isEmpty()) {
			for (int i = 0, size = joins.size(); i < size; i++) {
				LinQJoinValue joinValue = joinValues.get(i);
				List<Object> values = joinValue.getTarget();
				if (joins.get(i).isLeftJoin()) {
					if (!values.isEmpty()) {
						for (Object value : values) {
							records.add(new Record(object, value, joinValue.getAliasIndex()));
						}
					} else {
						records.add(new Record(object, Collections.emptyMap(), joinValue.getAliasIndex()));
					}
				} else {
					if (!values.isEmpty()) {
						records.add(new Record(object, values.get(0), joinValue.getAliasIndex()));
					}
				}
			}
		} else {
			records.add(new Record(object));
		}
	}

	private List<LinQJoinValue> processJoin(Object object) {
		List<LinQJoinValue> joinValues = new ArrayList<>();
		if (joins.isEmpty()) {
			joinValues.add(new LinQJoinValue(Collections.singletonList(object), -1));
		} else {
			joins.forEach(join -> {
				LinQJoinValue joinResult = new LinQJoinValue(new ArrayList<>(), join.getAliasIndex());
				for (Object joinItem : join.getTarget()) {
					context.setVarValue(join.getAliasIndex(), joinItem);
					if (OperatorHandle.isTrue(join.getCondition().apply(context, EMPTY_PARAMETER))) {
						joinResult.addValue(joinItem);
						if (join.isLeftJoin()) {
							break;
						}
					}
				}
				joinValues.add(joinResult);
			});
		}
		return joinValues;
	}
}
