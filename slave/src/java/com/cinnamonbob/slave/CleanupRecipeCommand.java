package com.cinnamonbob.slave;

import com.cinnamonbob.bootstrap.ConfigurationManager;
import com.cinnamonbob.core.BuildException;
import com.cinnamonbob.core.util.FileSystemUtils;

import java.io.File;

/**
 */
public class CleanupRecipeCommand implements Runnable
{
    private long recipeId;
    private ConfigurationManager configurationManager;

    public CleanupRecipeCommand(long recipeId)
    {
        this.recipeId = recipeId;
    }

    public void run()
    {
        SlaveRecipePaths recipeProcessorPaths = new SlaveRecipePaths(recipeId, configurationManager);
        File recipeRoot = recipeProcessorPaths.getRecipeRoot();
        if (!FileSystemUtils.removeDirectory(recipeRoot))
        {
            throw new BuildException("Unable to remove recipe directory '" + recipeRoot + "'");
        }
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
