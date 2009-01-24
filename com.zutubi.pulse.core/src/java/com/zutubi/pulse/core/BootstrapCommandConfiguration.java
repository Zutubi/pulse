package com.zutubi.pulse.core;

import com.zutubi.pulse.core.commands.api.CommandConfigurationSupport;

/**
 */
public class BootstrapCommandConfiguration extends CommandConfigurationSupport
{
    private Bootstrapper bootstrapper;

    public BootstrapCommandConfiguration(Bootstrapper bootstrapper)
    {
        this.bootstrapper = bootstrapper;
        setName("bootstrap");
    }

    public Command createCommand()
    {
        return new BootstrapCommand(bootstrapper);
    }
}
