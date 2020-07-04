package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.interpreter.AbstractReflection;
import org.ssssssss.script.parsing.Span;

import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class FunctionCall extends Expression {
    private final Expression function;
    private final List<Expression> arguments;
    private final ThreadLocal<Object[]> cachedArguments;
    private Object cachedFunction;
    private static final BiFunction<Integer, Integer, Iterator<Integer>> range =  (from, to) -> new Iterator<Integer>() {
        int idx = from;

        public boolean hasNext() {
            return idx <= to;
        }

        public Integer next() {
            return idx++;
        }
    };

    public FunctionCall(Span span, Expression function, List<Expression> arguments) {
        super(span);
        this.function = function;
        this.arguments = arguments;
        this.cachedArguments = new ThreadLocal<>();
    }

    public Expression getFunction() {
        return function;
    }

    public List<Expression> getArguments() {
        return arguments;
    }

    public Object getCachedFunction() {
        return cachedFunction;
    }

    public void setCachedFunction(Object cachedFunction) {
        this.cachedFunction = cachedFunction;
    }

    public Object[] getCachedArguments() {
        Object[] args = cachedArguments.get();
        if (args == null) {
            args = new Object[arguments.size()];
            cachedArguments.set(args);
        }
        return args;
    }

    /**
     * Must be invoked when this node is done evaluating so we don't leak memory
     **/
    public void clearCachedArguments() {
        Object[] args = getCachedArguments();
        for (int i = 0; i < args.length; i++) {
            args[i] = null;
        }
    }

    @Override
    public Object evaluate(MagicScriptContext context) {
        try {
            Object[] argumentValues = getCachedArguments();
            List<Expression> arguments = getArguments();
            for (int i = 0, n = argumentValues.length; i < n; i++) {
                Expression expr = arguments.get(i);
                argumentValues[i] = expr.evaluate(context);
            }

            // This is a special case to handle magicScript level macros. If a call to a macro is
            // made, evaluating the function expression will result in an exception, as the
            // function name can't be found in the context. Instead we need to manually check
            // if the function expression is a VariableAccess and if so, if it can be found
            // in the context.
            Object function = null;
            if("range".equals(getFunction().getSpan().getText())){
                function = range;
            }else if (getFunction() instanceof VariableAccess) {
                VariableAccess varAccess = (VariableAccess) getFunction();
                function = context.get(varAccess.getVariableName().getText());
            }  else {
                function = getFunction().evaluate(context);
            }
            if (function instanceof Function) {
                return ((Function<Object[],Object>) function).apply(argumentValues);
            }
            if (function != null) {
                Object method = getCachedFunction();
                if (method != null) {
                    try {
                        return AbstractReflection.getInstance().callMethod(function, method, argumentValues);
                    } catch (Throwable t) {
                        // fall through
                    }
                }
                method = AbstractReflection.getInstance().getMethod(function, null, argumentValues);
                if (method == null) {
                    MagicScriptError.error("Couldn't find function.", getSpan());
                }
                setCachedFunction(method);
                try {
                    return AbstractReflection.getInstance().callMethod(function, method, argumentValues);
                } catch (Throwable t) {
                    MagicScriptError.error(t.getMessage(), getSpan(), t);
                    return null; // never reached
                }
            } else {
                MagicScriptError.error("Couldn't find function " + getFunction(), getSpan());
                return null; // never reached
            }
        } finally {
            clearCachedArguments();
        }
    }
}