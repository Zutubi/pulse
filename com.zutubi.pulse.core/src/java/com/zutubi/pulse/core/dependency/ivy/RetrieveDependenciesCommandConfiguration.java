package com.zutubi.pulse.core.dependency.ivy;

import com.zutubi.pulse.core.commands.api.CommandConfigurationSupport;

public class RetrieveDependenciesCommandConfiguration extends CommandConfigurationSupport
{
    private IvySupport ivy;

    public RetrieveDependenciesCommandConfiguration()
    {
        super(RetrieveDependenciesCommand.class);
    }

    public IvySupport getIvy()
    {
        return ivy;
    }

    public void setIvy(IvySupport ivy)
    {
        this.ivy = ivy;
    }
}
