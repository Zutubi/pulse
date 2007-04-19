package com.zutubi.util;

/**
 * Simple binary predicate.
 */
public interface Predicate<T>
{
    boolean satisfied(T t);
}
