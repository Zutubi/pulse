package com.cinnamonbob.core;

/**
 * 
 *
 */
public class BobRuntimeException extends RuntimeException
{
    /**
     * @param errorMessage
     */
    public BobRuntimeException(String errorMessage)
    {
        super(errorMessage);
    }

    /**
     * 
     */
    public BobRuntimeException()
    {
        super();
    }

    /**
     * @param cause
     */
    public BobRuntimeException(Throwable cause)
    {
        super(cause);
    }

    /**
     * @param errorMessage
     * @param cause
     */
    public BobRuntimeException(String errorMessage, Throwable cause)
    {
        super(errorMessage, cause);
    }


}
