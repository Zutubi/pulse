package com.zutubi.util;

/**
 * A unary procedure that does nothing.
 */
public class NullUnaryProcedure<T> implements UnaryProcedure<T>
{
    public void run(T t)
    {
    }
}
