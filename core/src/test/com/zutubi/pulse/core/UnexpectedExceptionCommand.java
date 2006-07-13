package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;

/**
 * Simple testing command that always throws a generic RuntimeException.
 */
public class UnexpectedExceptionCommand extends CommandSupport
{
    public UnexpectedExceptionCommand()
    {
    }

    public UnexpectedExceptionCommand(String name)
    {
        super(name);
    }

    public void execute(long recipeId, CommandContext context, CommandResult result)
    {
        throw new RuntimeException("unexpected exception command");
    }
}
