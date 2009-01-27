package com.zutubi.pulse.core;

import com.zutubi.pulse.core.commands.api.CommandConfigurationSupport;
import com.zutubi.tove.annotations.SymbolicName;

/**
 */
@SymbolicName("zutubi.unexpectedExceptionCommandConfig")
public class UnexpectedExceptionCommandConfiguration extends CommandConfigurationSupport
{
    public UnexpectedExceptionCommandConfiguration()
    {
        super(UnexpectedExceptionCommand.class);
    }
}