package com.zutubi.pulse.servercore;

import com.zutubi.pulse.core.Bootstrapper;
import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.engine.api.BuildException;

import java.io.File;

/**
 * A bootstrapper that merely makes sure the base directory exists.
 */
public class TrivialBootstrapper implements Bootstrapper
{
    public void bootstrap(CommandContext commandContext) throws BuildException
    {
        File baseDir = commandContext.getExecutionContext().getWorkingDir();
        if (!baseDir.exists() && !baseDir.mkdirs())
        {
            throw new BuildException("Unable to create base directory '" + baseDir.getAbsolutePath() + "'");
        }
    }

    public void terminate()
    {
    }
}
