package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;

import java.io.File;

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

    public void execute(long recipeId, RecipePaths paths, File outputDir, CommandResult result)
    {
        throw new BuildException("exception command");
    }
}
