package com.zutubi.pulse.master.build.control;

import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.master.agent.AgentService;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.MasterBuildPaths;
import com.zutubi.pulse.servercore.AgentRecipeDetails;

import java.io.File;

/**
 */
public class DefaultRecipeResultCollector implements RecipeResultCollector
{
    private MasterBuildPaths paths;
    private ProjectConfiguration projectConfig;

    public DefaultRecipeResultCollector(MasterConfigurationManager configManager)
    {
        this.paths = new MasterBuildPaths(configManager);
    }

    public void prepare(BuildResult result, long recipeId)
    {
        // ensure that we have created the necessary directories.
        File recipeDir = paths.getRecipeDir(result, recipeId);
        if (!recipeDir.mkdirs())
        {
            throw new BuildException("Failed to create the '" + recipeDir + "' directory.");
        }
    }

    public void collect(BuildResult result, long recipeId, boolean collectWorkingCopy, boolean incremental, AgentService agentService)
    {
        if (agentService != null)
        {
            File outputDest = paths.getOutputDir(result, recipeId);
            File workDest = null;
            if (collectWorkingCopy)
            {
                workDest = paths.getBaseDir(result, recipeId);
            }

            agentService.collectResults(getRecipeDetails(agentService.getAgentConfig(), recipeId, incremental), outputDest, workDest);
        }
    }

    public void cleanup(BuildResult result, long recipeId, boolean incremental, AgentService agentService)
    {
        if (agentService != null)
        {
            agentService.cleanup(getRecipeDetails(agentService.getAgentConfig(), recipeId, incremental));
        }
    }

    private AgentRecipeDetails getRecipeDetails(AgentConfiguration agent, long recipeId, boolean incremental)
    {
        return new AgentRecipeDetails(agent.getHandle(), agent.getName(), agent.getDataDirectory(), projectConfig.getHandle(), projectConfig.getName(), recipeId, incremental, projectConfig.getOptions().getPersistentWorkDir());
    }

    public File getRecipeDir(BuildResult result, long recipeId)
    {
        return paths.getRecipeDir(result, recipeId);
    }

    public void setProjectConfig(ProjectConfiguration projectConfig)
    {
        this.projectConfig = projectConfig;
    }
}
