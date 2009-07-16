package com.zutubi.util;

/**
 * Generic zero-argument function that may raise an exception.
 */
public interface NullaryFunctionE<T, E extends Throwable>
{
    T process() throws E;
}