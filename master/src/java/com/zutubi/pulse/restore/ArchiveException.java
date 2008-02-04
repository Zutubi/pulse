package com.zutubi.pulse.restore;

import com.zutubi.pulse.core.PulseException;

/**
 *
 *
 */
public class ArchiveException extends PulseException
{
    public ArchiveException(String errorMessage)
    {
        super(errorMessage);
    }

    public ArchiveException()
    {
    }

    public ArchiveException(Throwable cause)
    {
        super(cause);
    }

    public ArchiveException(String errorMessage, Throwable cause)
    {
        super(errorMessage, cause);
    }
}
