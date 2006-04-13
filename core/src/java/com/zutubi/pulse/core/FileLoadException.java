/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.core;

/**
 * 
 *
 */
public class FileLoadException extends PulseException
{
    /**
     * @param errorMessage
     */
    public FileLoadException(String errorMessage)
    {
        super(errorMessage);
    }

    /**
     * 
     */
    public FileLoadException()
    {
        super();
    }

    /**
     * @param cause
     */
    public FileLoadException(Throwable cause)
    {
        super(cause);
    }

    /**
     * @param errorMessage
     * @param cause
     */
    public FileLoadException(String errorMessage, Throwable cause)
    {
        super(errorMessage, cause);
    }
}
