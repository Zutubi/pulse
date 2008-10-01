package com.zutubi.pulse;

import com.zutubi.pulse.agent.MasterLocationProvider;
import com.zutubi.pulse.agent.PingStatus;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.bootstrap.StartupManager;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.core.config.ResourceRequirement;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.core.util.FileSystemUtils;
import com.zutubi.pulse.logging.CustomLogRecord;
import com.zutubi.pulse.logging.ServerMessagesHandler;
import com.zutubi.pulse.model.ResourceManager;
import com.zutubi.pulse.resources.ResourceDiscoverer;
import com.zutubi.pulse.services.SlaveStatus;
import com.zutubi.pulse.tove.config.agent.AgentConfiguration;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 */
public class MasterAgentService implements AgentService
{
    private static final Logger LOG = Logger.getLogger(MasterAgentService.class);

    private AgentConfiguration agentConfig;

    private MasterRecipeProcessor masterRecipeProcessor;
    private MasterConfigurationManager configurationManager;
    private ResourceManager resourceManager;
    private StartupManager startupManager;
    private ServerMessagesHandler serverMessagesHandler;
    private MasterLocationProvider masterLocationProvider;

    public MasterAgentService(AgentConfiguration agentConfig)
    {
        this.agentConfig = agentConfig;
    }

    public String getUrl()
    {
        return masterLocationProvider.getMasterUrl();
    }

    public int ping()
    {
        return Version.getVersion().getBuildNumberAsInt();
    }

    public SlaveStatus getStatus(String masterLocation)
    {
        long recipeId = getMasterRecipeProcessor().getBuildingRecipe();
        if (recipeId == 0)
        {
            return new SlaveStatus(PingStatus.IDLE);
        }
        else
        {
            return new SlaveStatus(PingStatus.BUILDING, recipeId, false);
        }
    }

    public boolean updateVersion(String masterBuild, String masterUrl, long handle, String packageUrl, long packageSize)
    {
        LOG.warning("Illegal request to update version of master agent.");
        return true;
    }

    public List<Resource> discoverResources()
    {
        ResourceDiscoverer discoverer = new ResourceDiscoverer();
        return discoverer.discover();
    }

    public SystemInfo getSystemInfo()
    {
        return SystemInfo.getSystemInfo(configurationManager, startupManager);
    }

    public List<CustomLogRecord> getRecentMessages()
    {
        return serverMessagesHandler.takeSnapshot();
    }

    public AgentConfiguration getAgentConfig()
    {
        return agentConfig;
    }

    public boolean hasResource(ResourceRequirement requirement)
    {
        return getResourceManager().getAgentRepository(agentConfig).hasResource(requirement);
    }

    public boolean build(RecipeRequest request)
    {
        getMasterRecipeProcessor().processRecipe(request, getResourceManager().getAgentRepository(agentConfig));
        return true;
    }

    public long getBuildingRecipe()
    {
        return getMasterRecipeProcessor().getBuildingRecipe();
    }

    public void collectResults(String project, long recipeId, boolean incremental, File outputDest, File workDest)
    {
        ServerRecipePaths recipePaths = new ServerRecipePaths(project, recipeId, configurationManager.getUserPaths().getData(), incremental);
        File outputDir = recipePaths.getOutputDir();

        if (!FileSystemUtils.rename(outputDir, outputDest, true))
        {
            throw new BuildException("Unable to rename output directory '" + outputDir.getAbsolutePath() + "' to '" + outputDest.getAbsolutePath() + "'");
        }

        if (workDest != null)
        {
            File workDir = recipePaths.getBaseDir();
            if (incremental)
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
                if (!FileSystemUtils.rename(workDir, workDest, true))
                {
                    throw new BuildException("Unable to rename work directory '" + workDir.getAbsolutePath() + "' to '" + workDest.getAbsolutePath() + "'");
                }
            }
        }
    }

    public void cleanup(String project, long recipeId, boolean incremental)
    {
        // We rename the output dir, so no need to remove it.
        ServerRecipePaths recipePaths = new ServerRecipePaths(project, recipeId, configurationManager.getUserPaths().getData(), incremental);
        File recipeRoot = recipePaths.getRecipeRoot();

        if (!FileSystemUtils.rmdir(recipeRoot))
        {
            throw new BuildException("Unable to remove recipe directory '" + recipeRoot.getAbsolutePath() + "'");
        }
    }

    public void terminateRecipe(long recipeId)
    {
        getMasterRecipeProcessor().terminateRecipe(recipeId);
    }

    public String getHostName()
    {
        return "master";
    }

    public void garbageCollect()
    {
        Runtime.getRuntime().gc();
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

    private MasterRecipeProcessor getMasterRecipeProcessor()
    {
        if (masterRecipeProcessor == null)
        {
            masterRecipeProcessor = SpringComponentContext.getBean("masterRecipeProcessor");
        }
        return masterRecipeProcessor;
    }

    public void setMasterRecipeProcessor(MasterRecipeProcessor masterRecipeProcessor)
    {
        this.masterRecipeProcessor = masterRecipeProcessor;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public ResourceManager getResourceManager()
    {
        if (resourceManager == null)
        {
            resourceManager = SpringComponentContext.getBean("resourceManager");
        }
        return resourceManager;
    }
    
    public void setResourceManager(ResourceManager resourceManager)
    {
        this.resourceManager = resourceManager;
    }

    public void setStartupManager(StartupManager startupManager)
    {
        this.startupManager = startupManager;
    }

    public void setServerMessagesHandler(ServerMessagesHandler serverMessagesHandler)
    {
        this.serverMessagesHandler = serverMessagesHandler;
    }

    public void setMasterLocationProvider(MasterLocationProvider masterLocationProvider)
    {
        this.masterLocationProvider = masterLocationProvider;
    }
}
