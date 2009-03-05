package com.zutubi.util;

/**
 * The identity function simply returns its input unchanged.
 */
public class IdentityFunction<T> implements UnaryFunction<T, T>
{
    public T process(T t)
    {
        return t;
    }
}
