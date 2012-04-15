package com.zutubi.util;

/**
 * An exception raised when retries on error have failed.
 *
 * @see RetryHandler
 */
public class RetriesExhaustedException extends RuntimeException
{
    public RetriesExhaustedException()
    {
    }

    public RetriesExhaustedException(String message)
    {
        super(message);
    }

    public RetriesExhaustedException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public RetriesExhaustedException(Throwable cause)
    {
        super(cause);
    }
}
