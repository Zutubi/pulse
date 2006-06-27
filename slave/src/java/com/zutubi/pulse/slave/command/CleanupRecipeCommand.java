package com.zutubi.pulse.slave.command;

import com.zutubi.pulse.ServerRecipePaths;
import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.logging.Logger;

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
