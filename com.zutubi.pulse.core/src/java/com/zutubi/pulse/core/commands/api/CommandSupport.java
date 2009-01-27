package com.zutubi.pulse.core.commands.api;

import com.zutubi.pulse.core.Command;

/**
 * Support class to make implementing the Command interface simpler for the
 * simple cases.
 */
public abstract class CommandSupport implements Command
{
    private CommandConfigurationSupport config;

    protected CommandSupport(CommandConfigurationSupport config)
    {
        this.config = config;
    }

    public CommandConfigurationSupport getConfig()
    {
        return config;
    }

    public void terminate()
    {
    }
}
