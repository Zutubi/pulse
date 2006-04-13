/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;

import java.io.File;

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

    public void execute(File baseDir, File outputDir, CommandResult result)
    {
        result.failure("failure command");
    }
}
