package com.zutubi.pulse.core;

import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.commands.api.CommandSupport;

/**
 * Simple testing command that always fails.
 */
public class FailureCommand extends CommandSupport<FailureCommandConfiguration>
{
    public FailureCommand(FailureCommandConfiguration config)
    {
        super(config);
    }

    public void execute(CommandContext commandContext)
    {
        commandContext.failure("failure command");
    }
}
