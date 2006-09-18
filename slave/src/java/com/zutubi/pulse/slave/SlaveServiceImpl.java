package com.zutubi.pulse.slave;

import com.zutubi.pulse.ResourceDiscoverer;
import com.zutubi.pulse.SystemInfo;
import com.zutubi.pulse.Version;
import com.zutubi.pulse.agent.Status;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.logging.CustomLogRecord;
import com.zutubi.pulse.logging.ServerMessagesHandler;
import com.zutubi.pulse.services.InvalidTokenException;
import com.zutubi.pulse.services.ServiceTokenManager;
import com.zutubi.pulse.services.SlaveService;
import com.zutubi.pulse.services.SlaveStatus;
import com.zutubi.pulse.slave.command.CleanupRecipeCommand;
import com.zutubi.pulse.slave.command.RecipeCommand;
import com.zutubi.pulse.slave.command.UpdateCommand;
import com.zutubi.pulse.util.logging.Logger;

import java.util.List;

/**
 */
public class SlaveServiceImpl implements SlaveService
{
    private static final Logger LOG = Logger.getLogger(SlaveServiceImpl.class);

    private ServiceTokenManager serviceTokenManager;
    private SlaveQueue slaveQueue;
    private SlaveThreadPool threadPool;
    private SlaveConfigurationManager configurationManager;
    private SlaveStartupManager startupManager;
    private SlaveRecipeProcessor slaveRecipeProcessor;
    private ServerMessagesHandler serverMessagesHandler;

    public int ping()
    {
        return Version.getVersion().getBuildNumberAsInt();
    }

    public boolean updateVersion(String token, String build, String master, long id, String packageUrl, long packageSize)
    {
        serviceTokenManager.validateToken(token);

        // Currently we always accept the request!  Should check we can write to the
        // home dirs first I suppose...
        UpdateCommand command = new UpdateCommand(build, master, token, id, packageUrl, packageSize);
        ComponentContext.autowire(command);
        threadPool.execute(command);
        return true;
    }

    public SlaveStatus getStatus(String token)
    {
        try
        {
            serviceTokenManager.validateToken(token);
        }
        catch(InvalidTokenException e)
        {
            // Respond as status
            return new SlaveStatus(Status.TOKEN_MISMATCH);
        }

        // Synchronous request
        long recipe = slaveRecipeProcessor.getBuildingRecipe();
        if(recipe != 0)
        {
            return new SlaveStatus(Status.BUILDING, recipe);
        }
        else
        {
            return new SlaveStatus(Status.IDLE, 0);
        }
    }

    public boolean build(String token, String master, long slaveId, RecipeRequest request) throws InvalidTokenException
    {
        serviceTokenManager.validateToken(token);

        RecipeCommand command = new RecipeCommand(master, slaveId, request);
        ComponentContext.autowire(command);
        ErrorHandlingRunnable runnable = new ErrorHandlingRunnable(master, serviceTokenManager, request.getId(), command);
        ComponentContext.autowire(runnable);

        return slaveQueue.enqueueExclusive(runnable);
    }

    public void cleanupRecipe(String token, String project, String spec, long recipeId, boolean incremental) throws InvalidTokenException
    {
        serviceTokenManager.validateToken(token);

        CleanupRecipeCommand command = new CleanupRecipeCommand(project, spec, recipeId, incremental);
        ComponentContext.autowire(command);
        threadPool.execute(command);
    }

    public void terminateRecipe(String token, long recipeId) throws InvalidTokenException
    {
        serviceTokenManager.validateToken(token);

        // Do this request synchronously
        slaveRecipeProcessor.terminateRecipe(recipeId);
    }

    public SystemInfo getSystemInfo(String token) throws InvalidTokenException
    {
        serviceTokenManager.validateToken(token);
        return SystemInfo.getSystemInfo(configurationManager, startupManager);
    }

    public List<CustomLogRecord> getRecentMessages(String token) throws InvalidTokenException
    {
        serviceTokenManager.validateToken(token);
        return serverMessagesHandler.takeSnapshot();
    }

    public List<Resource> discoverResources(String token)
    {
        ResourceDiscoverer discoverer = new ResourceDiscoverer();
        return discoverer.discover();
    }

    public void setThreadPool(SlaveThreadPool threadPool)
    {
        this.threadPool = threadPool;
    }

    public void setSlaveRecipeProcessor(SlaveRecipeProcessor slaveRecipeProcessor)
    {
        this.slaveRecipeProcessor = slaveRecipeProcessor;
    }

    public void setConfigurationManager(SlaveConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setStartupManager(SlaveStartupManager startupManager)
    {
        this.startupManager = startupManager;
    }

    public void setServerMessagesHandler(ServerMessagesHandler serverMessagesHandler)
    {
        this.serverMessagesHandler = serverMessagesHandler;
    }

    public ServiceTokenManager getServiceTokenManager()
    {
        return serviceTokenManager;
    }

    public void setServiceTokenManager(ServiceTokenManager serviceTokenManager)
    {
        this.serviceTokenManager = serviceTokenManager;
    }

    public void setSlaveQueue(SlaveQueue slaveQueue)
    {
        this.slaveQueue = slaveQueue;
    }
}
