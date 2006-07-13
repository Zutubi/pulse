package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;

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

    public void execute(long recipeId, CommandContext context, CommandResult result)
    {
        result.failure("failure command");
    }
}
