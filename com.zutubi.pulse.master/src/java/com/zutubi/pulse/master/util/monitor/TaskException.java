package com.zutubi.pulse.master.util.monitor;

import com.zutubi.pulse.core.PulseException;

public class TaskException extends PulseException
{
    public TaskException()
    {
    }

    public TaskException(String message)
    {
        super(message);
    }

    public TaskException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public TaskException(Throwable cause)
    {
        super(cause);
    }
}
