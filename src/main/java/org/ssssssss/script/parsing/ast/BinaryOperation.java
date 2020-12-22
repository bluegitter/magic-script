package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.functions.ObjectConvertExtension;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.Token;
import org.ssssssss.script.parsing.ast.binary.*;

import java.math.BigDecimal;
import java.util.Date;

public abstract class BinaryOperation extends Expression {

	private Expression leftOperand;
	private Expression rightOperand;

	public BinaryOperation(Expression leftOperand, Span span, Expression rightOperand) {
		super(span);
		this.leftOperand = leftOperand;
		this.rightOperand = rightOperand;
	}

	public static Expression create(Expression left, Token operator, Expression right, int linqLevel) {
		Expression expression = null;
		Span span = operator.getSpan();
		switch (operator.getType()) {
			case Assignment:
				expression = linqLevel == 0 ? new AssigmentOperation(left, span, right) : new EqualOperation(left, span, right);
				break;
			case Plus:
				expression = new AddOperation(left, span, right);
				break;
			case Minus:
				expression = new SubtractionOperation(left, span, right);
				break;
			case Asterisk:
				expression = new MultiplicationOperation(left, span, right);
				break;
			case ForwardSlash:
				expression = new DivisionOperation(left, span, right);
				break;
			case Percentage:
				expression = new ModuloOperation(left, span, right);
				break;
			case PlusEqual:
				expression = new PlusEqualOperation(left, span, right);
				break;
			case MinusEqual:
				expression = new MinusEqualOperation(left, span, right);
				break;
			case AsteriskEqual:
				expression = new AsteriskEqualOperation(left, span, right);
				break;
			case ForwardSlashEqual:
				expression = new ForwardSlashEqualOperation(left, span, right);
				break;
			case PercentEqual:
				expression = new PercentEqualOperation(left, span, right);
				break;
			case Less:
				expression = new LessOperation(left, span, right);
				break;
			case LessEqual:
				expression = new LessEqualOperation(left, span, right);
				break;
			case Greater:
				expression = new GreaterOperation(left, span, right);
				break;
			case GreaterEqual:
				expression = new GreaterEqualOperation(left, span, right);
				break;
			case Equal:
				expression = new EqualOperation(left, span, right);
				break;
			case SqlNotEqual:
			case NotEqual:
				expression = new NotEqualOperation(left, span, right);
				break;
			case SqlAnd:
			case And:
				expression = new AndOperation(left, span, right);
				break;
			case SqlOr:
			case Or:
				expression = new OrOperation(left, span, right);
				break;
			default:
				MagicScriptError.error("[" + operator.getText() + "]操作符未实现", span);
		}
		return expression;
	}

	/**
	 * 比较两个值
	 * 1 左边大
	 * 0 相等
	 * -1 右边大
	 * -2 无法比较
	 */
	public static int compare(Object left, Object right) {
		if (left == null && right == null) {
			return -2;
		}
		if (left == null) {
			return -1;
		}
		if (right == null) {
			return 1;
		}
		if (left instanceof Number && right instanceof Number) {
			if (left instanceof BigDecimal || right instanceof BigDecimal) {
				return ObjectConvertExtension.asDecimal(left).compareTo(ObjectConvertExtension.asDecimal(right));
			} else if (left instanceof Double || right instanceof Double) {
				return Double.compare(((Number) left).doubleValue(), ((Number) right).doubleValue());
			} else if (left instanceof Float || right instanceof Float) {
				return Float.compare(((Number) left).floatValue(), ((Number) right).floatValue());
			} else if (left instanceof Long || right instanceof Long) {
				return Long.compare(((Number) left).longValue(), ((Number) right).longValue());
			} else if (left instanceof Integer || right instanceof Integer) {
				return Integer.compare(((Number) left).intValue(), ((Number) right).intValue());
			} else if (left instanceof Short || right instanceof Short) {
				return Short.compare(((Number) left).shortValue(), ((Number) right).shortValue());
			} else if (left instanceof Byte || right instanceof Byte) {
				return Byte.compare(((Number) left).byteValue(), ((Number) right).byteValue());
			}
		}
		if (left instanceof Date && right instanceof Date) {
			return ((Date) left).compareTo((Date) right);
		}
		if (left instanceof String && right instanceof String) {
			return ((String) left).compareTo((String) right);
		}
		return -2;

	}

	public void setLeftOperand(Expression leftOperand) {
		this.leftOperand = leftOperand;
	}

	public void setRightOperand(Expression rightOperand) {
		this.rightOperand = rightOperand;
	}

	public Expression getLeftOperand() {
		return leftOperand;
	}

	public Expression getRightOperand() {
		return rightOperand;
	}

}
