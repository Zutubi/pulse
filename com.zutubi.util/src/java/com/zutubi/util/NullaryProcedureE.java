package com.zutubi.util;

/**
 * Generic nullary procedure which may raise an exception.
 */
public interface NullaryProcedureE<E extends Exception>
{
    void run() throws E;
}
