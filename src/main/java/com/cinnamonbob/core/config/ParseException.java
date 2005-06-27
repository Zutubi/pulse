package com.cinnamonbob.core.config;

import com.cinnamonbob.BobException;

/**
 * 
 *
 */
public class ParseException extends BobException
{
    /**
     * @param errorMessage
     */
    public ParseException(String errorMessage)
    {
        super(errorMessage);
    }

    /**
     * 
     */
    public ParseException()
    {
        super();
    }

    /**
     * @param cause
     */
    public ParseException(Throwable cause)
    {
        super(cause);
    }

    /**
     * @param errorMessage
     * @param cause
     */
    public ParseException(String errorMessage, Throwable cause)
    {
        super(errorMessage, cause);
    }

}
