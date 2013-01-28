package com.zutubi.util.reflection;

import com.google.common.base.Predicate;

import java.lang.reflect.Method;

/**
 * A predicate to test if a method name equals an expected name.
 */
public class MethodNameEqualsPredicate implements Predicate<Method>
{
    private String name;

    /**
     * Create a predicate matching methods with the given name.
     *
     * @param name the name to test for
     */
    public MethodNameEqualsPredicate(String name)
    {
        this.name = name;
    }

    public boolean apply(Method method)
    {
        return method.getName().equals(name);
    }
}