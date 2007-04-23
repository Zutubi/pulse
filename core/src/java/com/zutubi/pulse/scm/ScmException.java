package com.zutubi.pulse.scm;

import com.zutubi.pulse.core.PulseException;

/**
 * An error raised during interaction with an SCM server.
 *
 * @author jsankey
 */
public class ScmException extends PulseException
{
    /**
     * Create a new SCM exception.
     *
     * @param message human-readable error message
     * @param cause   root cause of the error, or null if there is none
     */
    public ScmException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Create a new SCM exception.
     *
     * @param message human-readable error message
     */
    public ScmException(String message)
    {
        super(message);
    }

    /**
     * Create a new SCM exception.
     */
    public ScmException()
    {

    }

    /**
     * Create a new SCM exception.
     *
     * @param cause root cause of the error, or null if there is none
     */
    public ScmException(Throwable cause)
    {
        super(cause);
    }
}
