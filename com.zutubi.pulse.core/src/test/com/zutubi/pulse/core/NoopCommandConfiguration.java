package com.zutubi.pulse.core;

import com.zutubi.pulse.core.commands.api.BuildingCommandConfigurationSupport;
import com.zutubi.tove.annotations.SymbolicName;

/**
 */
@SymbolicName("zutubi.noopCommandConfig")
public class NoopCommandConfiguration extends BuildingCommandConfigurationSupport
{
    public NoopCommandConfiguration()
    {
        super(NoopCommand.class);
    }
}