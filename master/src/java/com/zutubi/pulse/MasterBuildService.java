package com.zutubi.pulse;

import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.pulse.agent.MasterAgent;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.bootstrap.SystemConfiguration;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.model.ResourceManager;
import com.zutubi.pulse.prototype.config.admin.GeneralAdminConfiguration;
import com.zutubi.pulse.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;

/**
 */
public class MasterBuildService implements BuildService
{
    private MasterRecipeProcessor masterRecipeProcessor;
    private ConfigurationProvider configurationProvider;
    private MasterConfigurationManager configurationManager;
    private ResourceManager resourceManager;

    public MasterBuildService(MasterRecipeProcessor masterRecipeProcessor, ConfigurationProvider configurationProvider, MasterConfigurationManager configurationManager, ResourceManager resourceManager)
    {
        this.masterRecipeProcessor = masterRecipeProcessor;
        this.configurationProvider = configurationProvider;
        this.configurationManager = configurationManager;
        this.resourceManager = resourceManager;
    }

    public String getUrl()
    {
        GeneralAdminConfiguration generalConfig = configurationProvider.get(GeneralAdminConfiguration.class);
        SystemConfiguration systemConfig = configurationManager.getSystemConfig();
        return MasterAgent.constructMasterLocation(generalConfig, systemConfig);
    }

    public boolean hasResource(String resource, String version)
    {
        return resourceManager.getMasterRepository().hasResource(resource, version);
    }

    public boolean build(RecipeRequest request, BuildContext context)
    {
        masterRecipeProcessor.processRecipe(request, context);
        return true;
    }

    public long getBuildingRecipe()
    {
        return masterRecipeProcessor.getBuildingRecipe();
    }

    public void collectResults(String project, String spec, long recipeId, boolean incremental, File outputDest, File workDest)
    {
        ServerRecipePaths recipePaths = new ServerRecipePaths(project, spec, recipeId, configurationManager.getUserPaths().getData(), incremental);
        File outputDir = recipePaths.getOutputDir();

        if (!FileSystemUtils.rename(outputDir, outputDest, true))
        {
            throw new BuildException("Unable to rename output directory '" + outputDir.getAbsolutePath() + "' to '" + outputDest.getAbsolutePath() + "'");
        }

        if (workDest != null)
        {
            File workDir = recipePaths.getBaseDir();
            if(incremental)
            {
                try
                {
                    FileSystemUtils.copy(workDest, workDir);
                }
                catch(IOException e)
                {
                    throw new BuildException("Unable to snapshot work directory '" + workDir.getAbsolutePath() + "' to '" + workDest.getAbsolutePath() + "': " + e.getMessage());
                }
            }
            else
            {
                if (!FileSystemUtils.rename(workDir, workDest, true))
                {
                    throw new BuildException("Unable to rename work directory '" + workDir.getAbsolutePath() + "' to '" + workDest.getAbsolutePath() + "'");
                }
            }
        }
    }

    public void cleanup(String project, String spec, long recipeId, boolean incremental)
    {
        // We rename the output dir, so no need to remove it.
        ServerRecipePaths recipePaths = new ServerRecipePaths(project, spec, recipeId, configurationManager.getUserPaths().getData(), incremental);
        File recipeRoot = recipePaths.getRecipeRoot();

        if (!FileSystemUtils.rmdir(recipeRoot))
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
