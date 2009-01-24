package com.zutubi.pulse.core;

import com.zutubi.pulse.core.commands.api.BuildingCommandConfigurationSupport;
import com.zutubi.tove.annotations.SymbolicName;

/**
 */
@SymbolicName("zutubi.exceptionCommandConfig")
public class ExceptionCommandConfiguration extends BuildingCommandConfigurationSupport
{
    public ExceptionCommandConfiguration()
    {
        super(ExceptionCommand.class);
    }
}
