package com.cinnamonbob.core.scm;

/**
 * An error raised during interaction with an SCM server.
 * 
 * @author jsankey
 */
public class SCMException extends Exception
{
    /**
     * Create a new SCM exception.
     * 
     * @param message
     *        human-readable error message
     * @param cause
     *        root cause of the error, or null if there is none
     */
    public SCMException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
