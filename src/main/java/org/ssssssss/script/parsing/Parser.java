package org.ssssssss.script.parsing;


import org.ssssssss.script.MagicScript;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.parsing.ast.*;
import org.ssssssss.script.parsing.ast.linq.*;
import org.ssssssss.script.parsing.ast.literal.*;
import org.ssssssss.script.parsing.ast.statement.*;

import javax.xml.transform.Source;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;


/**
 * Parses a {@link Source} into a {@link MagicScript}. The implementation is a simple recursive descent parser with a lookahead of
 * 1.
 **/
public class Parser {

	private static final TokenType[][] binaryOperatorPrecedence = new TokenType[][]{
			new TokenType[]{TokenType.Assignment},
			new TokenType[]{TokenType.PlusEqual, TokenType.MinusEqual, TokenType.AsteriskEqual, TokenType.ForwardSlashEqual, TokenType.PercentEqual},
			new TokenType[]{TokenType.Or, TokenType.And, TokenType.SqlOr, TokenType.SqlAnd, TokenType.Xor},
			new TokenType[]{TokenType.Equal, TokenType.NotEqual, TokenType.SqlNotEqual},
			new TokenType[]{TokenType.Less, TokenType.LessEqual, TokenType.Greater, TokenType.GreaterEqual},
			new TokenType[]{TokenType.Plus, TokenType.Minus},
			new TokenType[]{TokenType.ForwardSlash, TokenType.Asterisk, TokenType.Percentage}
	};

	private static final TokenType[][] linqBinaryOperatorPrecedence = new TokenType[][]{
			new TokenType[]{TokenType.PlusEqual, TokenType.MinusEqual, TokenType.AsteriskEqual, TokenType.ForwardSlashEqual, TokenType.PercentEqual},
			new TokenType[]{TokenType.Or, TokenType.And, TokenType.SqlOr, TokenType.SqlAnd, TokenType.Xor},
			new TokenType[]{TokenType.Assignment, TokenType.Equal, TokenType.NotEqual, TokenType.SqlNotEqual},
			new TokenType[]{TokenType.Less, TokenType.LessEqual, TokenType.Greater, TokenType.GreaterEqual},
			new TokenType[]{TokenType.Plus, TokenType.Minus},
			new TokenType[]{TokenType.ForwardSlash, TokenType.Asterisk, TokenType.Percentage}
	};

	private static final TokenType[] unaryOperators = new TokenType[]{TokenType.Not, TokenType.PlusPlus, TokenType.MinusMinus, TokenType.Plus, TokenType.Minus};

	private static final List<String> keywords = Arrays.asList("import", "as", "var", "return", "break", "continue", "if", "for", "in", "new", "true", "false", "null", "else", "try", "catch", "finally", "async", "while", "exit", "and", "or");

	private static final List<String> linqKeywords = Arrays.asList("from", "join", "left", "group", "by", "as", "having", "and", "or", "in", "where");

	private Stack<List<String>> varNames = new Stack<>();

	private List<String> current = new ArrayList<>();

	private int linqLevel = 0;

	public int getTopVarCount() {
		return current.size();
	}

	/**
	 * Parses a {@link Source} into a {@link MagicScript}.
	 **/
	public List<Node> parse(String source) {
		List<Node> nodes = new ArrayList<Node>();
		TokenStream stream = Tokenizer.tokenize(source);
		while (stream.hasMore()) {
			Node node = parseStatement(stream);
			if (node != null) {
				validateNode(node);
				nodes.add(node);
			}
		}
		return nodes;
	}

	private void validateNode(Node node) {
		if (node instanceof Literal || node instanceof VariableAccess || node instanceof MapOrArrayAccess) {
			MagicScriptError.error("literal cannot be used alone", node.getSpan());
		}
	}

	private Node parseStatement(TokenStream tokens) {
		return parseStatement(tokens, false);
	}

	private Node parseStatement(TokenStream tokens, boolean expectRightCurly) {
		Node result = null;
		if (tokens.match("import", false)) {
			result = parseImport(tokens);
		} else if (tokens.match("var", false)) {
			result = parseVarDefine(tokens);
		} else if (tokens.match("if", false)) {
			result = parseIfStatement(tokens);
		} else if (tokens.match("return", false)) {
			result = parseReturn(tokens);
		} else if (tokens.match("for", false)) {
			result = parseForStatement(tokens);
		} else if (tokens.match("while", false)) {
			result = parseWhileStatement(tokens);
		} else if (tokens.match("continue", false)) {
			result = new Continue(tokens.consume().getSpan());
		} else if (tokens.match("async", false)) {
			result = parseAsync(tokens);
		} else if (tokens.match("try", false)) {
			result = parseTryStatement(tokens);
		} else if (tokens.match("break", false)) {
			result = new Break(tokens.consume().getSpan());
		} else if (tokens.match("exit", false)) {
			result = parseExit(tokens);
		} else {
			result = parseExpression(tokens, expectRightCurly);
		}
		// consume semi-colons as statement delimiters
		while (tokens.match(";", true)) {
			;
		}
		return result;
	}

	private VarIndex add(String name) {
		int index = current.lastIndexOf(name);
		if (index > -1) {
			return new VarIndex(name, index, true);
		}
		index = current.size();
		current.add(name);
		return new VarIndex(name, index, true);
	}

	private VarIndex forceAdd(String name) {
		int index = current.size();
		current.add(name);
		return new VarIndex(name, index, false);
	}

	private void push() {
		varNames.push(current);
		current = new ArrayList<>();
	}

	private int pop() {
		int count = current.size();
		current = varNames.pop();
		return count;
	}

	private Node parseExit(TokenStream stream) {
		Span opening = stream.expect("exit").getSpan();
		List<Expression> expressionList = new ArrayList<>();
		do {
			expressionList.add(parseExpression(stream));
		} while (stream.match(TokenType.Comma, true));
		return new Exit(new Span(opening, stream.getPrev().getSpan()), expressionList);
	}

	private Expression parseAsync(TokenStream stream) {
		Span opening = stream.expect("async").getSpan();
		Expression expression = parseExpression(stream);
		if (expression instanceof MethodCall || expression instanceof FunctionCall || expression instanceof LambdaFunction) {
			return new AsyncCall(new Span(opening, stream.getPrev().getSpan()), expression);
		}
		MagicScriptError.error("Expected MethodCall or FunctionCall or LambdaFunction", stream.getPrev().getSpan());
		return null;
	}

	private Import parseImport(TokenStream stream) {
		Span opening = stream.expect("import").getSpan();
		if (stream.hasMore()) {
			Token expected = stream.consume();
			String packageName = null;
			boolean isStringLiteral = expected.getType() == TokenType.StringLiteral;
			if (isStringLiteral) {
				packageName = new StringLiteral(expected.getSpan()).getValue();
			} else if (expected.getType() == TokenType.Identifier) {
				packageName = expected.getSpan().getText();
			} else {
				MagicScriptError.error("Expected identifier or string, but got stream is " + expected.getType().getError(), stream.getPrev().getSpan());
			}
			String varName = packageName;
			if (isStringLiteral || stream.match("as", false)) {
				stream.expect("as");
				expected = stream.expect(TokenType.Identifier);
				checkKeyword(expected.getSpan());
				varName = expected.getSpan().getText();
			}
			return new Import(new Span(opening, expected.getSpan()), packageName, add(varName), !isStringLiteral);
		}
		MagicScriptError.error("Expected identifier or string, but got stream is EOF", stream.getPrev().getSpan());
		return null;
	}

	private TryStatement parseTryStatement(TokenStream stream) {
		Token opening = stream.expect("try");
		push();
		List<Node> tryBlocks = parseFunctionBody(stream);
		int tryVarCount = pop();
		List<Node> catchBlocks = new ArrayList<>();
		List<Node> finallyBlocks = new ArrayList<>();
		int catchVarCount = 0;
		VarIndex exceptionVarNode = null;
		if (stream.match("catch", true)) {
			push();
			if (stream.match("(", true)) {
				exceptionVarNode = add(stream.expect(TokenType.Identifier).getText());
				stream.expect(")");
			}
			catchBlocks.addAll(parseFunctionBody(stream));
			catchVarCount = pop();
		}
		int finallyVarCount = 0;
		if (stream.match("finally", true)) {
			push();
			finallyBlocks.addAll(parseFunctionBody(stream));
			finallyVarCount = pop();
		}
		return new TryStatement(new Span(opening.getSpan(), stream.getPrev().getSpan()), exceptionVarNode, tryBlocks, catchBlocks, finallyBlocks, tryVarCount, catchVarCount, finallyVarCount);
	}

	private List<Node> parseFunctionBody(TokenStream stream) {
		stream.expect("{");
		List<Node> blocks = new ArrayList<Node>();
		while (stream.hasMore() && !stream.match("}", false)) {
			Node node = parseStatement(stream, true);
			if (node != null) {
				validateNode(node);
				blocks.add(node);
			}
		}
		expectCloseing(stream);
		return blocks;
	}

	private Expression parseNewExpression(Span opening, TokenStream stream) {
		Token identifier = stream.expect(TokenType.Identifier);
		List<Expression> arguments = new ArrayList<>();
		arguments.addAll(parseArguments(stream));
		Span closing = stream.expect(")").getSpan();
		NewStatement newStatement = new NewStatement(new Span(opening, closing), add(identifier.getText()), arguments);
		if (stream.match(false, TokenType.Period, TokenType.QuestionPeriod)) {
			return parseAccessOrCall(stream, newStatement);
		}
		return newStatement;
	}

	private VariableDefine parseVarDefine(TokenStream stream) {
		Span opening = stream.expect("var").getSpan();
		TokenType expected = null;
		if (stream.hasMore()) {
			expected = TokenType.Identifier;
			Token token = stream.expect(expected);
			checkKeyword(token.getSpan());
			String variableName = token.getSpan().getText();
			expected = TokenType.Assignment;
			if (stream.hasMore()) {
				stream.expect(expected);
				return new VariableDefine(new Span(opening, stream.getPrev().getSpan()), forceAdd(variableName), parseExpression(stream));
			}
		}
		MagicScriptError.error("Expected " + expected.getError() + ", but got stream is EOF", stream.getPrev().getSpan());
		return null;
	}

	private void checkKeyword(Span span) {
		if (keywords.contains(span.getText())) {
			MagicScriptError.error("变量名不能定义为关键字", span);
		}
	}

	private WhileStatement parseWhileStatement(TokenStream stream) {
		Span openingWhile = stream.expect("while").getSpan();
		Expression condition = parseExpression(stream);
		push();
		List<Node> trueBlock = parseFunctionBody(stream);
		Span closingEnd = stream.getPrev().getSpan();
		return new WhileStatement(new Span(openingWhile, closingEnd), condition, trueBlock, pop());
	}

	private ForStatement parseForStatement(TokenStream stream) {
		Span openingFor = stream.expect("for").getSpan();
		stream.expect("(");
		push();
		Span index = null;
		Span value = stream.expect(TokenType.Identifier).getSpan();
		checkKeyword(value);
		if (stream.match(TokenType.Comma, true)) {
			index = value;
			value = stream.expect(TokenType.Identifier).getSpan();
			checkKeyword(value);
		}
		VarIndex indexOrKeyNode = null;
		if (index != null) {
			indexOrKeyNode = forceAdd(index.getText());
		}
		VarIndex valueNode = forceAdd(value.getText());
		stream.expect("in");
		Expression mapOrArray = parseExpression(stream);
		stream.expect(")");
		List<Node> body = parseFunctionBody(stream);
		return new ForStatement(new Span(openingFor, stream.getPrev().getSpan()), indexOrKeyNode, valueNode, pop(), mapOrArray, body);
	}

	private static Span expectCloseing(TokenStream stream) {
		if (!stream.hasMore()) {
			MagicScriptError.error("Did not find closing }.", stream.prev().getSpan());
		}
		return stream.expect("}").getSpan();
	}

	private Node parseIfStatement(TokenStream stream) {
		Span openingIf = stream.expect("if").getSpan();
		Expression condition = parseExpression(stream);
		int trueVarCount = 0;
		push();
		List<Node> trueBlock = parseFunctionBody(stream);
		trueVarCount = pop();
		List<IfStatement> elseIfs = new ArrayList<>();
		List<Node> falseBlock = new ArrayList<>();
		int falseVarCount = 0;
		while (stream.hasMore() && stream.match("else", true)) {
			if (stream.hasMore() && stream.match("if", false)) {
				Span elseIfOpening = stream.expect("if").getSpan();
				Expression elseIfCondition = parseExpression(stream);
				push();
				List<Node> elseIfBlock = parseFunctionBody(stream);
				Span elseIfSpan = new Span(elseIfOpening, elseIfBlock.size() > 0 ? elseIfBlock.get(elseIfBlock.size() - 1).getSpan() : elseIfOpening);
				elseIfs.add(new IfStatement(elseIfSpan, elseIfCondition, elseIfBlock, new ArrayList<>(), new ArrayList<>(), pop(), 0));
			} else {
				push();
				falseBlock.addAll(parseFunctionBody(stream));
				falseVarCount = pop();
				break;
			}
		}
		Span closingEnd = stream.getPrev().getSpan();

		return new IfStatement(new Span(openingIf, closingEnd), condition, trueBlock, elseIfs, falseBlock, trueVarCount, falseVarCount);
	}

	private Node parseReturn(TokenStream tokens) {
		Span returnSpan = tokens.expect("return").getSpan();
		if (tokens.match(";", false)) return new Return(returnSpan, null);
		Expression returnValue = parseExpression(tokens);
		return new Return(new Span(returnSpan, returnValue.getSpan()), returnValue);
	}

	public Expression parseExpression(TokenStream stream) {
		return parseTernaryOperator(stream);
	}

	public Expression parseExpression(TokenStream stream, boolean expectRightCurly) {
		return parseTernaryOperator(stream, expectRightCurly);
	}

	private Expression parseTernaryOperator(TokenStream stream, boolean expectRightCurly) {
		Expression condition = parseBinaryOperator(stream, 0, expectRightCurly);
		if (stream.match(TokenType.Questionmark, true)) {
			Expression trueExpression = parseTernaryOperator(stream, expectRightCurly);
			stream.expect(TokenType.Colon);
			Expression falseExpression = parseTernaryOperator(stream, expectRightCurly);
//			if (condition instanceof BinaryOperation) {
//				BinaryOperation operation = (BinaryOperation) condition;
//				operation.setRightOperand(new TernaryOperation(operation.getRightOperand(), trueExpression, falseExpression));
//				return operation;
//			}
			return new TernaryOperation(condition, trueExpression, falseExpression);
		} else {
			return condition;
		}
	}

	private Expression parseTernaryOperator(TokenStream stream) {
		return parseTernaryOperator(stream, false);
	}

	private Expression parseBinaryOperator(TokenStream stream, TokenType[][] precedence, int level, boolean expectRightCurly) {
		int nextLevel = level + 1;
		Expression left = nextLevel == precedence.length ? parseUnaryOperator(stream, expectRightCurly) : parseBinaryOperator(stream, nextLevel, expectRightCurly);

		TokenType[] operators = precedence[level];
		while (stream.hasMore() && stream.match(false, operators)) {
			Token operator = stream.consume();
			if (operator.getType().isInLinq() && linqLevel == 0) {
				MagicScriptError.error(operator.getText() + " 只能在Linq中使用", stream);
			}
			Expression right = nextLevel == precedence.length ? parseUnaryOperator(stream, expectRightCurly) : parseBinaryOperator(stream, nextLevel, expectRightCurly);
			left = BinaryOperation.create(left, operator, right, linqLevel);
		}

		return left;
	}

	private Expression parseBinaryOperator(TokenStream stream, int level, boolean expectRightCurly) {
		if (linqLevel > 0) {
			return parseBinaryOperator(stream, linqBinaryOperatorPrecedence, level, expectRightCurly);
		}
		return parseBinaryOperator(stream, binaryOperatorPrecedence, level, expectRightCurly);
	}


	private Expression parseUnaryOperator(TokenStream stream, boolean expectRightCurly) {
		if (stream.match(false, unaryOperators)) {
			return new UnaryOperation(stream.consume(), parseUnaryOperator(stream, expectRightCurly));
		} else {
			if (stream.match(TokenType.LeftParantheses, false)) {    //(
				Span openSpan = stream.expect(TokenType.LeftParantheses).getSpan();
				int index = stream.makeIndex();
				List<VarIndex> parameters = new ArrayList<>();
				push();
				try {
					while (stream.match(TokenType.Identifier, false)) {
						Token identifier = stream.expect(TokenType.Identifier);
						checkKeyword(identifier.getSpan());
						parameters.add(forceAdd(identifier.getSpan().getText()));
						if (stream.match(TokenType.Comma, true)) { //,
							continue;
						}
						if (stream.match(TokenType.RightParantheses, true)) {  //)
							if (stream.match(TokenType.Lambda, true)) {   // =>
								return parseLambdaBody(stream, openSpan, parameters);
							}
							break;
						}
					}
					if (stream.match(TokenType.RightParantheses, true) && stream.match(TokenType.Lambda, true)) {
						return parseLambdaBody(stream, openSpan, parameters);
					}
				} finally {
					pop();
				}
				stream.resetIndex(index);
				Expression expression = parseExpression(stream);
				stream.expect(TokenType.RightParantheses);
				if (stream.match(false, TokenType.Period, TokenType.QuestionPeriod)) {
					expression = parseAccessOrCall(stream, expression);
				}
				return expression;
			} else {
				Expression expression = parseAccessOrCallOrLiteral(stream, expectRightCurly);
				if (expression instanceof VariableSetter) {
					if (stream.match(false, TokenType.PlusPlus, TokenType.MinusMinus)) {
						return new UnaryOperation(stream.consume(), expression, true);
					}
				}
				return expression;
			}
		}
	}

	private Expression parseLambdaBody(TokenStream stream, Span openSpan, List<VarIndex> parameters) {
		int index = stream.makeIndex();
		List<Node> childNodes = new ArrayList<>();
		try {
			Expression expression = parseExpression(stream);
			childNodes.add(new Return(new Span("return", 0, 6), expression));
			return new LambdaFunction(new Span(openSpan, expression.getSpan()), parameters, current.size(), childNodes);
		} catch (Exception e) {
			stream.resetIndex(index);
			if (stream.match(TokenType.LeftCurly, true)) {
				while (stream.hasMore() && !stream.match(false, "}")) {
					Node node = parseStatement(stream, true);
					validateNode(node);
					childNodes.add(node);
				}
				Span closeSpan = expectCloseing(stream);
				return new LambdaFunction(new Span(openSpan, closeSpan), parameters, current.size(), childNodes);
			} else {
				Node node = parseStatement(stream);
				childNodes.add(new Return(new Span("return", 0, 6), node));
				return new LambdaFunction(new Span(openSpan, node.getSpan()), parameters, current.size(), childNodes);
			}
		}
	}

	private Expression parseSpreadAccess(TokenStream stream, Token spread) {
		Expression target = parseExpression(stream);
		return new Spread(new Span(spread.getSpan(), target.getSpan()), target);
	}

	private Expression parseSpreadAccess(TokenStream stream) {
		Token spread = stream.expect(TokenType.Spread);
		return parseSpreadAccess(stream, spread);
	}

	private Expression parseSelect(TokenStream stream) {
		Span opeing = stream.expect("select").getSpan();
		linqLevel++;
		List<LinqField> fields = parseLinqFields(stream);
		stream.expect("from");
		LinqField from = parseLinqField(stream);
		List<LinqJoin> joins = parseLinqJoins(stream);
		Expression where = null;
		if (stream.match("where", true)) {
			where = parseExpression(stream);
		}
		List<LinqField> groups = parseGroup(stream);
		List<LinqOrder> orders = parseLinqOrders(stream);
		linqLevel--;
		Span close = stream.getPrev().getSpan();
		return new LinqSelect(new Span(opeing, close), fields, from, joins, where, groups, orders);
	}

	private List<LinqField> parseGroup(TokenStream stream) {
		List<LinqField> groups = new ArrayList<>();
		if (stream.match("group", true)) {
			stream.expect("by");
			do {
				Expression expression = parseExpression(stream);
				groups.add(new LinqField(expression.getSpan(), expression, null));
			} while (stream.match(TokenType.Comma, true));
		}
		return groups;
	}

	private List<LinqOrder> parseLinqOrders(TokenStream stream) {
		List<LinqOrder> orders = new ArrayList<>();
		if (stream.match("order", true)) {
			stream.expect("by");
			do {
				Expression expression = parseExpression(stream);
				int order = 1;
				if (stream.match(false, "desc", "asc")) {
					if ("desc".equals(stream.consume().getText())) {
						order = -1;
					}
				}
				orders.add(new LinqOrder(new Span(expression.getSpan(), stream.getPrev().getSpan()), expression, null, order));
			} while (stream.match(TokenType.Comma, true));
		}
		return orders;
	}

	private List<LinqField> parseLinqFields(TokenStream stream) {
		List<LinqField> fields = new ArrayList<>();
		do {
			Expression expression = parseExpression(stream);

			if (stream.match(TokenType.Identifier, false) && !stream.match(linqKeywords, false)) {
				if (expression instanceof WholeLiteral) {
					MagicScriptError.error("* 后边不能跟别名", stream);
				} else if (expression instanceof MemberAccess && ((MemberAccess) expression).isWhole()) {
					MagicScriptError.error(expression.getSpan().getText() + " 后边不能跟别名", stream);
				}
				Span alias = stream.consume().getSpan();
				fields.add(new LinqField(new Span(expression.getSpan(), alias), expression, add(alias.getText())));
			} else {
				fields.add(new LinqField(expression.getSpan(), expression, null));
			}
		} while (stream.match(TokenType.Comma, true));    //,
		if (fields.isEmpty()) {
			MagicScriptError.error("至少要查询一个字段", stream);
		}
		return fields;
	}

	private List<LinqJoin> parseLinqJoins(TokenStream stream) {
		List<LinqJoin> joins = new ArrayList<>();
		do {
			boolean isLeft = stream.match("left", false);
			Span opeing = isLeft ? stream.consume().getSpan() : null;
			if (stream.match("join", true)) {
				opeing = isLeft ? opeing : stream.getPrev().getSpan();
				LinqField target = parseLinqField(stream);
				stream.expect("on");
				Expression condition = parseExpression(stream);
				joins.add(new LinqJoin(new Span(opeing, stream.getPrev().getSpan()), isLeft, target, condition));
			}
		} while (stream.match(false, "left", "join"));
		return joins;
	}

	private LinqField parseLinqField(TokenStream stream) {
		Expression expression = parseExpression(stream);
		if (stream.match(TokenType.Identifier, false) && !stream.match(linqKeywords, false)) {
			Span alias = stream.expect(TokenType.Identifier).getSpan();
			return new LinqField(new Span(expression.getSpan(), alias), expression, add(alias.getText()));
		}
		return new LinqField(expression.getSpan(), expression, null);
	}

	private Expression parseAccessOrCallOrLiteral(TokenStream stream, boolean expectRightCurly) {
		if (expectRightCurly && stream.match("}", false)) {
			return null;
		} else if (stream.match("async", false)) {
			return parseAsync(stream);
		} else if (stream.match("select", false)) {
			return parseSelect(stream);
		} else if (stream.match(TokenType.Spread, false)) {
			return parseSpreadAccess(stream);
		} else if (stream.match(TokenType.Identifier, false)) {
			return parseAccessOrCall(stream, TokenType.Identifier);
		} else if (stream.match(TokenType.LeftCurly, false)) {
			return parseMapLiteral(stream);
		} else if (stream.match(TokenType.LeftBracket, false)) {
			return parseListLiteral(stream);
		} else if (stream.match(TokenType.StringLiteral, false)) {
			if (stream.hasNext()) {
				TokenType nextType = stream.next().getType();
				if (nextType == TokenType.Period || nextType == TokenType.QuestionPeriod) {
					stream.prev();
					return parseAccessOrCall(stream, TokenType.StringLiteral);
				}
				stream.prev();
			}

			return new StringLiteral(stream.expect(TokenType.StringLiteral).getSpan());
		} else if (stream.match(TokenType.BooleanLiteral, false)) {
			return new BooleanLiteral(stream.expect(TokenType.BooleanLiteral).getSpan());
		} else if (stream.match(TokenType.DoubleLiteral, false)) {
			return new DoubleLiteral(stream.expect(TokenType.DoubleLiteral).getSpan());
		} else if (stream.match(TokenType.FloatLiteral, false)) {
			return new FloatLiteral(stream.expect(TokenType.FloatLiteral).getSpan());
		} else if (stream.match(TokenType.ByteLiteral, false)) {
			return new ByteLiteral(stream.expect(TokenType.ByteLiteral).getSpan());
		} else if (stream.match(TokenType.ShortLiteral, false)) {
			return new ShortLiteral(stream.expect(TokenType.ShortLiteral).getSpan());
		} else if (stream.match(TokenType.IntegerLiteral, false)) {
			return new IntegerLiteral(stream.expect(TokenType.IntegerLiteral).getSpan());
		} else if (stream.match(TokenType.LongLiteral, false)) {
			return new LongLiteral(stream.expect(TokenType.LongLiteral).getSpan());
		} else if (stream.match(TokenType.DecimalLiteral, false)) {
			return new BigDecimalLiteral(stream.expect(TokenType.DecimalLiteral).getSpan());
		} else if (stream.match(TokenType.RegexpLiteral, false)) {
			Token token = stream.expect(TokenType.RegexpLiteral);
			Expression target = new RegexpLiteral(token.getSpan(), token);
			return parseAccessOrCall(stream, target);
		} else if (stream.match(TokenType.NullLiteral, false)) {
			return new NullLiteral(stream.expect(TokenType.NullLiteral).getSpan());
		} else if (linqLevel > 0 && stream.match(TokenType.Asterisk, false)) {
			return new WholeLiteral(stream.expect(TokenType.Asterisk).getSpan());
		} else {
			MagicScriptError.error("Expected a variable, field, map, array, function or method call, or literal.", stream);
			return null; // not reached
		}
	}


	private Expression parseMapLiteral(TokenStream stream) {
		Span openCurly = stream.expect(TokenType.LeftCurly).getSpan();

		List<Token> keys = new ArrayList<>();
		List<Expression> values = new ArrayList<>();
		while (stream.hasMore() && !stream.match("}", false)) {
			Token key;
			if (stream.hasPrev()) {
				Token prev = stream.getPrev();
				if (stream.match(TokenType.Spread, false) && (prev.getType() == TokenType.LeftCurly || prev.getType() == TokenType.Comma)) {
					Token spread = stream.expect(TokenType.Spread);
					keys.add(spread);
					values.add(parseSpreadAccess(stream, spread));
					if (stream.match(false, TokenType.Comma, TokenType.RightCurly)) {
						stream.match(TokenType.Comma, true);
					}
					continue;
				}
			}
			if (stream.match(TokenType.StringLiteral, false)) {
				key = stream.expect(TokenType.StringLiteral);
			} else {
				key = stream.expect(TokenType.Identifier);
			}
			keys.add(key);
			if (stream.match(false, TokenType.Comma, TokenType.RightCurly)) {
				stream.match(TokenType.Comma, true);
				if (key.getType() == TokenType.Identifier) {
					values.add(new VariableAccess(key.getSpan(), add(key.getText())));
				} else {
					values.add(new StringLiteral(key.getSpan()));
				}
			} else {
				stream.expect(":");
				values.add(parseExpression(stream));
				if (!stream.match("}", false)) {
					stream.expect(TokenType.Comma);
				}
			}
		}
		Span closeCurly = stream.expect("}").getSpan();
		return new MapLiteral(new Span(openCurly, closeCurly), keys, values);
	}

	private Expression parseListLiteral(TokenStream stream) {
		Span openBracket = stream.expect(TokenType.LeftBracket).getSpan();

		List<Expression> values = new ArrayList<>();
		while (stream.hasMore() && !stream.match(TokenType.RightBracket, false)) {
			values.add(parseExpression(stream));
			if (!stream.match(TokenType.RightBracket, false)) {
				stream.expect(TokenType.Comma);
			}
		}

		Span closeBracket = stream.expect(TokenType.RightBracket).getSpan();
		Expression target = new ListLiteral(new Span(openBracket, closeBracket), values);
		if (stream.match(false, TokenType.Period, TokenType.QuestionPeriod)) {
			target = parseAccessOrCall(stream, target);
		}
		return target;
	}


	private Expression parseAccessOrCall(TokenStream stream, TokenType tokenType) {
		//Span identifier = stream.expect(TokenType.Identifier);
		//Expression result = new VariableAccess(identifier);
		Span identifier = stream.expect(tokenType).getSpan();
		if (tokenType == TokenType.Identifier && "new".equals(identifier.getText())) {
			return parseNewExpression(identifier, stream);
		}
		if (tokenType == TokenType.Identifier && stream.match(TokenType.Lambda, true)) {
			push();
			Expression expression = parseLambdaBody(stream, identifier, Arrays.asList(forceAdd(identifier.getText())));
			pop();
			return expression;
		}
		Expression result = tokenType == TokenType.StringLiteral ? new StringLiteral(identifier) : new VariableAccess(identifier, add(identifier.getText()));
		return parseAccessOrCall(stream, result);
	}

	private Expression parseAccessOrCall(TokenStream stream, Expression target) {
		while (stream.hasMore() && stream.match(false, TokenType.LeftParantheses, TokenType.LeftBracket, TokenType.Period, TokenType.QuestionPeriod)) {
			// function or method call
			if (stream.match(TokenType.LeftParantheses, false)) {
				List<Expression> arguments = parseArguments(stream);
				Span closingSpan = stream.expect(TokenType.RightParantheses).getSpan();
				if (target instanceof VariableAccess || target instanceof MapOrArrayAccess)
					target = new FunctionCall(new Span(target.getSpan(), closingSpan), target, arguments, linqLevel > 0);
				else if (target instanceof MemberAccess) {
					target = new MethodCall(new Span(target.getSpan(), closingSpan), (MemberAccess) target, arguments, linqLevel > 0);
				} else {
					MagicScriptError.error("Expected a variable, field or method.", stream);
				}
			}

			// map or array access
			else if (stream.match(TokenType.LeftBracket, true)) {
				Expression keyOrIndex = parseExpression(stream);
				Span closingSpan = stream.expect(TokenType.RightBracket).getSpan();
				target = new MapOrArrayAccess(new Span(target.getSpan(), closingSpan), target, keyOrIndex);
			}

			// field or method access
			else if (stream.match(false, TokenType.Period, TokenType.QuestionPeriod)) {
				boolean optional = stream.consume().getType() == TokenType.QuestionPeriod;
				if (linqLevel > 0 && stream.match(TokenType.Asterisk, false)) {
					target = new MemberAccess(target, optional, stream.expect(TokenType.Asterisk).getSpan(), true);
				} else {
					target = new MemberAccess(target, optional, stream.expect(TokenType.Identifier).getSpan(), false);
				}
			}
		}
		return target;
	}

	/**
	 * Does not consume the closing parentheses.
	 **/
	private List<Expression> parseArguments(TokenStream stream) {
		stream.expect(TokenType.LeftParantheses);
		List<Expression> arguments = new ArrayList<Expression>();
		while (stream.hasMore() && !stream.match(TokenType.RightParantheses, false)) {
			arguments.add(parseExpression(stream));
			if (!stream.match(TokenType.RightParantheses, false)) stream.expect(TokenType.Comma);
		}
		return arguments;
	}
}
