package com.zutubi.pulse.core;

import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
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

    public void execute(ExecutionContext context, CommandResult result)
    {
        throw new BuildException("exception command");
    }
}
