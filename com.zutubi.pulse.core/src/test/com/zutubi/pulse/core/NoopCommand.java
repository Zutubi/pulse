package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.engine.api.ExecutionContext;

/**
 * <class comment/>
 */
public class NoopCommand extends CommandSupport
{
    private boolean executed;

    private boolean terminated;

    public void execute(ExecutionContext context, CommandResult result)
    {
        executed = true;

        if (terminated)
        {
            result.error("Command terminated");
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
