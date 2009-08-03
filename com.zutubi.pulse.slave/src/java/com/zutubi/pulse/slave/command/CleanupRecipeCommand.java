package com.zutubi.pulse.slave.command;

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

    private long projectHandle;
    private String project;
    private long recipeId;
    private boolean incremental;
    private String persistentPattern;
    private ConfigurationManager configurationManager;

    public CleanupRecipeCommand(long projectHandle, String project, long recipeId, boolean incremental, String persistentPattern)
    {
        this.projectHandle = projectHandle;
        this.project = project;
        this.recipeId = recipeId;
        this.incremental = incremental;
        this.persistentPattern = persistentPattern;
    }

    public void run()
    {
        ServerRecipePaths recipeProcessorPaths = new ServerRecipePaths(projectHandle, project, recipeId, configurationManager.getUserPaths().getData(), incremental, persistentPattern);
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
