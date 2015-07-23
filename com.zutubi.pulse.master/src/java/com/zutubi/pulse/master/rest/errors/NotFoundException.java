package com.zutubi.pulse.master.rest.errors;

/**
 * Generic API exception for cases where a requested resource does not exist.
 */
public class NotFoundException extends RuntimeException
{
    public NotFoundException(String message)
    {
        super(message);
    }

    public NotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
