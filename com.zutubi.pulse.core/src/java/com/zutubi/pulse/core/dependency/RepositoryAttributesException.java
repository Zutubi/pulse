package com.zutubi.pulse.core.dependency;

import com.zutubi.pulse.core.api.PulseRuntimeException;

/**
 * Raised when the repository attributes encounter a problem with reading or
 * writing the attributes to disk.
 */
public class RepositoryAttributesException extends PulseRuntimeException
{
    public RepositoryAttributesException()
    {
    }

    public RepositoryAttributesException(String message)
    {
        super(message);
    }

    public RepositoryAttributesException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public RepositoryAttributesException(Throwable cause)
    {
        super(cause);
    }
}
