package com.zutubi.plugins;

/**
 * <class-comment/>
 */
public class PluginParseException extends PluginException
{
    public PluginParseException()
    {
    }

    public PluginParseException(String message)
    {
        super(message);
    }

    public PluginParseException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public PluginParseException(Throwable cause)
    {
        super(cause);
    }
}
