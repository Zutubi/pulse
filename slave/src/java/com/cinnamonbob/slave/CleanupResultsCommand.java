package com.cinnamonbob.slave;

import com.cinnamonbob.bootstrap.ConfigurationManager;
import com.cinnamonbob.core.util.FileSystemUtils;

import java.io.File;

/**
 */
public class CleanupResultsCommand implements Runnable
{
    private long recipeId;
    private ConfigurationManager configurationManager;

    public CleanupResultsCommand(long recipeId)
    {
        this.recipeId = recipeId;
    }

    public void run()
    {
        SlaveRecipePaths recipeProcessorPaths = new SlaveRecipePaths(recipeId, configurationManager);
        FileSystemUtils.removeDirectory(recipeProcessorPaths.getOutputDir());
        File workZip = recipeProcessorPaths.getOutputZip();
        workZip.delete();
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
