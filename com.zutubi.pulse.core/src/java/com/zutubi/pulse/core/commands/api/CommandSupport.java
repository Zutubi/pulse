package com.zutubi.pulse.core.commands.api;

import com.zutubi.pulse.core.Command;

/**
 * Support class to make implementing the Command interface simpler for the
 * simple cases.
 */
public abstract class CommandSupport<T extends CommandConfiguration> implements Command
{
    private T config;

    protected CommandSupport(T config)
    {
        this.config = config;
    }

    public T getConfig()
    {
        return config;
    }

    public void terminate()
    {
    }
}
