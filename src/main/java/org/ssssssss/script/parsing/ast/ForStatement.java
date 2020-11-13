package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.VarNode;
import org.ssssssss.script.interpreter.AstInterpreter;
import org.ssssssss.script.parsing.Span;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ForStatement extends Node {
	private final VarNode indexOrKey;
	private final VarNode value;
    private final Expression mapOrArray;
    private final List<Node> body;

	public ForStatement(Span span, VarNode indexOrKey, VarNode value, Expression mapOrArray, List<Node> body) {
        super(span);
		this.indexOrKey = indexOrKey;
		this.value = value;
        this.mapOrArray = mapOrArray;
        this.body = body;
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
    public Object evaluate(MagicScriptContext context) {
        Object mapOrArray = getMapOrArray().evaluate(context);
        if (mapOrArray == null) MagicScriptError.error("Expected a map or array, got null.", getMapOrArray().getSpan());
        if (mapOrArray instanceof Map) {
            Map map = (Map) mapOrArray;
			if (indexOrKey != null) {
                for (Object entry : map.entrySet()) {
                    Map.Entry e = (Map.Entry) entry;
					indexOrKey.setValue(context, e.getKey());
					value.setValue(context, e.getValue());
                    Object breakOrContinueOrReturn = AstInterpreter.interpretNodeList(getBody(), context);
                    if (breakOrContinueOrReturn == Break.BREAK_SENTINEL) {
                        break;
                    }
					if (breakOrContinueOrReturn instanceof Return.ReturnValue) {
                        return breakOrContinueOrReturn;
                    }
                }
            } else {
                for (Object value : map.values()) {
					this.value.setValue(context, value);
                    Object breakOrContinueOrReturn = AstInterpreter.interpretNodeList(getBody(), context);
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
					indexOrKey.setValue(context, i++);
					value.setValue(context, iter.next());
                    Object breakOrContinueOrReturn = AstInterpreter.interpretNodeList(getBody(), context);
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
					value.setValue(context, iter.next());
                    Object breakOrContinueOrReturn = AstInterpreter.interpretNodeList(getBody(), context);
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
					value.setValue(context, iter.next());
                    Object breakOrContinueOrReturn = AstInterpreter.interpretNodeList(getBody(), context);
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
					indexOrKey.setValue(context, i);
					value.setValue(context, Array.get(mapOrArray, i));
                    Object breakOrContinueOrReturn = AstInterpreter.interpretNodeList(getBody(), context);
                    if (breakOrContinueOrReturn == Break.BREAK_SENTINEL) {
                        break;
                    }
					if (breakOrContinueOrReturn instanceof Return.ReturnValue) {
                        return breakOrContinueOrReturn;
                    }
                }
            } else {
                for (int i = 0; i < len; i++) {
					value.setValue(context, Array.get(mapOrArray, i));
                    Object breakOrContinueOrReturn = AstInterpreter.interpretNodeList(getBody(), context);
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