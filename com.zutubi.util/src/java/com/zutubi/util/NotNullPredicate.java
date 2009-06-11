package com.zutubi.util;

/**
 * A predicate that is satisfied by any non-null object.
 */
public class NotNullPredicate<T> implements Predicate<T>
{
    public boolean satisfied(T o)
    {
        return o != null;
    }
}
