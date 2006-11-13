package com.zutubi.pulse.scm;

/**
 * An exception to be thrown when an operation is to be cancelled.
 */
public class SCMCancelledException extends SCMException
{
    public SCMCancelledException(String message)
    {
        super(message);
    }

    public SCMCancelledException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public SCMCancelledException(Throwable cause)
    {
        super(cause);
    }
}
