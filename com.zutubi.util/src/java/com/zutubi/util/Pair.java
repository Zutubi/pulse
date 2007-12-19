package com.zutubi.util;

/**
 * Simple struct to hold two objects.
 */
public class Pair<T, U>
{
    public T first;
    public U second;

    public Pair(T first, U second)
    {
        this.first = first;
        this.second = second;
    }
}
