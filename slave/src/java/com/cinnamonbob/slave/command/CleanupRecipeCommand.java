package com.cinnamonbob.slave.command;

import com.cinnamonbob.ServerRecipePaths;
import com.cinnamonbob.bootstrap.ConfigurationManager;
import com.cinnamonbob.core.util.FileSystemUtils;
import com.cinnamonbob.util.logging.Logger;

import java.io.File;

/**
 */
public class CleanupRecipeCommand implements Runnable
{
    private static final Logger LOG = Logger.getLogger(CleanupRecipeCommand.class);

    private long recipeId;
    private ConfigurationManager configurationManager;

    public CleanupRecipeCommand(long recipeId)
    {
        this.recipeId = recipeId;
    }

    public void run()
    {
        ServerRecipePaths recipeProcessorPaths = new ServerRecipePaths(recipeId, configurationManager);
        File recipeRoot = recipeProcessorPaths.getRecipeRoot();
        if (!FileSystemUtils.removeDirectory(recipeRoot))
        {
            LOG.warning("Unable to remove recipe directory '" + recipeRoot + "'");
        }
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
