package com.zutubi.util;

/**
 * A {@link com.zutubi.util.Predicate} that always returns true.
 */
public class TruePredicate<T> implements Predicate<T>
{
    public boolean satisfied(T t)
    {
        return true;
    }
}
