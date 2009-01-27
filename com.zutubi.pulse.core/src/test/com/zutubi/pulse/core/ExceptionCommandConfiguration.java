package com.zutubi.pulse.core;

import com.zutubi.pulse.core.commands.api.CommandConfigurationSupport;
import com.zutubi.tove.annotations.SymbolicName;

/**
 */
@SymbolicName("zutubi.exceptionCommandConfig")
public class ExceptionCommandConfiguration extends CommandConfigurationSupport
{
    public ExceptionCommandConfiguration()
    {
        super(ExceptionCommand.class);
    }
}
