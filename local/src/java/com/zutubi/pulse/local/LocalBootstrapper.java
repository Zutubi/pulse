package com.zutubi.pulse.local;

import com.zutubi.pulse.core.BootstrapperSupport;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.RecipePaths;
import com.zutubi.pulse.core.util.FileSystemUtils;

import java.io.IOException;

/**
 */
public class LocalBootstrapper extends BootstrapperSupport
{
    public void bootstrap(long recipeId, RecipePaths paths) throws BuildException
    {
        try
        {
            FileSystemUtils.cleanOutputDir(paths.getOutputDir());
        }
        catch (IOException e)
        {
            throw new BuildException(e);
        }
    }
}
