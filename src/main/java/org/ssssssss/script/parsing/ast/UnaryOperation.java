package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Token;
import org.ssssssss.script.parsing.TokenType;

import java.math.BigDecimal;

/**
 * 一元操作符
 */
public class UnaryOperation extends Expression {

    private final UnaryOperator operator;
    private final Expression operand;
    private final boolean atAfter;

    public UnaryOperation(Token operator, Expression operand) {
        this(operator, operand, false);
    }

    public UnaryOperation(Token operator, Expression operand, boolean atAfter) {
        super(operator.getSpan());
        this.operator = UnaryOperator.getOperator(operator);
        this.operand = operand;
        this.atAfter = atAfter;
    }

    public UnaryOperator getOperator() {
        return operator;
    }

    private Expression getOperand() {
        return operand;
    }

    @Override
	public Object evaluate(MagicScriptContext context, Scope scope) {
        Object value = getOperand().evaluate(context, scope);
        switch (getOperator()) {
            case Not:
                if (!(value instanceof Boolean)) {
                    MagicScriptError.error("一元操作符[" + getOperator().name() + "]的值必须是boolean类型，获得的值为：" + operand, getSpan());
                }
                return !(Boolean) value;
            case PlusPlus:
            case MinusMinus:
                if (operand instanceof VariableSetter && value instanceof Number) {
                    Object result = addValue(value, getOperator() == UnaryOperator.PlusPlus ? 1 : -1);
                    ((VariableSetter) operand).setValue(context, scope, result);
                    return atAfter ? value : result;
                } else {
                    MagicScriptError.error("一元操作符[" + getOperator().name() + "] 操作的值必须可写：" + operand, getSpan());
                    return null; // never reached
                }
            case Negate:
                if (value instanceof Integer) {
                    return -(Integer) value;
                } else if (value instanceof Float) {
                    return -(Float) value;
                } else if (value instanceof Double) {
                    return -(Double) value;
                } else if (value instanceof Byte) {
                    return -(Byte) value;
                } else if (value instanceof Short) {
                    return -(Short) value;
                } else if (value instanceof Long) {
                    return -(Long) value;
                } else if (value instanceof BigDecimal) {
                    return ((BigDecimal) value).negate();
                } else {
                    MagicScriptError.error("一元操作符[" + getOperator().name() + "]的值必须是数值类型，获得的值为：" + operand, getSpan());
                }

        }
        return operand;
    }

    private Object addValue(Object target, int value) {
        if (target instanceof Integer) {
            return ((Integer) target) + value;
        } else if (target instanceof Long) {
            return ((Long) target) + value;
        } else if (target instanceof Double) {
            return ((Double) target) + value;
        } else if (target instanceof BigDecimal) {
            return ((BigDecimal) target).add(new BigDecimal(value));
        } else if (target instanceof Float) {
            return ((Float) target) + value;
        } else if (target instanceof Byte) {
            return ((Byte) target) + value;
        } else if (target instanceof Short) {
            return ((Short) target) + value;
        }
        return null;

    }

    public enum UnaryOperator {
        Not, Negate, Positive, PlusPlus, MinusMinus;

        public static UnaryOperator getOperator(Token op) {
            if (op.getType() == TokenType.Not) {
                return UnaryOperator.Not;
            }
            if (op.getType() == TokenType.Plus) {
                return UnaryOperator.Positive;
            }
            if (op.getType() == TokenType.Minus) {
                return UnaryOperator.Negate;
            }
            if (op.getType() == TokenType.PlusPlus) {
                return UnaryOperator.PlusPlus;
            }
            if (op.getType() == TokenType.MinusMinus) {
                return UnaryOperator.MinusMinus;
            }
            MagicScriptError.error("不支持的一元操作符：" + op, op.getSpan());
            return null; // not reached
        }
    }
}