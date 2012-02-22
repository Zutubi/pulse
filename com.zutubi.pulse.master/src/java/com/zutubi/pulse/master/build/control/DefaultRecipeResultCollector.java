package com.zutubi.pulse.master.build.control;

import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.master.MasterBuildPaths;
import com.zutubi.pulse.master.agent.AgentService;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
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

    public void collect(BuildResult result, long stageHandle, String stage, long recipeId, boolean incremental, boolean update, AgentService agentService)
    {
        if (agentService != null)
        {
            File outputDest = paths.getOutputDir(result, recipeId);
            agentService.collectResults(getRecipeDetails(agentService.getAgentConfig(), stageHandle, stage, recipeId, incremental, update), outputDest);
        }
    }

    public void cleanup(BuildResult result, long stageHandle, String stage, long recipeId, boolean incremental, boolean update, AgentService agentService)
    {
        if (agentService != null)
        {
            agentService.cleanup(getRecipeDetails(agentService.getAgentConfig(), stageHandle, stage, recipeId, incremental, update));
        }
    }

    private AgentRecipeDetails getRecipeDetails(AgentConfiguration agent, long stageHandle, String stage, long recipeId, boolean incremental, boolean update)
    {
        AgentRecipeDetails details = new AgentRecipeDetails();
        details.setAgentHandle(agent.getHandle());
        details.setAgent(agent.getName());
        details.setAgentDataPattern(agent.getDataDirectory());
        details.setProjectHandle(projectConfig.getHandle());
        details.setProject(projectConfig.getName());
        details.setProjectPersistentPattern(projectConfig.getBootstrap().getPersistentDirPattern());
        details.setProjectTempPattern(projectConfig.getBootstrap().getTempDirPattern());
        details.setStageHandle(stageHandle);
        details.setStage(stage);
        details.setRecipeId(recipeId);
        details.setIncremental(incremental);
        details.setUpdate(update);
        return details;
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
