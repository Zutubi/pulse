package com.zutubi.util;

/**
 * A {@link Predicate} that always returns false.
 */
public class FalsePredicate<T> implements Predicate<T>
{
    public boolean satisfied(T t)
    {
        return false;
    }
}