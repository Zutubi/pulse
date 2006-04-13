/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.core;

/**
 * 
 *
 */
public class BuildException extends PulseRuntimeException
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
