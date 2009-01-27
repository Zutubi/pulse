package com.zutubi.pulse.core;

import com.zutubi.pulse.core.commands.api.CommandConfigurationSupport;
import com.zutubi.tove.annotations.SymbolicName;

/**
 */
@SymbolicName("zutubi.failureCommandConfig")
public class FailureCommandConfiguration extends CommandConfigurationSupport
{
    public FailureCommandConfiguration()
    {
        super(FailureCommand.class);
    }
}