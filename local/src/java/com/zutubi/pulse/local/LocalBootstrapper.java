package com.zutubi.pulse.local;

import com.zutubi.pulse.core.BootstrapperSupport;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.CommandContext;
import com.zutubi.pulse.util.FileSystemUtils;

import java.io.IOException;

/**
 */
public class LocalBootstrapper extends BootstrapperSupport
{
    public void bootstrap(CommandContext context) throws BuildException
    {
        try
        {
            FileSystemUtils.cleanOutputDir(context.getPaths().getOutputDir());
        }
        catch (IOException e)
        {
            throw new BuildException(e);
        }
    }
}
