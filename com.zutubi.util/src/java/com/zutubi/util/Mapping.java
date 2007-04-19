package com.zutubi.util;

/**
 * A function type that maps an object of type T to an object of type U.
 */
public interface Mapping<T, U>
{
    U map(T t);
}
