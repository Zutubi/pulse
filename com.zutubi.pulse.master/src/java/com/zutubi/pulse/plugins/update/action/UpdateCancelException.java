package com.zutubi.pulse.plugins.update.action;

import com.zutubi.pulse.core.PulseException;

/**
 */
public class UpdateCancelException extends PulseException
{
    public UpdateCancelException()
    {
    }

    public UpdateCancelException(String message)
    {
        super(message);
    }

    public UpdateCancelException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public UpdateCancelException(Throwable cause)
    {
        super(cause);
    }
}
