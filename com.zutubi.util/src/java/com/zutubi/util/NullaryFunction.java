package com.zutubi.util;

/**
 * Generic zero-argument function.
 */
public interface NullaryFunction<T> extends NullaryFunctionE<T, RuntimeException>
{
    T process();
}
