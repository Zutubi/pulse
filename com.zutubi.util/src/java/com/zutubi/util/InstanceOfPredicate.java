package com.zutubi.util;

/**
 * Simple predicate to test if an object is an instanceof some type.
 */
public class InstanceOfPredicate<T> implements Predicate<T>
{
    private Class<?> type;

    public InstanceOfPredicate(Class<? extends T> type)
    {
        this.type = type;
    }

    public boolean satisfied(T t)
    {
        return type.isInstance(t);
    }
}
