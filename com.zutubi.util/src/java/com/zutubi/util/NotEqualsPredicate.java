package com.zutubi.util;

/**
 * A {@link com.zutubi.util.Predicate} that tests for inequality with an object via {@link #equals(Object)}.
 */
public class NotEqualsPredicate<T> implements Predicate<T>
{
    private Object o;

    public NotEqualsPredicate(Object o)
    {
        this.o = o;
    }

    public boolean satisfied(T t)
    {
        return !t.equals(o);
    }
}