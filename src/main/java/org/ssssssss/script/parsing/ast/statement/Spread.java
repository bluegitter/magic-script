package org.ssssssss.script.parsing.ast.statement;

import org.ssssssss.script.compile.MagicScriptCompiler;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Expression;

/**
 * 展开语法 Spread syntax (...)
 */
public class Spread extends Expression {


    private final Expression target;

    public Spread(Span span, Expression target) {
        super(span);
        this.target = target;
    }

    @Override
    public void visitMethod(MagicScriptCompiler compiler) {
        target.visitMethod(compiler);
    }

    @Override
    public void compile(MagicScriptCompiler compiler) {
        // 对于...xxx 的参数 统一转换为 new Spread.Value(object)
        compiler.typeInsn(NEW, Value.class)
                .insn(DUP)
                .visit(target)
                .invoke(INVOKESPECIAL, Value.class, "<init>", void.class, Object.class);
    }

    public static class Value{

        private final Object value;

        public Value(Object value) {
            this.value = value;
        }

        public Object getValue() {
            return value;
        }

    }
}
