package com.zutubi.util;

/**
 * Generic unary procedure, used as a callback type.
 */
public interface UnaryProcedure<T>
{
    void process(T t);
}
