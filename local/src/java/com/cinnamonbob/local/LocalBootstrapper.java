package com.cinnamonbob.local;

import com.cinnamonbob.core.BootstrapperSupport;
import com.cinnamonbob.core.BuildException;
import com.cinnamonbob.core.RecipePaths;
import com.cinnamonbob.core.util.FileSystemUtils;

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
