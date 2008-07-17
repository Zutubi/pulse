package com.zutubi.tove.config;

/**
 * Used for fatal configuration subsystem errors.
 */
public class ToveRuntimeException extends RuntimeException
{
    public ToveRuntimeException()
    {
    }

    public ToveRuntimeException(String message)
    {
        super(message);
    }

    public ToveRuntimeException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ToveRuntimeException(Throwable cause)
    {
        super(cause);
    }
}
