package com.zutubi.pulse.slave;

import com.zutubi.pulse.Version;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.config.ResourceConfiguration;
import com.zutubi.pulse.core.resources.ResourceDiscoverer;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.servercore.AgentRecipeDetails;
import com.zutubi.pulse.servercore.SystemInfo;
import com.zutubi.pulse.servercore.agent.PingStatus;
import com.zutubi.pulse.servercore.bootstrap.StartupManager;
import com.zutubi.pulse.servercore.filesystem.FileInfo;
import com.zutubi.pulse.servercore.services.*;
import com.zutubi.pulse.servercore.util.logging.CustomLogRecord;
import com.zutubi.pulse.servercore.util.logging.ServerMessagesHandler;
import com.zutubi.pulse.slave.command.CleanupRecipeCommand;
import com.zutubi.pulse.slave.command.RecipeCommand;
import com.zutubi.pulse.slave.command.UpdateCommand;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.net.MalformedURLException;
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
    private StartupManager startupManager;
    private SlaveRecipeProcessor slaveRecipeProcessor;
    private ServerMessagesHandler serverMessagesHandler;
    private MasterProxyFactory masterProxyFactory;
    private ObjectFactory objectFactory;

    private boolean firstStatus = true;

    //---( Status API )---

    public int ping()
    {
        return Version.getVersion().getBuildNumberAsInt();
    }

    public boolean updateVersion(String token, String build, String master, long handle, String packageUrl, long packageSize)
    {
        serviceTokenManager.validateToken(token);

        // Currently we always accept the request
        UpdateCommand command = new UpdateCommand(build, master, token, handle, packageUrl);
        SpringComponentContext.autowire(command);
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

    public SlaveStatus getStatus(String token, String master)
    {
        try
        {
            serviceTokenManager.validateToken(token);
        }
        catch (InvalidTokenException e)
        {
            // Respond as status
            return new SlaveStatus(PingStatus.TOKEN_MISMATCH);
        }

        // Pong the master (CIB-825)
        try
        {
            pongMaster(master);
        }
        catch(Exception e)
        {
            LOG.severe(e);
            return new SlaveStatus(PingStatus.INVALID_MASTER, "Unable to contact master at location '" + master + "': " + e.getMessage());
        }

        boolean first = false;
        if(firstStatus)
        {
            first = true;
            firstStatus = false;
        }

        long recipe = slaveRecipeProcessor.getBuildingRecipe();
        if (recipe != 0)
        {
            return new SlaveStatus(PingStatus.BUILDING, recipe, first);
        }
        else
        {
            return new SlaveStatus(PingStatus.IDLE, 0, first);
        }
    }

    private void pongMaster(String master) throws MalformedURLException
    {
        MasterService service = masterProxyFactory.createProxy(master);
        service.pong();
    }

    //---( Build API )---

    public boolean build(String token, String master, long handle, RecipeRequest request) throws InvalidTokenException
    {
        serviceTokenManager.validateToken(token);

        try
        {
            RecipeCommand command = objectFactory.buildBean(RecipeCommand.class, new Class[]{String.class, Long.TYPE, RecipeRequest.class }, new Object[] {master, handle, request });
            ErrorHandlingRunnable runnable = new ErrorHandlingRunnable(masterProxyFactory.createProxy(master), serviceTokenManager.getToken(), request.getId(), command);
            return slaveQueue.enqueueExclusive(runnable);
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void cleanupRecipe(String token, AgentRecipeDetails recipeDetails) throws InvalidTokenException
    {
        serviceTokenManager.validateToken(token);

        CleanupRecipeCommand command = new CleanupRecipeCommand(recipeDetails);
        SpringComponentContext.autowire(command);
        threadPool.execute(command);
    }

    public void terminateRecipe(String token, long recipeId) throws InvalidTokenException
    {
        serviceTokenManager.validateToken(token);

        // Do this request synchronously
        slaveRecipeProcessor.terminateRecipe(recipeId);
    }

    //---( Resource API )---

    public List<ResourceConfiguration> discoverResources(String token)
    {
        ResourceDiscoverer discoverer = new ResourceDiscoverer();
        return discoverer.discover();
    }

    public void garbageCollect()
    {
        Runtime.getRuntime().gc();
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
    public void setStartupManager(StartupManager startupManager)
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

    public void setMasterProxyFactory(MasterProxyFactory masterProxyFactory)
    {
        this.masterProxyFactory = masterProxyFactory;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
