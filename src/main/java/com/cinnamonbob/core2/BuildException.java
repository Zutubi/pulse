package com.cinnamonbob.core2;

import com.cinnamonbob.BobException;

/**
 * 
 *
 */
public class BuildException extends BobException
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
