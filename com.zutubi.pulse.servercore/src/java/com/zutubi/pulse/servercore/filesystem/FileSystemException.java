package com.zutubi.pulse.servercore.filesystem;

import com.zutubi.pulse.core.api.PulseException;

/**
 * <class-comment/>
 */
public class FileSystemException extends PulseException
{
    public FileSystemException(String errorMessage)
    {
        super(errorMessage);
    }

    public FileSystemException()
    {
    }

    public FileSystemException(Throwable cause)
    {
        super(cause);
    }

    public FileSystemException(String errorMessage, Throwable cause)
    {
        super(errorMessage, cause);
    }
}
