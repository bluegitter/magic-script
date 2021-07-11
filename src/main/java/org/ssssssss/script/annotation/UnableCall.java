package org.ssssssss.script.annotation;

import java.lang.annotation.*;

/**
 * 禁止脚本中调用的方法、字段
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UnableCall {
}
