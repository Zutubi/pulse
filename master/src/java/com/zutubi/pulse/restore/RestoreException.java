package com.zutubi.pulse.restore;

import com.zutubi.pulse.core.PulseException;

/**
 *
 *
 */
public class RestoreException extends PulseException
{
    public RestoreException(String errorMessage)
    {
        super(errorMessage);
    }

    public RestoreException()
    {
    }

    public RestoreException(Throwable cause)
    {
        super(cause);
    }

    public RestoreException(String errorMessage, Throwable cause)
    {
        super(errorMessage, cause);
    }
}
