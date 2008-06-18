package com.zutubi.util;

/**
 * Generic unary function, takes one input and transforms it to one output.
 */
public interface UnaryFunction<T, U>
{
    U process(T t);
}
