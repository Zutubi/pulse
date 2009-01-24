package com.zutubi.pulse.core;

import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.commands.api.CommandSupport;

/**
 * Simple testing command that always throws a generic RuntimeException.
 */
public class UnexpectedExceptionCommand extends CommandSupport<UnexpectedExceptionCommandConfiguration>
{
    protected UnexpectedExceptionCommand(UnexpectedExceptionCommandConfiguration config)
    {
        super(config);
    }

    public void execute(CommandContext commandContext)
    {
        throw new RuntimeException("unexpected exception command");
    }
}
