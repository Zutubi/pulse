package com.zutubi.util;

/**
 * A {@link Predicate} that tests for equality with an object via {@link #equals(Object)}.
 */
public class EqualsPredicate<T> implements Predicate<T>
{
    private Object o;

    public EqualsPredicate(Object o)
    {
        this.o = o;
    }

    public boolean satisfied(T t)
    {
        return t.equals(o);
    }
}
