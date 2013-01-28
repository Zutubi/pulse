package com.zutubi.util.reflection;

import com.google.common.base.Predicate;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * A predicate that checks if a method returns a type compatible with the
 * given specification, based on {@link com.zutubi.util.reflection.ReflectionUtils#returnsType(java.lang.reflect.Method, Class, java.lang.reflect.Type[])}.
 */
public class MethodReturnsTypePredicate implements Predicate<Method>
{
    private Class<?> rawClass;
    private Type[] typeArguments;

    /**
     * Create a predicate that will be satisfied by methods returning a type
     * compatible with the given details.
     *
     * @param rawClass      raw class the return type must be assignable to
     * @param typeArguments if not empty, specifies the return type must be
     *                      generic with the given type arguments
     */
    public MethodReturnsTypePredicate(Class<?> rawClass, Type... typeArguments)
    {
        this.rawClass = rawClass;
        this.typeArguments = typeArguments;
    }

    public boolean apply(Method method)
    {
        return ReflectionUtils.returnsType(method, rawClass, typeArguments);
    }
}