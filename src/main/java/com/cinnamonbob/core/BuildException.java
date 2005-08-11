package com.cinnamonbob.core;

import com.cinnamonbob.BobRuntimeException;

/**
 * 
 *
 */
public class BuildException extends BobRuntimeException
{
    /**
     * @param errorMessage
     */
    public BuildException(String errorMessage)
    {
        super(errorMessage);
    }

    /**
     * 
     */
    public BuildException()
    {
        super();
    }

    /**
     * @param cause
     */
    public BuildException(Throwable cause)
    {
        super(cause);
    }

    /**
     * @param errorMessage
     * @param cause
     */
    public BuildException(String errorMessage, Throwable cause)
    {
        super(errorMessage, cause);
    }
}
