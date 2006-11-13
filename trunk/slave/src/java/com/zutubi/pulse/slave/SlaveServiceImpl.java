package com.zutubi.pulse.slave;

import com.zutubi.pulse.BuildContext;
import com.zutubi.pulse.SystemInfo;
import com.zutubi.pulse.Version;
import com.zutubi.pulse.agent.Status;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.filesystem.FileInfo;
import com.zutubi.pulse.logging.CustomLogRecord;
import com.zutubi.pulse.logging.ServerMessagesHandler;
import com.zutubi.pulse.resources.ResourceDiscoverer;
import com.zutubi.pulse.resources.ResourceConstructor;
import com.zutubi.pulse.services.InvalidTokenException;
import com.zutubi.pulse.services.ServiceTokenManager;
import com.zutubi.pulse.services.SlaveService;
import com.zutubi.pulse.services.SlaveStatus;
import com.zutubi.pulse.slave.command.CleanupRecipeCommand;
import com.zutubi.pulse.slave.command.RecipeCommand;
import com.zutubi.pulse.slave.command.UpdateCommand;
import com.zutubi.pulse.util.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
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

    //---( Status API )---

    public int ping()
    {
        return Version.getVersion().getBuildNumberAsInt();
    }

    public boolean updateVersion(String token, String build, String master, long id, String packageUrl, long packageSize)
    {
        serviceTokenManager.validateToken(token);

        // Currently we always accept the request
        UpdateCommand command = new UpdateCommand(build, master, token, id, packageUrl, packageSize);
        ComponentContext.autowire(command);
        threadPool.execute(command);
        return true;
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

    public SlaveStatus getStatus(String token)
    {
        try
        {
            serviceTokenManager.validateToken(token);
        }
        catch (InvalidTokenException e)
        {
            // Respond as status
            return new SlaveStatus(Status.TOKEN_MISMATCH);
        }

        // Synchronous request
        long recipe = slaveRecipeProcessor.getBuildingRecipe();
        if (recipe != 0)
        {
            return new SlaveStatus(Status.BUILDING, recipe);
        }
        else
        {
            return new SlaveStatus(Status.IDLE, 0);
        }
    }

    //---( Build API )---

    public boolean build(String token, String master, long slaveId, RecipeRequest request, BuildContext context) throws InvalidTokenException
    {
        serviceTokenManager.validateToken(token);

        RecipeCommand command = new RecipeCommand(master, slaveId, request, context);
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

    //---( Resource API )---

    public List<Resource> discoverResources(String token)
    {
        ResourceDiscoverer discoverer = new ResourceDiscoverer();
        return discoverer.discover();
    }

    public Resource createResource(ResourceConstructor constructor, String path)
    {
        try
        {
            return constructor.createResource(path);
        }
        catch (IOException e)
        {
            return null;
        }
    }

    public boolean isResourceHome(ResourceConstructor constructor, String path)
    {
        return constructor.isResourceHome(path);
    }

    //---( Remote File API )---

    public FileInfo getFileInfo(String token, String path)
    {
        serviceTokenManager.validateToken(token);

        return new FileInfo(new File(path));
    }

    public String[] listRoots(String token)
    {
        serviceTokenManager.validateToken(token);

        List<String> roots = new LinkedList<String>();
        for (File root : File.listRoots())
        {
            roots.add(root.getAbsolutePath());
        }
        return roots.toArray(new String[roots.size()]);
    }


    public ServiceTokenManager getServiceTokenManager()
    {
        return serviceTokenManager;
    }

    //---( Required resources. )---

    /**
     * Required resource.
     *  
     * @param threadPool instance
     */
    public void setThreadPool(SlaveThreadPool threadPool)
    {
        this.threadPool = threadPool;
    }

    /**
     * Required resource
     *
     * @param slaveRecipeProcessor instance
     */
    public void setSlaveRecipeProcessor(SlaveRecipeProcessor slaveRecipeProcessor)
    {
        this.slaveRecipeProcessor = slaveRecipeProcessor;
    }

    /**
     * Required resource
     *
     * @param configurationManager instance
     */
    public void setConfigurationManager(SlaveConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    /**
     * Required resource.
     *
     * @param startupManager instance
     */
    public void setStartupManager(SlaveStartupManager startupManager)
    {
        this.startupManager = startupManager;
    }

    /**
     * Required resource.
     *
     * @param serverMessagesHandler instance
     */
    public void setServerMessagesHandler(ServerMessagesHandler serverMessagesHandler)
    {
        this.serverMessagesHandler = serverMessagesHandler;
    }

    /**
     * Required resource.
     *
     * @param serviceTokenManager instance
     */
    public void setServiceTokenManager(ServiceTokenManager serviceTokenManager)
    {
        this.serviceTokenManager = serviceTokenManager;
    }

    /**
     * Required resource.
     *
     * @param slaveQueue instance
     */
    public void setSlaveQueue(SlaveQueue slaveQueue)
    {
        this.slaveQueue = slaveQueue;
    }
}
