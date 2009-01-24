package com.zutubi.pulse.core;

import com.zutubi.pulse.core.commands.api.BuildingCommandConfigurationSupport;
import com.zutubi.tove.annotations.SymbolicName;

/**
 */
@SymbolicName("zutubi.unexpectedExceptionCommandConfig")
public class UnexpectedExceptionCommandConfiguration extends BuildingCommandConfigurationSupport
{
    public UnexpectedExceptionCommandConfiguration()
    {
        super(UnexpectedExceptionCommand.class);
    }
}