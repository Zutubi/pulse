package com.zutubi.pulse.core;

import com.zutubi.pulse.core.commands.api.CommandConfigurationSupport;
import com.zutubi.tove.annotations.SymbolicName;

/**
 */
@SymbolicName("zutubi.noopCommandConfig")
public class NoopCommandConfiguration extends CommandConfigurationSupport
{
    public NoopCommandConfiguration()
    {
        super(NoopCommand.class);
    }

    public NoopCommandConfiguration(String name)
    {
        super(NoopCommand.class);
        setName(name);
    }
}