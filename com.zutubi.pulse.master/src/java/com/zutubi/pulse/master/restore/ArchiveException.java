package com.zutubi.pulse.master.restore;

import com.zutubi.pulse.master.util.monitor.TaskException;

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
