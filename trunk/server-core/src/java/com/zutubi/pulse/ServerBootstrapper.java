package com.zutubi.pulse;

import com.zutubi.pulse.core.BootstrapperSupport;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.CommandContext;
import com.zutubi.pulse.util.FileSystemUtils;

import java.io.IOException;

/**
 */
public class ServerBootstrapper extends BootstrapperSupport
{
    public void bootstrap(CommandContext context)
    {
        // ensure that the paths exist
        try
        {
            FileSystemUtils.createDirectory(context.getPaths().getOutputDir());
            FileSystemUtils.createDirectory(context.getPaths().getBaseDir());
        }
        catch (IOException e)
        {
            throw new BuildException(e);
        }
    }
}
