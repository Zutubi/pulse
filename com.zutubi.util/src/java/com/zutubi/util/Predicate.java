package com.zutubi.util;

/**
 * Single-argument condition.
 */
public interface Predicate<T>
{
    boolean satisfied(T t);
}
