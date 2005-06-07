package com.cinnamonbob;

/**
 * 
 *
 */
public class BobException extends Exception
{
    /**
     * @param errorMessage
     */
    public BobException(String errorMessage)
    {
        super(errorMessage);
    }

    /**
     * 
     */
    public BobException()
    {
        super();
    }

    /**
     * @param cause
     */
    public BobException(Throwable cause)
    {
        super(cause);
    }

    /**
     * @param errorMessage
     * @param cause
     */
    public BobException(String errorMessage, Throwable cause)
    {
        super(errorMessage, cause);
    }

}
