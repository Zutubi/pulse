package com.zutubi.plugins;

/**
 * <class-comment/>
 */
public class PluginException extends Exception
{
    public PluginException()
    {
    }

    public PluginException(String message)
    {
        super(message);
    }

    public PluginException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public PluginException(Throwable cause)
    {
        super(cause);
    }
}
