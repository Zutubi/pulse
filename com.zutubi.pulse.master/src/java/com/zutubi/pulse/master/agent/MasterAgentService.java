package com.zutubi.pulse.master.agent;

import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.ResourceRepository;
import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.master.MasterRecipeRunner;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.model.ResourceManager;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.servercore.AgentRecipeDetails;
import com.zutubi.pulse.servercore.ServerRecipePaths;
import com.zutubi.pulse.servercore.ServerRecipeService;
import com.zutubi.pulse.servercore.agent.SynchronisationMessage;
import com.zutubi.pulse.servercore.agent.SynchronisationMessageResult;
import com.zutubi.pulse.servercore.agent.SynchronisationTaskExecutor;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.bean.ObjectFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * A service to communicate with agents running within the master.
 */
public class MasterAgentService implements AgentService
{
    private AgentConfiguration agentConfig;

    private ServerRecipeService serverRecipeService;
    private MasterConfigurationManager configurationManager;
    private ObjectFactory objectFactory;
    private ResourceManager resourceManager;
    private SynchronisationTaskExecutor synchronisationTaskExecutor;

    public MasterAgentService(AgentConfiguration agentConfig)
    {
        this.agentConfig = agentConfig;
    }

    public AgentConfiguration getAgentConfig()
    {
        return agentConfig;
    }

    public boolean build(RecipeRequest request)
    {
        MasterRecipeRunner recipeRunner = objectFactory.buildBean(MasterRecipeRunner.class, new Class[]{ResourceRepository.class}, new Object[]{resourceManager.getAgentRepository(agentConfig)});
        serverRecipeService.processRecipe(agentConfig.getHandle(), request, recipeRunner);
        return true;
    }

    public void collectResults(AgentRecipeDetails recipeDetails, File outputDest, File workDest)
    {
        ServerRecipePaths recipePaths = new ServerRecipePaths(recipeDetails, configurationManager.getUserPaths().getData());
        File outputDir = recipePaths.getOutputDir();

        try
        {
            FileSystemUtils.rename(outputDir, outputDest, true);
        }
        catch (IOException e)
        {
            throw new BuildException("Renaming output directory: " + e.getMessage(), e);
        }

        if (workDest != null)
        {
            File workDir = recipePaths.getBaseDir();
            if (recipeDetails.isIncremental())
            {
                try
                {
                    FileSystemUtils.copy(workDest, workDir);
                }
                catch (IOException e)
                {
                    throw new BuildException("Unable to snapshot work directory '" + workDir.getAbsolutePath() + "' to '" + workDest.getAbsolutePath() + "': " + e.getMessage());
                }
            }
            else
            {
                try
                {
                    FileSystemUtils.rename(workDir, workDest, true);
                }
                catch (IOException e)
                {
                    throw new BuildException("Renaming work directory: " + e.getMessage(), e);
                }
            }
        }
    }

    public void cleanup(AgentRecipeDetails recipeDetails)
    {
        // We rename the output dir, so no need to remove it.
        ServerRecipePaths recipePaths = new ServerRecipePaths(recipeDetails, configurationManager.getUserPaths().getData());
        File recipeRoot = recipePaths.getRecipeRoot();

        if (!FileSystemUtils.rmdir(recipeRoot))
        {
            throw new BuildException("Unable to remove recipe directory '" + recipeRoot.getAbsolutePath() + "'");
        }
    }

    public void terminateRecipe(long recipeId)
    {
        serverRecipeService.terminateRecipe(agentConfig.getHandle(), recipeId);
    }

    public List<SynchronisationMessageResult> synchronise(List<SynchronisationMessage> messages)
    {
        return synchronisationTaskExecutor.synchronise(messages);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof MasterAgentService)
        {
            MasterAgentService other = (MasterAgentService) obj;
            return other.getAgentConfig().equals(agentConfig);
        }

        return false;
    }

    public void setServerRecipeService(ServerRecipeService serverRecipeService)
    {
        this.serverRecipeService = serverRecipeService;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setResourceManager(ResourceManager resourceManager)
    {
        this.resourceManager = resourceManager;
    }

    public void setSynchronisationTaskExecutor(SynchronisationTaskExecutor synchronisationTaskExecutor)
    {
        this.synchronisationTaskExecutor = synchronisationTaskExecutor;
    }
}
