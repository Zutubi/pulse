package com.zutubi.pulse.master.scheduling;

import com.zutubi.pulse.core.api.PulseException;

/**
 * <class-comment/>
 */
public class SchedulingException extends PulseException
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