package org.ssssssss.script.parsing.ast.statement;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.interpreter.AstInterpreter;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.VarIndex;
import org.ssssssss.script.parsing.ast.Expression;
import org.ssssssss.script.parsing.ast.Node;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ForStatement extends Node {
	private final VarIndex indexOrKey;
	private final VarIndex value;
	private final Expression mapOrArray;
	private final List<Node> body;
	private final int varCount;

	public ForStatement(Span span, VarIndex indexOrKey, VarIndex value, int varCount, Expression mapOrArray, List<Node> body) {
		super(span);
		this.indexOrKey = indexOrKey;
		this.value = value;
		this.mapOrArray = mapOrArray;
		this.body = body;
		this.varCount = varCount;
	}

	/**
	 * Returns null if no index or key name was given
	 **/


	public Expression getMapOrArray() {
		return mapOrArray;
	}

	public List<Node> getBody() {
		return body;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object evaluate(MagicScriptContext context, Scope scope) {
		scope = scope.create(varCount);
		Object mapOrArray = getMapOrArray().evaluate(context, scope);
		if (mapOrArray == null) MagicScriptError.error("Expected a map or array, got null.", getMapOrArray().getSpan());
		if (mapOrArray instanceof Map) {
			Map map = (Map) mapOrArray;
			if (indexOrKey != null) {
				for (Object entry : map.entrySet()) {
					Map.Entry e = (Map.Entry) entry;
					scope.setValue(indexOrKey, e.getKey());
					scope.setValue(value, e.getValue());
					Object breakOrContinueOrReturn = AstInterpreter.interpretNodeList(getBody(), context, scope);
					if (breakOrContinueOrReturn == Break.BREAK_SENTINEL) {
						break;
					}
					if (breakOrContinueOrReturn instanceof Return.ReturnValue) {
						return breakOrContinueOrReturn;
					}
				}
			} else {
				for (Object value : map.values()) {
					scope.setValue(this.value, value);
					Object breakOrContinueOrReturn = AstInterpreter.interpretNodeList(getBody(), context, scope);
					if (breakOrContinueOrReturn == Break.BREAK_SENTINEL) {
						break;
					}
					if (breakOrContinueOrReturn instanceof Return.ReturnValue) {
						return breakOrContinueOrReturn;
					}
				}
			}
		} else if (mapOrArray instanceof Iterable) {
			if (indexOrKey != null) {
				Iterator iter = ((Iterable) mapOrArray).iterator();
				int i = 0;
				while (iter.hasNext()) {
					scope.setValue(indexOrKey, i++);
					scope.setValue(value, iter.next());
					Object breakOrContinueOrReturn = AstInterpreter.interpretNodeList(getBody(), context, scope);
					if (breakOrContinueOrReturn == Break.BREAK_SENTINEL) {
						break;
					}
					if (breakOrContinueOrReturn instanceof Return.ReturnValue) {
						return breakOrContinueOrReturn;
					}
				}
			} else {
				Iterator iter = ((Iterable) mapOrArray).iterator();
				while (iter.hasNext()) {
					scope.setValue(value, iter.next());
					Object breakOrContinueOrReturn = AstInterpreter.interpretNodeList(getBody(), context, scope);
					if (breakOrContinueOrReturn == Break.BREAK_SENTINEL) {
						break;
					}
					if (breakOrContinueOrReturn instanceof Return.ReturnValue) {
						return breakOrContinueOrReturn;
					}
				}
			}
		} else if (mapOrArray instanceof Iterator) {
			if (indexOrKey != null) {
				MagicScriptError.error("Can not do indexed/keyed for loop on an iterator.", getMapOrArray().getSpan());
			} else {
				Iterator iter = (Iterator) mapOrArray;
				while (iter.hasNext()) {
					scope.setValue(value, iter.next());
					Object breakOrContinueOrReturn = AstInterpreter.interpretNodeList(getBody(), context, scope);
					if (breakOrContinueOrReturn == Break.BREAK_SENTINEL) {
						break;
					}
					if (breakOrContinueOrReturn instanceof Return.ReturnValue) {
						return breakOrContinueOrReturn;
					}
				}
			}
		} else if (mapOrArray != null && mapOrArray.getClass().isArray()) {
			int len = Array.getLength(mapOrArray);
			if (indexOrKey != null) {
				for (int i = 0; i < len; i++) {
					scope.setValue(indexOrKey, i);
					scope.setValue(value, Array.get(mapOrArray, i));
					Object breakOrContinueOrReturn = AstInterpreter.interpretNodeList(getBody(), context, scope);
					if (breakOrContinueOrReturn == Break.BREAK_SENTINEL) {
						break;
					}
					if (breakOrContinueOrReturn instanceof Return.ReturnValue) {
						return breakOrContinueOrReturn;
					}
				}
			} else {
				for (int i = 0; i < len; i++) {
					scope.setValue(value, Array.get(mapOrArray, i));
					Object breakOrContinueOrReturn = AstInterpreter.interpretNodeList(getBody(), context, scope);
					if (breakOrContinueOrReturn == Break.BREAK_SENTINEL) {
						break;
					}
					if (breakOrContinueOrReturn instanceof Return.ReturnValue) {
						return breakOrContinueOrReturn;
					}
				}
			}
		} else {
			MagicScriptError.error("Expected a map, an array or an iterable, got " + mapOrArray, getMapOrArray().getSpan());
		}
		return null;
	}
}