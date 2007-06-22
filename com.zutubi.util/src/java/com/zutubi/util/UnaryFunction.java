package com.zutubi.util;

/**
 * Generic unary function, used as a callback type.
 */
public interface UnaryFunction<T>
{
    void process(T t);
}
