package com.zutubi.pulse.core.api;

/**
 * Base class for unchecked exceptions specific to Pulse.
 */
public class PulseRuntimeException extends RuntimeException
{
    /**
     * Creates a new exception with no further information.
     */
    public PulseRuntimeException()
    {
        super();
    }

    /**
     * Creates a new exception with a detail message.
     *
     * @param errorMessage the exception detail message
     */
    public PulseRuntimeException(String errorMessage)
    {
        super(errorMessage);
    }

    /**
     * Creates a new exception with the given cause.
     *
     * @param cause what caused this exception
     */
    public PulseRuntimeException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Creates a new exception with the given detail message and cause.
     *
     * @param errorMessage the exception detail message
     * @param cause what caused this exception
     */
    public PulseRuntimeException(String errorMessage, Throwable cause)
    {
        super(errorMessage, cause);
    }
}
