package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.engine.api.ExecutionContext;

/**
 * Simple testing command that always fails.
 */
public class FailureCommand extends CommandSupport
{
    public FailureCommand()
    {

    }

    public FailureCommand(String name)
    {
        super(name);
    }

    public void execute(ExecutionContext context, CommandResult result)
    {
        result.failure("failure command");
    }
}
