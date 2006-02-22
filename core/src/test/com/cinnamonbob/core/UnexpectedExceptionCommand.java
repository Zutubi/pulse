package com.cinnamonbob.core;

import com.cinnamonbob.core.model.CommandResult;

import java.io.File;

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

    public void execute(File baseDir, File outputDir, CommandResult result)
    {
        throw new RuntimeException("unexpected exception command");
    }
}
