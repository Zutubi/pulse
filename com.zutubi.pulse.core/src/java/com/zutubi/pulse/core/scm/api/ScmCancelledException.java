package com.zutubi.pulse.core.scm.api;

/**
 * An exception to be thrown when an operation is to be cancelled.
 */
public class ScmCancelledException extends ScmException
{
    public ScmCancelledException(String message)
    {
        super(message);
    }

    public ScmCancelledException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ScmCancelledException(Throwable cause)
    {
        super(cause);
    }
}
