package com.zutubi.pulse.restore;

import com.zutubi.pulse.core.PulseRuntimeException;

/**
 *
 *
 */
public class ArchiveRuntimeException extends PulseRuntimeException
{
    public ArchiveRuntimeException(String errorMessage)
    {
        super(errorMessage);
    }

    public ArchiveRuntimeException()
    {
    }

    public ArchiveRuntimeException(Throwable cause)
    {
        super(cause);
    }

    public ArchiveRuntimeException(String errorMessage, Throwable cause)
    {
        super(errorMessage, cause);
    }
}
