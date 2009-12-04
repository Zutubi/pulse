package com.zutubi.util;

/**
 * Mapping that converts objects to strings using ToString.
 */
public class ToStringMapping<T> implements Mapping<T, String>
{
    public String map(T t)
    {
        return t.toString();
    }
}
