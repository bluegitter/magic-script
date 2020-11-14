package org.ssssssss.script.parsing.ast.statement;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Expression;
import org.ssssssss.script.parsing.ast.VariableSetter;

import java.util.List;
import java.util.Map;

public class MapOrArrayAccess extends Expression implements VariableSetter {
	private final Expression mapOrArray;
	private final Expression keyOrIndex;

	public MapOrArrayAccess(Span span, Expression mapOrArray, Expression keyOrIndex) {
		super(span);
		this.mapOrArray = mapOrArray;
		this.keyOrIndex = keyOrIndex;
	}

	/**
	 * Returns an expression that must evaluate to a map or array.
	 **/
	public Expression getMapOrArray() {
		return mapOrArray;
	}

	/**
	 * Returns an expression that is used as the key or index to fetch a map or array element.
	 **/
	public Expression getKeyOrIndex() {
		return keyOrIndex;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object evaluate(MagicScriptContext context, Scope scope) {
		Object mapOrArray = getMapOrArray().evaluate(context, scope);
		if (mapOrArray == null) {
			MagicScriptError.error(String.format("对象[%s]为空",getMapOrArray().getSpan().getText()),getMapOrArray().getSpan());
		}
		Object keyOrIndex = getKeyOrIndex().evaluate(context, scope);
		if (keyOrIndex == null) {
			return null;
		}

		if (mapOrArray instanceof Map) {
			return ((Map) mapOrArray).get(keyOrIndex);
		} else if (mapOrArray instanceof List) {
			if (!(keyOrIndex instanceof Number)) {
				MagicScriptError.error("List index must be an integer, but was " + keyOrIndex.getClass().getSimpleName(), getKeyOrIndex().getSpan());
			}
			int index = ((Number) keyOrIndex).intValue();
			return ((List) mapOrArray).get(index);
		} else {
			if (!(keyOrIndex instanceof Number)) {
				MagicScriptError.error("Array index must be an integer, but was " + keyOrIndex.getClass().getSimpleName(), getKeyOrIndex().getSpan());
			}
			int index = ((Number) keyOrIndex).intValue();
			if (mapOrArray instanceof int[]) {
				return ((int[]) mapOrArray)[index];
			} else if (mapOrArray instanceof float[]) {
				return ((float[]) mapOrArray)[index];
			} else if (mapOrArray instanceof double[]) {
				return ((double[]) mapOrArray)[index];
			} else if (mapOrArray instanceof boolean[]) {
				return ((boolean[]) mapOrArray)[index];
			} else if (mapOrArray instanceof char[]) {
				return ((char[]) mapOrArray)[index];
			} else if (mapOrArray instanceof short[]) {
				return ((short[]) mapOrArray)[index];
			} else if (mapOrArray instanceof long[]) {
				return ((long[]) mapOrArray)[index];
			} else if (mapOrArray instanceof byte[]) {
				return ((byte[]) mapOrArray)[index];
			} else if (mapOrArray instanceof String) {
				return Character.toString(((String) mapOrArray).charAt(index));
			} else {
				try {
					return ((Object[]) mapOrArray)[index];
				} catch (Exception e) {
					MagicScriptError.error("value must be Map or List or Array", getSpan(), e);
				}
				return null;
			}
		}
	}

	@Override
	public void setValue(MagicScriptContext context, Scope scope, Object value) {
		Object mapOrArray = getMapOrArray().evaluate(context, scope);
		if (mapOrArray != null) {
			Object keyOrIndex = getKeyOrIndex().evaluate(context, scope);
			if (keyOrIndex != null) {
				if (mapOrArray instanceof Map) {
					((Map) mapOrArray).put(keyOrIndex, value);
				} else if (mapOrArray instanceof List) {
					if (!(keyOrIndex instanceof Number)) {
						MagicScriptError.error("List index must be an integer, but was " + keyOrIndex.getClass().getSimpleName(), getKeyOrIndex().getSpan());
					}
					int index = ((Number) keyOrIndex).intValue();
					((List) mapOrArray).set(index, value);
				}
			}
		}
	}
}