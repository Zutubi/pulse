package com.zutubi.pulse;

import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.model.ResourceManager;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.logging.Logger;

import java.io.File;

/**
 * <class-comment/>
 */
public class MasterBuildService implements BuildService
{
    private static final Logger LOG = Logger.getLogger(MasterBuildService.class);

    private MasterRecipeProcessor masterRecipeProcessor;
    private MasterConfigurationManager configurationManager;
    private ResourceManager resourceManager;

    public MasterBuildService(MasterRecipeProcessor masterRecipeProcessor, MasterConfigurationManager configurationManager, ResourceManager resourceManager)
    {
        this.masterRecipeProcessor = masterRecipeProcessor;
        this.configurationManager = configurationManager;
        this.resourceManager = resourceManager;
    }

    public String getUrl()
    {
        return configurationManager.getAppConfig().getHostName();
    }

    public boolean hasResource(String resource, String version)
    {
        return resourceManager.getMasterRepository().hasResource(resource, version);
    }

    public boolean build(RecipeRequest request)
    {
        masterRecipeProcessor.processRecipe(request);
        return true;
    }

    public void collectResults(long recipeId, File outputDest, File workDest)
    {
        ServerRecipePaths recipePaths = new ServerRecipePaths(recipeId, configurationManager.getUserPaths().getData());
        File outputDir = recipePaths.getOutputDir();

        if (!FileSystemUtils.rename(outputDir, outputDest, true))
        {
            throw new BuildException("Unable to rename output directory '" + outputDir.getAbsolutePath() + "' to '" + outputDest.getAbsolutePath() + "'");
        }

        if (workDest != null)
        {
            File workDir = recipePaths.getBaseDir();
            if (!FileSystemUtils.rename(workDir, workDest, true))
            {
                throw new BuildException("Unable to rename work directory '" + workDir.getAbsolutePath() + "' to '" + workDest.getAbsolutePath() + "'");
            }
        }
    }

    public void cleanup(long recipeId)
    {
        // We rename the output dir, so no need to remove it.
        ServerRecipePaths recipePaths = new ServerRecipePaths(recipeId, configurationManager.getUserPaths().getData());
        File recipeRoot = recipePaths.getRecipeRoot();

        if (!FileSystemUtils.removeDirectory(recipeRoot))
        {
            throw new BuildException("Unable to remove recipe directory '" + recipeRoot.getAbsolutePath() + "'");
        }
    }

    public void terminateRecipe(long recipeId)
    {
        masterRecipeProcessor.terminateRecipe(recipeId);
    }

    public String getHostName()
    {
        return "master";
    }

    public void setMasterRecipeProcessor(MasterRecipeProcessor masterRecipeProcessor)
    {
        this.masterRecipeProcessor = masterRecipeProcessor;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof MasterBuildService;
    }

    public void setResourceManager(ResourceManager resourceManager)
    {
        this.resourceManager = resourceManager;
    }
}
