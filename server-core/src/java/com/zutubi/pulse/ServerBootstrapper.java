package com.zutubi.pulse;

import com.zutubi.pulse.core.BootstrapperSupport;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.RecipePaths;
import com.zutubi.pulse.util.FileSystemUtils;

import java.io.IOException;

/**
 */
public class ServerBootstrapper extends BootstrapperSupport
{
    public void bootstrap(RecipePaths paths)
    {
        // ensure that the paths exist
        try
        {
            FileSystemUtils.createDirectory(paths.getOutputDir());
            FileSystemUtils.createDirectory(paths.getBaseDir());
        }
        catch (IOException e)
        {
            throw new BuildException(e);
        }
    }
}
