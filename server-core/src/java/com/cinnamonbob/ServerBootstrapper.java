package com.cinnamonbob;

import com.cinnamonbob.core.Bootstrapper;
import com.cinnamonbob.core.BuildException;
import com.cinnamonbob.core.RecipePaths;
import com.cinnamonbob.core.util.FileSystemUtils;

import java.io.IOException;

/**
 */
public class ServerBootstrapper implements Bootstrapper
{
    public void bootstrap(RecipePaths paths)
    {
        // ensure that the paths exist
        try
        {
            FileSystemUtils.createDirectory(paths.getOutputDir());
            FileSystemUtils.createDirectory(paths.getWorkDir());
        }
        catch (IOException e)
        {
            throw new BuildException(e);
        }
    }
}
