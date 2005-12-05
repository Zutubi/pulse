package com.cinnamonbob.scheduling;

import com.cinnamonbob.core.BobException;

/**
 * <class-comment/>
 */
public class SchedulingException extends BobException
{
    /**
     * @param errorMessage
     */
    public SchedulingException(String errorMessage)
    {
        super(errorMessage);
    }

    /**
     *
     */
    public SchedulingException()
    {
        super();
    }

    /**
     * @param cause
     */
    public SchedulingException(Throwable cause)
    {
        super(cause);
    }

    /**
     * @param errorMessage
     * @param cause
     */
    public SchedulingException(String errorMessage, Throwable cause)
    {
        super(errorMessage, cause);
    }

}