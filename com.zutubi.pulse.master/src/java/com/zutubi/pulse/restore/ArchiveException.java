package com.zutubi.pulse.restore;

import com.zutubi.pulse.monitor.TaskException;

/**
 *
 *
 */
public class ArchiveException extends TaskException
{
    public ArchiveException()
    {
    }

    public ArchiveException(String errorMessage)
    {
        super(errorMessage);
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
