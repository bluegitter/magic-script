package org.ssssssss.script.reflection;

import org.ssssssss.script.convert.ClassImplicitConvert;
import org.ssssssss.script.interpreter.AstInterpreter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Used by {@link AstInterpreter} to access fields and methods of objects. This is a singleton class used by all
 * {@link AstInterpreter} instances. Replace the default implementation via {@link #setInstance(AbstractReflection)}. The implementation
 * must be thread-safe.
 */
public abstract class AbstractReflection {
    private static AbstractReflection instance = new JavaReflection();

    /**
     * Returns the Reflection instance used to fetch field and call methods
     **/
    public synchronized static AbstractReflection getInstance() {
        return instance;
    }

    /**
     * Sets the Reflection instance to be used by all Template interpreters
     **/
    public synchronized static void setInstance(AbstractReflection abstractReflection) {
        instance = abstractReflection;
    }

    /**
     * Returns an opaque handle to a field with the given name or null if the field could not be found
     **/
    public abstract Field getField(Object obj, String name);

    /**
     * Returns an opaque handle to the method with the given name best matching the signature implied by the given arguments, or
     * null if the method could not be found. If obj is an instance of Class, the matching static method is returned. If the name
     * is null and the object is a {@link FunctionalInterface}, the first declared method on the object is returned.
     **/
    public abstract JavaInvoker<Method> getMethod(Object obj, String name, Object... arguments);

	public abstract JavaInvoker<Method> getFunction(String name, Object... arguments);

    public abstract void registerExtensionClass(Class<?> target, Class<?> clazz);

    public abstract void registerImplicitConvert(ClassImplicitConvert classImplicitConvert);

    /**
     * Returns the value of the field from the object. The field must have been previously retrieved via
     * {@link #getField(Object, String)}.
     **/
    public abstract Object getFieldValue(Object obj, Field field);

    public abstract void setFieldValue(Object obj, Field field,Object value);

}
