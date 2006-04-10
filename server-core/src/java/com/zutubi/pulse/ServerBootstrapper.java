package com.cinnamonbob;

import com.cinnamonbob.core.BootstrapperSupport;
import com.cinnamonbob.core.BuildException;
import com.cinnamonbob.core.RecipePaths;
import com.cinnamonbob.core.util.FileSystemUtils;

import java.io.IOException;

/**
 */
public class ServerBootstrapper extends BootstrapperSupport
{
    public void bootstrap(long recipeId, RecipePaths paths)
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
