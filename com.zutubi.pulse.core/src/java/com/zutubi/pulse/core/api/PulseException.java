package com.zutubi.pulse.core.api;

/**
 * Base class for checked exceptions specific to Pulse.
 */
public class PulseException extends Exception
{
    /**
     * Creates a new exception with no further information.
     */
    public PulseException()
    {
        super();
    }

    /**
     * Creates a new exception with a detail message.
     *
     * @param errorMessage the exception detail message
     */
    public PulseException(String errorMessage)
    {
        super(errorMessage);
    }

    /**
     * Creates a new exception with the given cause.
     *
     * @param cause what caused this exception
     */
    public PulseException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Creates a new exception with the given detail message and cause.
     *
     * @param errorMessage the exception detail message
     * @param cause what caused this exception
     */
    public PulseException(String errorMessage, Throwable cause)
    {
        super(errorMessage, cause);
    }
}
