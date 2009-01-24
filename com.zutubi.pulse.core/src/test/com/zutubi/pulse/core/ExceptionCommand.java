package com.zutubi.pulse.core;

import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.commands.api.CommandSupport;
import com.zutubi.pulse.core.engine.api.BuildException;

/**
 * Simple testing command that always throws a BuildException.
 */
public class ExceptionCommand extends CommandSupport<ExceptionCommandConfiguration>
{
    public ExceptionCommand(ExceptionCommandConfiguration config)
    {
        super(config);
    }

    public void execute(CommandContext commandContext)
    {
        throw new BuildException("exception command");
    }
}
