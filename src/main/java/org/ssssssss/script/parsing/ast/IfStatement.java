package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.interpreter.AstInterpreter;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.literal.BooleanLiteral;

import java.util.List;

public class IfStatement extends Node {
    private final Expression condition;
    private final List<Node> trueBlock;
    private final List<IfStatement> elseIfs;
    private final List<Node> falseBlock;

    public IfStatement(Span span, Expression condition, List<Node> trueBlock, List<IfStatement> elseIfs, List<Node> falseBlock) {
        super(span);
        this.condition = condition;
        this.trueBlock = trueBlock;
        this.elseIfs = elseIfs;
        this.falseBlock = falseBlock;
    }

    public Expression getCondition() {
        return condition;
    }

    public List<Node> getTrueBlock() {
        return trueBlock;
    }

    public List<IfStatement> getElseIfs() {
        return elseIfs;
    }

    public List<Node> getFalseBlock() {
        return falseBlock;
    }

    @Override
    public Object evaluate(MagicScriptContext context) {
        Object condition = getCondition().evaluate(context);
        if (BooleanLiteral.isTrue(condition)) {
			return AstInterpreter.interpretNodeList(getTrueBlock(), context);
        }

        if (getElseIfs().size() > 0) {
            for (IfStatement elseIf : getElseIfs()) {
                condition = elseIf.getCondition().evaluate(context);
                if (BooleanLiteral.isTrue(condition)) {
					return AstInterpreter.interpretNodeList(elseIf.getTrueBlock(), context);
                }
            }
        }

        if (getFalseBlock().size() > 0) {
			return AstInterpreter.interpretNodeList(getFalseBlock(), context);
        }
        return null;
    }
}