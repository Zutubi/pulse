package com.zutubi.pulse.core;

/**
 * 
 *
 */
public class PulseException extends Exception
{
    /**
     * @param errorMessage
     */
    public PulseException(String errorMessage)
    {
        super(errorMessage);
    }

    /**
     * 
     */
    public PulseException()
    {
        super();
    }

    /**
     * @param cause
     */
    public PulseException(Throwable cause)
    {
        super(cause);
    }

    /**
     * @param errorMessage
     * @param cause
     */
    public PulseException(String errorMessage, Throwable cause)
    {
        super(errorMessage, cause);
    }

}
