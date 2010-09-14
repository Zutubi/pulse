package com.zutubi.pulse.core.plugins.repository;

/**
 * Runtime exception raised on plugin repository error.
 */
public class PluginRepositoryException extends RuntimeException
{
    public PluginRepositoryException()
    {
    }

    public PluginRepositoryException(String message)
    {
        super(message);
    }

    public PluginRepositoryException(Throwable cause)
    {
        super(cause);
    }

    public PluginRepositoryException(String message, Throwable cause)
    {
        super(message, cause);
    }
}