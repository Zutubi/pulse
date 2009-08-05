package com.zutubi.pulse.slave.command;

import com.zutubi.pulse.servercore.AgentRecipeDetails;
import com.zutubi.pulse.servercore.ServerRecipePaths;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;

/**
 */
public class CleanupRecipeCommand implements Runnable
{
    private static final Logger LOG = Logger.getLogger(CleanupRecipeCommand.class);

    private AgentRecipeDetails recipeDetails;
    private ConfigurationManager configurationManager;

    public CleanupRecipeCommand(AgentRecipeDetails recipeDetails)
    {
        this.recipeDetails = recipeDetails;
    }

    public void run()
    {
        ServerRecipePaths recipeProcessorPaths = new ServerRecipePaths(recipeDetails, configurationManager.getUserPaths().getData());
        File recipeRoot = recipeProcessorPaths.getRecipeRoot();
        if (!FileSystemUtils.rmdir(recipeRoot))
        {
            LOG.warning("Unable to remove recipe directory '" + recipeRoot + "'");
        }
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
