package com.cinnamonbob.core.config;

import com.cinnamonbob.BobException;

/**
 * 
 *
 */
public class CommandException extends BobException
{
    /**
     * @param errorMessage
     */
    public CommandException(String errorMessage)
    {
        super(errorMessage);
    }

    /**
     * 
     */
    public CommandException()
    {
        super();
    }

    /**
     * @param cause
     */
    public CommandException(Throwable cause)
    {
        super(cause);
    }

    /**
     * @param errorMessage
     * @param cause
     */
    public CommandException(String errorMessage, Throwable cause)
    {
        super(errorMessage, cause);
    }
}
