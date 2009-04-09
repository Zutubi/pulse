package com.zutubi.util.reflection;

import com.zutubi.util.Predicate;

import java.lang.reflect.Method;

/**
 * A predicate that checks if a method accepts parameters of given types, based
 * on {@link ReflectionUtils#acceptsParameters(java.lang.reflect.Method, Class[])}.
 */
public class MethodAcceptsParametersPredicate implements Predicate<Method>
{
    private Class<?>[] parameterTypes;

    /**
     * Create a predicate that will be satisfied by methods accepting the given
     * types.
     *
     * @param parameterTypes parameter types to test for
     */
    public MethodAcceptsParametersPredicate(Class<?>... parameterTypes)
    {
        this.parameterTypes = parameterTypes;
    }

    public boolean satisfied(Method method)
    {
        return ReflectionUtils.acceptsParameters(method, parameterTypes);
    }
}
