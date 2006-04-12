package com.zutubi.pulse.scm;

import com.zutubi.pulse.core.PulseException;

/**
 * An error raised during interaction with an SCM server.
 *
 * @author jsankey
 */
public class SCMException extends PulseException
{
    /**
     * Create a new SCM exception.
     *
     * @param message human-readable error message
     * @param cause   root cause of the error, or null if there is none
     */
    public SCMException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Create a new SCM exception.
     *
     * @param message human-readable error message
     */
    public SCMException(String message)
    {
        super(message);
    }

    /**
     * Create a new SCM exception.
     */
    public SCMException()
    {

    }

    /**
     * Create a new SCM exception.
     *
     * @param cause root cause of the error, or null if there is none
     */
    public SCMException(Throwable cause)
    {
        super(cause);
    }
}
