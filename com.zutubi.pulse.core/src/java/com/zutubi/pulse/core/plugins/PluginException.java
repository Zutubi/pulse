package com.zutubi.pulse.core.plugins;

import com.zutubi.pulse.core.api.PulseException;

/**
 */
public class PluginException extends PulseException
{
    public PluginException()
    {
    }

    public PluginException(String errorMessage)
    {
        super(errorMessage);
    }

    public PluginException(Throwable cause)
    {
        super(cause);
    }

    public PluginException(String errorMessage, Throwable cause)
    {
        super(errorMessage, cause);
    }
}
