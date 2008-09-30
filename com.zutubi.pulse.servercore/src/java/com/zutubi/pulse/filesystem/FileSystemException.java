package com.zutubi.pulse.filesystem;

import com.zutubi.pulse.core.PulseException;

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
