/********************************************************************************
  @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.core;

/**
 * 
 *
 */
public class PulseRuntimeException extends RuntimeException
{
    /**
     * @param errorMessage
     */
    public PulseRuntimeException(String errorMessage)
    {
        super(errorMessage);
    }

    /**
     * 
     */
    public PulseRuntimeException()
    {
        super();
    }

    /**
     * @param cause
     */
    public PulseRuntimeException(Throwable cause)
    {
        super(cause);
    }

    /**
     * @param errorMessage
     * @param cause
     */
    public PulseRuntimeException(String errorMessage, Throwable cause)
    {
        super(errorMessage, cause);
    }


}
