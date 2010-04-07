package com.zutubi.pulse.core;

import com.zutubi.pulse.core.commands.api.CommandConfigurationSupport;

/**
 */
public class BootstrapCommandConfiguration extends CommandConfigurationSupport
{
    public static final String COMMAND_NAME = "bootstrap";
    public static final String OUTPUT_NAME = "bootstrap output";
    public static final String FILES_FILE = "files.txt";

    private Bootstrapper bootstrapper;

    public BootstrapCommandConfiguration(Bootstrapper bootstrapper)
    {
        super(BootstrapCommand.class);
        this.bootstrapper = bootstrapper;
        setName(COMMAND_NAME);
    }

    public Bootstrapper getBootstrapper()
    {
        return bootstrapper;
    }
}
