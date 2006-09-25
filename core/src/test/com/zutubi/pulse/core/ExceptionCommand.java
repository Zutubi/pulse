package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;

/**
 * Simple testing command that always throws a BuildException.
 */
public class ExceptionCommand extends CommandSupport
{
    public ExceptionCommand()
    {
    }

    public ExceptionCommand(String name)
    {
        super(name);
    }

    public void execute(CommandContext context, CommandResult result)
    {
        throw new BuildException("exception command");
    }
}
