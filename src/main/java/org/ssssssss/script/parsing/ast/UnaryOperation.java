package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.compile.MagicScriptCompileException;
import org.ssssssss.script.compile.MagicScriptCompiler;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.Token;
import org.ssssssss.script.parsing.TokenType;
import org.ssssssss.script.parsing.ast.statement.VariableAccess;
import org.ssssssss.script.runtime.handle.OperatorHandle;

import java.util.List;
import java.util.function.Supplier;

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

    @Override
    public List<Span> visitSpan() {
        return operand.visitSpan();
    }

    public UnaryOperator getOperator() {
        return operator;
    }

    @Override
    public void visitMethod(MagicScriptCompiler compiler) {
        operand.visitMethod(compiler);
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

    @Override
    public void compile(MagicScriptCompiler compiler) {
        switch (getOperator()) {
            case Not:
                compiler.compile(operand)
                        .invoke(INVOKESTATIC, OperatorHandle.class, "isFalse", boolean.class,Object.class)
                        .asBoolean();
                break;
            case PlusPlus:
            case MinusMinus:
                if(operand instanceof VariableSetter){
                    boolean access = operand instanceof VariableAccess;
                    Supplier<MagicScriptCompiler> plus = () -> compiler.visit(operand)  // 访问变量
                            // 执行 ± 1 操作
                            .visitInt(operator == UnaryOperator.PlusPlus ? 1 : -1)
                            .asInteger()
                            .arithmetic("plus");
                    if(atAfter){    // ++ -- 在后
                        if(access){ // a++ a--
                            // 执行 ± 操作
                            compiler.compile(operand).pre_store(((VariableAccess) operand).getVarIndex());
                            plus.get().store();
                        }else{  // map.key++ map.key--
                            compiler.compile(operand);    // 先访问变量，后续返回使用
                            ((VariableSetter)operand).compile_visit_variable(compiler); // 赋值前准备
                            plus.get()
                                .call("set_variable_value",3)   // 赋值操作
                                .insn(POP); // 抛弃 ++ -- 的返回值。
                        }
                    }else{  // ++ -- 在前
                        if(access){ // ++a --a
                            compiler.pre_store(((VariableAccess) operand).getVarIndex());
                            // 执行 ± 操作
                            plus.get().store()   // 结果存入到变量中
                                .visit(operand);
                        }else{  // ++map.key --map.key
                            // 赋值前准备
                            ((VariableSetter)operand).compile_visit_variable(compiler);
                            // 将执行结果赋值给变量。
                            plus.get().call("set_variable_value",3);
                        }
                    }
                    break;
                }
                throw new MagicScriptCompileException("此处不支持++/--操作");
            case Negate:
                compiler.visit(operand)
                        .visitInt(-1)
                        .asInteger()
                        .arithmetic("mul");
                break;
        }
    }
}