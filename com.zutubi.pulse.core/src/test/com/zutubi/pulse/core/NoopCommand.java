package com.zutubi.pulse.core;

import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.commands.api.CommandSupport;

/**
 * A simple command that does no processing.
 */
public class NoopCommand extends CommandSupport
{
    private boolean executed;

    private boolean terminated;

    public NoopCommand(NoopCommandConfiguration config)
    {
        super(config);
    }

    public void execute(CommandContext commandContext)
    {
        executed = true;

        if (terminated)
        {
            commandContext.error("Command terminated");
        }
    }

    public boolean hasExecuted()
    {
        return executed;
    }

    public void terminate()
    {
        this.terminated = true;
    }

    public boolean isTerminated()
    {
        return terminated;
    }
}
