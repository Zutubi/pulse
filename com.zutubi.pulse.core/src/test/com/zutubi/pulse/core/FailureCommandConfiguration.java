package com.zutubi.pulse.core;

import com.zutubi.pulse.core.commands.api.BuildingCommandConfigurationSupport;
import com.zutubi.tove.annotations.SymbolicName;

/**
 */
@SymbolicName("zutubi.failureCommandConfig")
public class FailureCommandConfiguration extends BuildingCommandConfigurationSupport
{
    public FailureCommandConfiguration()
    {
        super(FailureCommand.class);
    }
}