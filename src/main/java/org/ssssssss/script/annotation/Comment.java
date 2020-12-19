package org.ssssssss.script.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Comment {

	String value();

	boolean origin() default false;
}
