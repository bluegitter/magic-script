package org.ssssssss.script.parsing.ast.literal;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Literal;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

/**
 * boolean常量
 */
public class BooleanLiteral extends Literal {
    private final Boolean value;

    public BooleanLiteral(Span literal) {
        super(literal);
        this.value = Boolean.parseBoolean(literal.getText());
    }

    @Override
    public Object evaluate(MagicScriptContext context) {
        return value;
    }

    public static boolean isTrue(Object object){
        if(object == null){
            return false;
        }
        if(object instanceof Boolean){
            return (Boolean) object;
        }
        if(object instanceof Number){   // 非0 为 true
            return ((Number)object).doubleValue() != 0;
        }
        if(object instanceof Collection){   // 非空集合
            return !((Collection) object).isEmpty();
        }
        if(object.getClass().isArray()){    // 非空数组
            return Array.getLength(object) > 0;
        }
        if(object instanceof String){   // 非空字符串
            return !object.toString().isEmpty();
        }
        if(object instanceof Map){  // 非空Map
            return !((Map)object).isEmpty();
        }
        // 其它情况全视作为true
        return true;
    }
}