package com.zutubi.pulse.core;

import com.zutubi.pulse.core.commands.api.CommandConfigurationSupport;

/**
 */
public class BootstrapCommandConfiguration extends CommandConfigurationSupport
{
    private Bootstrapper bootstrapper;

    public BootstrapCommandConfiguration(Bootstrapper bootstrapper)
    {
        super(BootstrapCommand.class);
        this.bootstrapper = bootstrapper;
        setName("bootstrap");
    }

    public Bootstrapper getBootstrapper()
    {
        return bootstrapper;
    }
}
