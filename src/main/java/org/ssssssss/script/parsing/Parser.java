package org.ssssssss.script.parsing;


import org.ssssssss.script.MagicScript;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.parsing.ast.*;
import org.ssssssss.script.parsing.ast.binary.AssigmentOperation;
import org.ssssssss.script.parsing.ast.linq.*;
import org.ssssssss.script.parsing.ast.literal.*;
import org.ssssssss.script.parsing.ast.statement.*;

import javax.xml.transform.Source;
import java.util.*;


/**
 * Parses a {@link Source} into a {@link MagicScript}. The implementation is a simple recursive descent parser with a lookahead of
 * 1.
 **/
public class Parser {

	private static final TokenType[][] binaryOperatorPrecedence = new TokenType[][]{
			new TokenType[]{TokenType.Assignment},
			new TokenType[]{TokenType.PlusEqual, TokenType.MinusEqual, TokenType.AsteriskEqual, TokenType.ForwardSlashEqual, TokenType.PercentEqual},
			new TokenType[]{TokenType.Or, TokenType.And, TokenType.SqlOr, TokenType.SqlAnd, TokenType.Xor},
			new TokenType[]{TokenType.EqualEqualEqual, TokenType.Equal, TokenType.NotEqualEqual, TokenType.NotEqual, TokenType.SqlNotEqual},
			new TokenType[]{TokenType.Plus, TokenType.Minus},
			new TokenType[]{TokenType.Less, TokenType.LessEqual, TokenType.Greater, TokenType.GreaterEqual},
			new TokenType[]{TokenType.ForwardSlash, TokenType.Asterisk, TokenType.Percentage}
	};

	private static final TokenType[][] linqBinaryOperatorPrecedence = new TokenType[][]{
			new TokenType[]{TokenType.PlusEqual, TokenType.MinusEqual, TokenType.AsteriskEqual, TokenType.ForwardSlashEqual, TokenType.PercentEqual},
			new TokenType[]{TokenType.Or, TokenType.And, TokenType.SqlOr, TokenType.SqlAnd, TokenType.Xor},
			new TokenType[]{TokenType.Assignment, TokenType.EqualEqualEqual, TokenType.Equal, TokenType.NotEqualEqual, TokenType.NotEqual, TokenType.SqlNotEqual},
			new TokenType[]{TokenType.Plus, TokenType.Minus},
			new TokenType[]{TokenType.Less, TokenType.LessEqual, TokenType.Greater, TokenType.GreaterEqual},
			new TokenType[]{TokenType.ForwardSlash, TokenType.Asterisk, TokenType.Percentage}
	};

	private static final TokenType[] unaryOperators = new TokenType[]{TokenType.Not, TokenType.PlusPlus, TokenType.MinusMinus, TokenType.Plus, TokenType.Minus};

	private static final List<String> keywords = Arrays.asList("import", "as", "var", "return", "break", "continue", "if", "for", "in", "new", "true", "false", "null", "else", "try", "catch", "finally", "async", "while", "exit", "and", "or"/*, "assert"*/);

	private static final List<String> linqKeywords = Arrays.asList("from", "join", "left", "group", "by", "as", "having", "and", "or", "in", "where", "on");

	public static final String ANONYMOUS_VARIABLE = "-anonymous";

	private final List<List<VarIndex>> varNames = new ArrayList<>();

	private int varListIndex = -1;

	private int varCount = 0;

	private int linqLevel = 0;

	private boolean requiredNew = true;

	private TokenStream stream;

	private final List<Span> spans = new ArrayList<>();

	private final Set<VarIndex> varIndices = new LinkedHashSet<>();

	public List<Span> getSpans() {
		return spans;
	}

	public Set<VarIndex> getVarIndices() {
		return varIndices;
	}


	public List<Node> parse(String source) {
		List<Node> nodes = new ArrayList<Node>();
		push();
		stream = Tokenizer.tokenize(source);
		while (stream.hasMore()) {
			Node node = parseStatement();
			if (node != null) {
				validateNode(node);
				nodes.add(node);
			}
		}
		pop();
		return nodes;
	}

	private void validateNode(Node node) {
		if (node instanceof Literal || node instanceof VariableAccess || node instanceof MapOrArrayAccess) {
			MagicScriptError.error("literal cannot be used alone", node.getSpan());
		}
	}

	private Node parseStatement() {
		return parseStatement(false);
	}

	private Node parseStatement(boolean expectRightCurly) {
		Node result;
		if (stream.match("import", false)) {
			result = parseImport();
		} else if (stream.match("var", false)) {
			result = parseVarDefine();
		} else if (stream.match("if", false)) {
			result = parseIfStatement();
		} else if (stream.match("return", false)) {
			result = parseReturn();
		} else if (stream.match("for", false)) {
			result = parseForStatement();
		} else if (stream.match("while", false)) {
			result = parseWhileStatement();
		} else if (stream.match("continue", false)) {
			result = new Continue(stream.consume().getSpan());
		} else if (stream.match("async", false)) {
			result = parseAsync();
		} else if (stream.match("try", false)) {
			result = parseTryStatement();
		} else if (stream.match("break", false)) {
			result = new Break(stream.consume().getSpan());
		} else if (stream.match("exit", false)) {
			result = parseExit();
		} else if (stream.match("assert", false)) {
			result = parserAssert();
		} else {
			result = parseExpression(expectRightCurly);
		}
		// consume semi-colons as statement delimiters
		while (stream.match(";", true)) {
			;
		}
		return result;
	}

	private List<VarIndex> _this() {
		return varNames.get(varListIndex);
	}

	private int processVarIndex(int index) {
		for (int i = 0; i < varListIndex; i++) {
			index += varNames.get(i).size();
		}
		return index;
	}

	private VarIndex add(String name) {
		List<VarIndex> vars = _this();
		for (int i = varListIndex; i >= 0; i--) {
			List<VarIndex> varIndices = varNames.get(i);
			for (int j = varIndices.size() - 1; j >= 0; j--) {
				VarIndex varIndex = varIndices.get(j);
				if (varIndex.getName().equals(name)) {
					return varIndex;
				}
			}
		}
		return add(new VarIndex(name, varCount++, true));
	}

	private VarIndex add(VarIndex varIndex) {
		varIndices.add(varIndex);
		_this().add(varIndex);
		return varIndex;
	}

	private VarIndex forceAdd(String name) {
		return add(new VarIndex(name, varCount++, false));
	}

	private void push() {
		varListIndex++;
		varNames.add(new ArrayList<>());
	}

	private int pop() {
		varListIndex--;
		return -1;
	}

	private Span addSpan(Span opening, Span ending) {
		addSpan(opening);
		addSpan(ending);
		return addSpan(new Span(opening, ending));
	}

	private Span addSpan(String source, int start, int end) {
		return addSpan(new Span(source, start, end));
	}

	private Span addSpan(Span span) {
		this.spans.add(span);
		return span;
	}

	private Node parseExit() {
		Span opening = stream.expect("exit").getSpan();
		List<Expression> expressionList = new ArrayList<>();
		do {
			expressionList.add(parseExpression());
		} while (stream.match(TokenType.Comma, true));
		return new Exit(addSpan(opening, stream.getPrev().getSpan()), expressionList);
	}
	private Node parserAssert() {
		int index = stream.makeIndex();
		try {
			Span opening = stream.expect("assert").getSpan();
			Expression condition = parseExpression();
			stream.expect(TokenType.Colon);
			List<Expression> expressionList = new ArrayList<>();
			do {
				expressionList.add(parseExpression());
			} while (stream.match(TokenType.Comma, true));
			return new Assert(addSpan(opening, stream.getPrev().getSpan()), condition, expressionList);
		} catch (Exception e) {
			stream.resetIndex(index);
			return parseExpression();
		}
	}

	private Expression parseAsync() {
		Span opening = stream.expect("async").getSpan();
		requiredNew = false;
		Expression expression = parseExpression();
		requiredNew = true;
		if (expression instanceof MethodCall || expression instanceof FunctionCall || expression instanceof LambdaFunction) {
			return new AsyncCall(addSpan(opening, stream.getPrev().getSpan()), expression);
		}
		MagicScriptError.error("Expected MethodCall or FunctionCall or LambdaFunction", stream.getPrev().getSpan());
		return null;
	}

	private Import parseImport() {
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
			if (isStringLiteral) {
				if (stream.match("as", true)) {
					expected = stream.expect(TokenType.Identifier);
					checkKeyword(expected.getSpan());
					varName = expected.getSpan().getText();
				} else {
					String temp = new StringLiteral(expected.getSpan()).getValue();
					if (!temp.startsWith("@")) {
						int index = temp.lastIndexOf(".");
						if (index != -1) {
							temp = temp.substring(index + 1);
						}
					} else {
						MagicScriptError.error("Expected as", stream);
					}
					varName = temp;
				}
			}
			return new Import(addSpan(opening, expected.getSpan()), packageName, forceAdd(varName), !isStringLiteral);
		}
		MagicScriptError.error("Expected identifier or string, but got stream is EOF", stream.getPrev().getSpan());
		return null;
	}

	private TryStatement parseTryStatement() {
		Token opening = stream.expect("try");
		push();
		List<Node> tryBlocks = parseFunctionBody();
		pop();
		List<Node> catchBlocks = new ArrayList<>();
		List<Node> finallyBlocks = new ArrayList<>();
		VarIndex exceptionVarNode = null;
		if (stream.match("catch", true)) {
			push();
			if (stream.match("(", true)) {
				exceptionVarNode = add(stream.expect(TokenType.Identifier).getText());
				stream.expect(")");
			}
			catchBlocks.addAll(parseFunctionBody());
			pop();
		}
		if (stream.match("finally", true)) {
			push();
			finallyBlocks.addAll(parseFunctionBody());
			pop();
		}
		return new TryStatement(addSpan(opening.getSpan(), stream.getPrev().getSpan()), exceptionVarNode, tryBlocks, catchBlocks, finallyBlocks);
	}

	private List<Node> parseFunctionBody() {
		stream.expect("{");
		List<Node> blocks = new ArrayList<>();
		while (stream.hasMore() && !stream.match("}", false)) {
			Node node = parseStatement(true);
			if (node != null) {
				validateNode(node);
				blocks.add(node);
			}
		}
		expectCloseing();
		return blocks;
	}

	private Expression parseNewExpression(Span opening) {
		Expression expression = parseAccessOrCall(TokenType.Identifier, true);
		if (expression instanceof MethodCall) {
			MethodCall call = (MethodCall) expression;
			Span span = addSpan(opening.getSource(), opening.getStart(), stream.getPrev().getSpan().getEnd());
			return parseConverterOrAccessOrCall(new NewStatement(span, call.getMethod(), call.getArguments()));
		} else if (expression instanceof FunctionCall) {
			FunctionCall call = (FunctionCall) expression;
			Span span = addSpan(opening.getSource(), opening.getStart(), stream.getPrev().getSpan().getEnd());
			return parseConverterOrAccessOrCall(new NewStatement(span, call.getFunction(), call.getArguments()));
		}
		MagicScriptError.error("Expected MethodCall or FunctionCall or LambdaFunction", stream.getPrev().getSpan());
		return null;
	}

	private VariableDefine parseVarDefine() {
		Span opening = stream.expect("var").getSpan();
		Token token = stream.expect(TokenType.Identifier);
		checkKeyword(token.getSpan());
		String variableName = token.getSpan().getText();
		if (stream.match(TokenType.Assignment, true)) {
			return new VariableDefine(addSpan(opening, stream.getPrev().getSpan()), forceAdd(variableName), parseExpression());
		}
		return new VariableDefine(addSpan(opening, stream.getPrev().getSpan()), forceAdd(variableName), null);
	}

	private void checkKeyword(Span span) {
		if (keywords.contains(span.getText())) {
			MagicScriptError.error("变量名不能定义为关键字", span);
		}
	}

	private WhileStatement parseWhileStatement() {
		Span openingWhile = stream.expect("while").getSpan();
		requiredNew = false;
		Expression condition = parseExpression();
		requiredNew = true;
		push();
		List<Node> trueBlock = parseFunctionBody();
		Span closingEnd = stream.getPrev().getSpan();
		pop();
		return new WhileStatement(addSpan(openingWhile, closingEnd), condition, trueBlock);
	}

	private ForStatement parseForStatement() {
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
		Expression mapOrArray = parseExpression();
		stream.expect(")");
		List<Node> body = parseFunctionBody();
		VarIndex anonymous = forceAdd(ANONYMOUS_VARIABLE);
		pop();
		return new ForStatement(addSpan(openingFor, stream.getPrev().getSpan()), indexOrKeyNode, valueNode, anonymous, mapOrArray, body);
	}

	private Span expectCloseing() {
		if (!stream.hasMore()) {
			MagicScriptError.error("Did not find closing }.", stream.prev().getSpan());
		}
		return stream.expect("}").getSpan();
	}

	private Node parseIfStatement() {
		Span openingIf = stream.expect("if").getSpan();
		requiredNew = false;
		Expression condition = parseExpression();
		requiredNew = true;
		push();
		List<Node> trueBlock = parseFunctionBody();
		pop();
		List<IfStatement> elseIfs = new ArrayList<>();
		List<Node> falseBlock = new ArrayList<>();
		while (stream.hasMore() && stream.match("else", true)) {
			if (stream.hasMore() && stream.match("if", false)) {
				Span elseIfOpening = stream.expect("if").getSpan();
				Expression elseIfCondition = parseExpression();
				push();
				List<Node> elseIfBlock = parseFunctionBody();
				Span elseIfSpan = addSpan(elseIfOpening, elseIfBlock.size() > 0 ? elseIfBlock.get(elseIfBlock.size() - 1).getSpan() : elseIfOpening);
				pop();
				elseIfs.add(new IfStatement(elseIfSpan, elseIfCondition, elseIfBlock, new ArrayList<>(), new ArrayList<>()));
			} else {
				push();
				falseBlock.addAll(parseFunctionBody());
				pop();
				break;
			}
		}
		Span closingEnd = stream.getPrev().getSpan();

		return new IfStatement(addSpan(openingIf, closingEnd), condition, trueBlock, elseIfs, falseBlock);
	}

	private Node parseReturn() {
		Span returnSpan = stream.expect("return").getSpan();
		if (stream.match(";", false)) return new Return(returnSpan, null);
		Expression returnValue = parseExpression();
		return new Return(addSpan(returnSpan, returnValue.getSpan()), returnValue);
	}

	public Expression parseExpression() {
		return parseTernaryOperator();
	}

	public Expression parseExpression( boolean expectRightCurly) {
		return parseTernaryOperator(expectRightCurly);
	}

	private Expression parseTernaryOperator( boolean expectRightCurly) {
		Expression condition = parseBinaryOperator(0, expectRightCurly);
		if (stream.match(TokenType.Questionmark, true)) {
			Expression trueExpression = parseTernaryOperator(expectRightCurly);
			stream.expect(TokenType.Colon);
			Expression falseExpression = parseTernaryOperator(expectRightCurly);
			if (condition instanceof AssigmentOperation) {
				AssigmentOperation operation = (AssigmentOperation) condition;
				operation.setRightOperand(new TernaryOperation(operation.getRightOperand(), trueExpression, falseExpression));
				return operation;
			}
			return new TernaryOperation(condition, trueExpression, falseExpression);
		} else {
			return condition;
		}
	}

	private Expression parseTernaryOperator() {
		return parseTernaryOperator(false);
	}

	private Expression parseBinaryOperator(TokenType[][] precedence, int level, boolean expectRightCurly) {
		int nextLevel = level + 1;
		Expression left = nextLevel == precedence.length ? parseUnaryOperator(expectRightCurly) : parseBinaryOperator(nextLevel, expectRightCurly);

		TokenType[] operators = precedence[level];
		while (stream.hasMore() && stream.match(false, operators)) {
			Token operator = stream.consume();
			if (operator.getType().isInLinq() && linqLevel == 0) {
				MagicScriptError.error(operator.getText() + " 只能在Linq中使用", stream);
			}
			Expression right = nextLevel == precedence.length ? parseUnaryOperator(expectRightCurly) : parseBinaryOperator(nextLevel, expectRightCurly);
			left = BinaryOperation.create(left, operator, right, linqLevel);
		}
		addSpan(left.getSpan());
		return left;
	}

	private Expression parseBinaryOperator(int level, boolean expectRightCurly) {
		if (linqLevel > 0) {
			return parseBinaryOperator(linqBinaryOperatorPrecedence, level, expectRightCurly);
		}
		return parseBinaryOperator(binaryOperatorPrecedence, level, expectRightCurly);
	}


	private Expression parseUnaryOperator(boolean expectRightCurly) {
		if (stream.match(false, unaryOperators)) {
			return new UnaryOperation(stream.consume(), parseUnaryOperator(expectRightCurly));
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
						if (requiredNew) {
							parameters.add(forceAdd(identifier.getSpan().getText()));
						} else {
							parameters.add(add(identifier.getSpan().getText()));
						}
						if (stream.match(TokenType.Comma, true)) { //,
							continue;
						}
						if (stream.match(TokenType.RightParantheses, true)) {  //)
							if (stream.match(TokenType.Lambda, true)) {   // =>
								return parseLambdaBody(openSpan, parameters);
							}
							break;
						}
					}
					if (stream.match(TokenType.RightParantheses, true) && stream.match(TokenType.Lambda, true)) {
						return parseLambdaBody(openSpan, parameters);
					}
				} finally {
					pop();
				}
				stream.resetIndex(index);
				Expression expression = parseExpression();
				stream.expect(TokenType.RightParantheses);
				return parseConverterOrAccessOrCall(expression);
			} else {
				Expression expression = parseAccessOrCallOrLiteral(expectRightCurly);
				if (expression instanceof VariableSetter) {
					if (stream.match(false, TokenType.PlusPlus, TokenType.MinusMinus)) {
						return new UnaryOperation(stream.consume(), expression, true);
					}
				}

				return expression;
			}
		}
	}

	private Expression parseConverterOrAccessOrCall(Expression expression) {
		while (stream.match(false, TokenType.Period, TokenType.QuestionPeriod, TokenType.ColonColon)) {
			if (stream.match(TokenType.ColonColon, false)) {
				Span open = stream.consume().getSpan();
				List<Expression> arguments = Collections.emptyList();
				Token identifier = stream.expect(TokenType.Identifier);
				Span closing = identifier.getSpan();
				if (stream.match(TokenType.LeftParantheses, false)) {
					arguments = parseArguments();
					closing = stream.expect(TokenType.RightParantheses).getSpan();
				}
				expression = new ClassConverter(addSpan(open, closing), identifier.getText(), expression, arguments);
			} else {
				expression = parseAccessOrCall(expression, false);
			}
		}
		return expression;
	}

	private Expression parseLambdaBody(Span openSpan, List<VarIndex> parameters) {
		int index = stream.makeIndex();
		List<Node> childNodes = new ArrayList<>();
		try {
			Expression expression = parseExpression();
			childNodes.add(new Return(new Span("return", 0, 6), expression));
			return new LambdaFunction(addSpan(openSpan, expression.getSpan()), parameters, childNodes);
		} catch (Exception e) {
			stream.resetIndex(index);
			if (stream.match(TokenType.LeftCurly, true)) {
				while (stream.hasMore() && !stream.match(false, "}")) {
					Node node = parseStatement(true);
					validateNode(node);
					childNodes.add(node);
				}
				Span closeSpan = expectCloseing();
				return new LambdaFunction(addSpan(openSpan, closeSpan), parameters, childNodes);
			} else {
				Node node = parseStatement();
				childNodes.add(new Return(addSpan("return", 0, 6), node));
				return new LambdaFunction(addSpan(openSpan, node.getSpan()), parameters, childNodes);
			}
		}
	}

	private Expression parseSpreadAccess(Token spread) {
		Expression target = parseExpression();
		return new Spread(addSpan(spread.getSpan(), target.getSpan()), target);
	}

	private Expression parseSpreadAccess() {
		Token spread = stream.expect(TokenType.Spread);
		return parseSpreadAccess(spread);
	}

	private Expression parseSelect() {
		Span opeing = stream.expect("select", true).getSpan();
		linqLevel++;
		List<LinqField> fields = parseLinqFields();
		stream.expect("from", true);
		LinqField from = parseLinqField();
		List<LinqJoin> joins = parseLinqJoins();
		LinqExpression where = null;
		if (stream.match("where", true, true)) {
			where = new LinqExpression(parseExpression());
		}
		List<LinqField> groups = parseGroup();
		LinqExpression having = null;
		if (stream.match("having", true, true)) {
			having = new LinqExpression(parseExpression());
		}
		List<LinqOrder> orders = parseLinqOrders();
		linqLevel--;
		Span close = stream.getPrev().getSpan();
		return new LinqSelect(addSpan(opeing, close), fields, from, joins, where, groups, having, orders);
	}

	private List<LinqField> parseGroup() {
		List<LinqField> groups = new ArrayList<>();
		if (stream.match("group", true, true)) {
			stream.expect("by", true);
			do {
				Expression expression = parseExpression();
				groups.add(new LinqField(expression.getSpan(), expression, null));
			} while (stream.match(TokenType.Comma, true));
		}
		return groups;
	}

	private List<LinqOrder> parseLinqOrders() {
		List<LinqOrder> orders = new ArrayList<>();
		if (stream.match("order", true, true)) {
			stream.expect("by", true);
			do {
				Expression expression = parseExpression();
				int order = 1;
				if (stream.match(false, true, "desc", "asc")) {
					if ("desc".equalsIgnoreCase(stream.consume().getText())) {
						order = -1;
					}
				}
				orders.add(new LinqOrder(addSpan(expression.getSpan(), stream.getPrev().getSpan()), expression, null, order));
			} while (stream.match(TokenType.Comma, true));
		}
		return orders;
	}

	private List<LinqField> parseLinqFields() {
		List<LinqField> fields = new ArrayList<>();
		do {
			Expression expression = parseExpression();

			if (stream.match(TokenType.Identifier, false) && !stream.match(linqKeywords, false, true)) {
				if (expression instanceof WholeLiteral) {
					MagicScriptError.error("* 后边不能跟别名", stream);
				} else if (expression instanceof MemberAccess && ((MemberAccess) expression).isWhole()) {
					MagicScriptError.error(expression.getSpan().getText() + " 后边不能跟别名", stream);
				}
				Span alias = stream.consume().getSpan();
				fields.add(new LinqField(addSpan(expression.getSpan(), alias), expression, add(alias.getText())));
			} else {
				fields.add(new LinqField(expression.getSpan(), expression, null));
			}
		} while (stream.match(TokenType.Comma, true));    //,
		if (fields.isEmpty()) {
			MagicScriptError.error("至少要查询一个字段", stream);
		}
		return fields;
	}

	private List<LinqJoin> parseLinqJoins() {
		List<LinqJoin> joins = new ArrayList<>();
		do {
			boolean isLeft = stream.match("left", false, true);
			Span opeing = isLeft ? stream.consume().getSpan() : null;
			if (stream.match("join", true, true)) {
				opeing = isLeft ? opeing : stream.getPrev().getSpan();
				LinqField target = parseLinqField();
				stream.expect("on", true);
				Expression condition = parseExpression();
				joins.add(new LinqJoin(addSpan(opeing, stream.getPrev().getSpan()), isLeft, target, condition));
			}
		} while (stream.match(false, true, "left", "join"));
		return joins;
	}

	private LinqField parseLinqField() {
		Expression expression = parseExpression();
		if (stream.match(TokenType.Identifier, false) && !stream.match(linqKeywords, false, true)) {
			Span alias = stream.expect(TokenType.Identifier).getSpan();
			return new LinqField(addSpan(expression.getSpan(), alias), expression, add(alias.getText()));
		}
		return new LinqField(expression.getSpan(), expression, null);
	}

	private Expression parseAccessOrCallOrLiteral(boolean expectRightCurly) {
		Expression expression = null;
		if (expectRightCurly && stream.match("}", false)) {
			return null;
		} else if (stream.match("async", false)) {
			expression = parseAsync();
		} else if (stream.match("select", false, true)) {
			expression = parseSelect();
		} else if (stream.match(TokenType.Spread, false)) {
			expression = parseSpreadAccess();
		} else if (stream.match(TokenType.Identifier, false)) {
			expression = parseAccessOrCall(TokenType.Identifier, false);
		} else if (stream.match(TokenType.LeftCurly, false)) {
			expression = parseMapLiteral();
		} else if (stream.match(TokenType.LeftBracket, false)) {
			expression = parseListLiteral();
		} else if (stream.match(TokenType.StringLiteral, false)) {
			expression = new StringLiteral(stream.expect(TokenType.StringLiteral).getSpan());
		} else if (stream.match(TokenType.BooleanLiteral, false)) {
			expression = new BooleanLiteral(stream.expect(TokenType.BooleanLiteral).getSpan());
		} else if (stream.match(TokenType.DoubleLiteral, false)) {
			expression = new DoubleLiteral(stream.expect(TokenType.DoubleLiteral).getSpan());
		} else if (stream.match(TokenType.FloatLiteral, false)) {
			expression = new FloatLiteral(stream.expect(TokenType.FloatLiteral).getSpan());
		} else if (stream.match(TokenType.ByteLiteral, false)) {
			expression = new ByteLiteral(stream.expect(TokenType.ByteLiteral).getSpan());
		} else if (stream.match(TokenType.ShortLiteral, false)) {
			expression = new ShortLiteral(stream.expect(TokenType.ShortLiteral).getSpan());
		} else if (stream.match(TokenType.IntegerLiteral, false)) {
			expression = new IntegerLiteral(stream.expect(TokenType.IntegerLiteral).getSpan());
		} else if (stream.match(TokenType.LongLiteral, false)) {
			expression = new LongLiteral(stream.expect(TokenType.LongLiteral).getSpan());
		} else if (stream.match(TokenType.DecimalLiteral, false)) {
			expression = new BigDecimalLiteral(stream.expect(TokenType.DecimalLiteral).getSpan());
		} else if (stream.match(TokenType.RegexpLiteral, false)) {
			Token token = stream.expect(TokenType.RegexpLiteral);
			Expression target = new RegexpLiteral(token.getSpan(), token);
			expression = parseAccessOrCall(target, false);
		} else if (stream.match(TokenType.NullLiteral, false)) {
			expression = new NullLiteral(stream.expect(TokenType.NullLiteral).getSpan());
		} else if (linqLevel > 0 && stream.match(TokenType.Asterisk, false)) {
			expression = new WholeLiteral(stream.expect(TokenType.Asterisk).getSpan());
		} else if (stream.match(TokenType.Language, false)) {
			expression = new LanguageExpression(stream.consume().getSpan(), stream.consume().getSpan());
		}
		if (expression instanceof StringLiteral && stream.match(false, TokenType.Period, TokenType.QuestionPeriod)) {
			stream.prev();
			expression = parseAccessOrCall(TokenType.StringLiteral, false);
		}
		if (expression == null) {
			MagicScriptError.error("Expected a variable, field, map, array, function or method call, or literal.", stream);
		}
		return parseConverterOrAccessOrCall(expression);
	}


	private Expression parseMapLiteral() {
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
					values.add(parseSpreadAccess(spread));
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
				values.add(parseExpression());
				if (!stream.match("}", false)) {
					stream.expect(TokenType.Comma);
				}
			}
		}
		Span closeCurly = stream.expect("}").getSpan();
		return new MapLiteral(addSpan(openCurly, closeCurly), keys, values);
	}

	private Expression parseListLiteral() {
		Span openBracket = stream.expect(TokenType.LeftBracket).getSpan();

		List<Expression> values = new ArrayList<>();
		while (stream.hasMore() && !stream.match(TokenType.RightBracket, false)) {
			values.add(parseExpression());
			if (!stream.match(TokenType.RightBracket, false)) {
				stream.expect(TokenType.Comma);
			}
		}

		Span closeBracket = stream.expect(TokenType.RightBracket).getSpan();
		return parseConverterOrAccessOrCall(new ListLiteral(addSpan(openBracket, closeBracket), values));
	}


	private Expression parseAccessOrCall(TokenType tokenType, boolean isNew) {
		//Span identifier = stream.expect(TokenType.Identifier);
		//Expression result = new VariableAccess(identifier);
		Span identifier = stream.expect(tokenType).getSpan();
		if (tokenType == TokenType.Identifier && "new".equals(identifier.getText())) {
			return parseNewExpression(identifier);
		}
		if (tokenType == TokenType.Identifier && stream.match(TokenType.Lambda, true)) {
			push();
			String name = identifier.getText();
			Expression expression = parseLambdaBody(identifier, Collections.singletonList(requiredNew ? forceAdd(name) : add(name)));
			pop();
			return expression;
		}
		Expression result = tokenType == TokenType.StringLiteral ? new StringLiteral(identifier) : new VariableAccess(identifier, add(identifier.getText()));
		return parseAccessOrCall(result, isNew);
	}

	private Expression parseAccessOrCall(Expression target, boolean isNew) {
		while (stream.hasMore() && stream.match(false, TokenType.LeftParantheses, TokenType.LeftBracket, TokenType.Period, TokenType.QuestionPeriod)) {
			// function or method call
			if (stream.match(TokenType.LeftParantheses, false)) {
				List<Expression> arguments = parseArguments();
				Span closingSpan = stream.expect(TokenType.RightParantheses).getSpan();
				if (target instanceof VariableAccess || target instanceof MapOrArrayAccess)
					target = new FunctionCall(addSpan(target.getSpan(), closingSpan), target, arguments, linqLevel > 0);
				else if (target instanceof MemberAccess) {
					target = new MethodCall(addSpan(target.getSpan(), closingSpan), (MemberAccess) target, arguments, linqLevel > 0);
				} else {
					MagicScriptError.error("Expected a variable, field or method.", stream);
				}
				if (isNew) {
					break;
				}
			}

			// map or array access
			else if (stream.match(TokenType.LeftBracket, true)) {
				Expression keyOrIndex = parseExpression();
				Span closingSpan = stream.expect(TokenType.RightBracket).getSpan();
				target = new MapOrArrayAccess(addSpan(target.getSpan(), closingSpan), target, keyOrIndex);
			}

			// field or method access
			else if (stream.match(false, TokenType.Period, TokenType.QuestionPeriod)) {
				boolean optional = stream.consume().getType() == TokenType.QuestionPeriod;
				if (linqLevel > 0 && stream.match(TokenType.Asterisk, false)) {
					target = new MemberAccess(target, optional, stream.expect(TokenType.Asterisk).getSpan(), true);
				} else {
					target = new MemberAccess(target, optional, stream.expect(TokenType.Identifier, TokenType.SqlAnd, TokenType.SqlOr).getSpan(), false);
				}
			}
		}
		return target;
	}

	/**
	 * Does not consume the closing parentheses.
	 **/
	private List<Expression> parseArguments() {
		stream.expect(TokenType.LeftParantheses);
		List<Expression> arguments = new ArrayList<Expression>();
		while (stream.hasMore() && !stream.match(TokenType.RightParantheses, false)) {
			arguments.add(parseExpression());
			if (!stream.match(TokenType.RightParantheses, false)) stream.expect(TokenType.Comma);
		}
		return arguments;
	}
}
