package com.zutubi.pulse.core.dependency.ivy;

import com.zutubi.pulse.core.commands.api.CommandConfigurationSupport;

public class RetrieveDependenciesCommandConfiguration extends CommandConfigurationSupport
{
    private IvyClient ivy;

    public RetrieveDependenciesCommandConfiguration()
    {
        super(RetrieveDependenciesCommand.class);
        setName(RetrieveDependenciesCommand.COMMAND_NAME);
    }

    public IvyClient getIvy()
    {
        return ivy;
    }

    public void setIvy(IvyClient ivy)
    {
        this.ivy = ivy;
    }
}
