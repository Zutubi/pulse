package com.zutubi.pulse.slave.command;

import com.zutubi.pulse.servercore.AgentRecipeDetails;
import com.zutubi.pulse.servercore.ServerRecipePaths;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.io.IOException;

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
        try
        {
            FileSystemUtils.rmdir(recipeRoot);
        }
        catch (IOException e)
        {
            LOG.warning("Unable to remove recipe root directory: " + e.getMessage(), e);
        }

        try
        {
            FileSystemUtils.rmdir(recipeProcessorPaths.getTempDir());
        }
        catch (IOException e)
        {
            LOG.warning("Unable to remove recipe temp directory: " + e.getMessage(), e);
        }
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
