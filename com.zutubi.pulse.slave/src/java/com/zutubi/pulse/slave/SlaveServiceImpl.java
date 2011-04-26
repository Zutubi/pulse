package com.zutubi.pulse.slave;

import com.zutubi.pulse.Version;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.plugins.PluginManager;
import com.zutubi.pulse.core.plugins.ResourceLocatorExtensionManager;
import com.zutubi.pulse.core.plugins.repository.PluginInfo;
import com.zutubi.pulse.core.plugins.repository.PluginRepository;
import com.zutubi.pulse.core.plugins.repository.PluginScopePredicate;
import com.zutubi.pulse.core.plugins.sync.PluginSynchroniser;
import com.zutubi.pulse.core.plugins.sync.SynchronisationActions;
import com.zutubi.pulse.core.resources.ResourceDiscoverer;
import com.zutubi.pulse.core.resources.api.ResourceConfiguration;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.servercore.AgentRecipeDetails;
import com.zutubi.pulse.servercore.ServerInfoModel;
import com.zutubi.pulse.servercore.ServerRecipePaths;
import com.zutubi.pulse.servercore.ServerRecipeService;
import com.zutubi.pulse.servercore.agent.PingStatus;
import com.zutubi.pulse.servercore.agent.SynchronisationMessage;
import com.zutubi.pulse.servercore.agent.SynchronisationMessageResult;
import com.zutubi.pulse.servercore.agent.SynchronisationTaskRunnerService;
import com.zutubi.pulse.servercore.bootstrap.StartupManager;
import com.zutubi.pulse.servercore.filesystem.FileInfo;
import com.zutubi.pulse.servercore.filesystem.ToFileInfoMapping;
import com.zutubi.pulse.servercore.services.*;
import com.zutubi.pulse.servercore.util.logging.CustomLogRecord;
import com.zutubi.pulse.servercore.util.logging.ServerMessagesHandler;
import com.zutubi.pulse.slave.command.CleanupRecipeCommand;
import com.zutubi.pulse.slave.command.InstallPluginsCommand;
import com.zutubi.pulse.slave.command.SyncPluginsCommand;
import com.zutubi.pulse.slave.command.UpdateCommand;
import com.zutubi.util.CollectionUtils;
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
    private ServerRecipeService serverRecipeService;
    private SlaveThreadPool threadPool;
    private SlaveConfigurationManager configurationManager;
    private StartupManager startupManager;
    private ServerMessagesHandler serverMessagesHandler;
    private MasterProxyFactory masterProxyFactory;
    private ObjectFactory objectFactory;
    private SynchronisationTaskRunnerService synchronisationTaskRunnerService;
    private ForwardingEventListener forwardingEventListener;
    private PluginManager pluginManager;
    private PluginSynchroniser pluginSynchroniser;
    private ResourceLocatorExtensionManager resourceLocatorExtensionManager;

    private boolean firstStatus = true;

    //---( Status API )---

    public int ping()
    {
        return Version.getVersion().getBuildNumberAsInt();
    }

    public boolean updateVersion(String token, String build, String master, long hostId, String packageUrl, long packageSize)
    {
        serviceTokenManager.validateToken(token);

        // Currently we always accept the request
        UpdateCommand command = new UpdateCommand(build, master, token, hostId, packageUrl);
        SpringComponentContext.autowire(command);
        threadPool.execute(command);
        return true;
    }

    public boolean syncPlugins(String token, String master, long hostId, String pluginRepositoryUrl)
    {
        serviceTokenManager.validateToken(token);
        SyncPluginsCommand command = new SyncPluginsCommand(master, token, hostId, pluginRepositoryUrl);
        SpringComponentContext.autowire(command);
        threadPool.execute(command);
        return true;
    }

    public ServerInfoModel getSystemInfo(String token, boolean includeDetailed) throws InvalidTokenException
    {
        serviceTokenManager.validateToken(token);
        return ServerInfoModel.getServerInfo(configurationManager, startupManager, includeDetailed);
    }

    public List<CustomLogRecord> getRecentMessages(String token) throws InvalidTokenException
    {
        serviceTokenManager.validateToken(token);
        return serverMessagesHandler.takeSnapshot();
    }

    public HostStatus getStatus(String token, String master)
    {
        try
        {
            serviceTokenManager.validateToken(token);
        }
        catch (InvalidTokenException e)
        {
            // Respond as status
            return new HostStatus(PingStatus.TOKEN_MISMATCH);
        }

        // Pong the master (CIB-825)
        List<PluginInfo> masterPlugins;
        try
        {
            masterPlugins = pongMaster(master);
        }
        catch (Exception e)
        {
            LOG.severe(e);
            return new HostStatus(PingStatus.INVALID_MASTER, "Unable to contact master at location '" + master + "': " + e.getMessage());
        }
        
        if (pluginManager.getPlugins().isEmpty() || checkForPluginSync(master, masterPlugins))
        {
            return new HostStatus(PingStatus.PLUGIN_MISMATCH);
        }

        boolean first = false;
        if (firstStatus)
        {
            first = true;
            firstStatus = false;
        }

        return new HostStatus(serverRecipeService.getBuildingRecipes(), first);
    }

    private boolean checkForPluginSync(String masterUrl, List<PluginInfo> masterPlugins)
    {
        List<PluginInfo> serverPlugins = CollectionUtils.filter(masterPlugins, new PluginScopePredicate(PluginRepository.Scope.SERVER));
        SynchronisationActions requiredActions = pluginSynchroniser.determineRequiredActions(serverPlugins);
        if (requiredActions.isRebootRequired())
        {
            return true;
        }
        else if (requiredActions.isSyncRequired())
        {
            // Simple case: new plugins are available.  Just install them in
            // the background.
            InstallPluginsCommand command = new InstallPluginsCommand(masterUrl);
            SpringComponentContext.autowire(command);
            threadPool.execute(command);
        }
        
        return false;
    }

    public List<SynchronisationMessageResult> synchronise(String token, String master, long agentId, List<SynchronisationMessage> messages)
    {
        serviceTokenManager.validateToken(token);
        try
        {
            MasterService masterService = masterProxyFactory.createProxy(master);
            forwardingEventListener.setMaster(master, masterService);
            return synchronisationTaskRunnerService.synchronise(agentId, messages);
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    }

    private List<PluginInfo> pongMaster(String master) throws MalformedURLException
    {
        MasterService service = masterProxyFactory.createProxy(master);
        return service.pong();
    }

    //---( Build API )---

    public boolean build(String token, String master, long agentHandle, RecipeRequest request) throws InvalidTokenException
    {
        serviceTokenManager.validateToken(token);

        try
        {
            MasterService masterService = masterProxyFactory.createProxy(master);
            forwardingEventListener.setMaster(master, masterService);
            SlaveRecipeRunner delegateRunner = objectFactory.buildBean(SlaveRecipeRunner.class, new Class[]{String.class}, new Object[]{master});
            ErrorHandlingRecipeRunner recipeRunner = new ErrorHandlingRecipeRunner(masterService, serviceTokenManager.getToken(), request.getId(), delegateRunner);
            serverRecipeService.processRecipe(agentHandle, request, recipeRunner);
            return true;
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

    public void terminateRecipe(String token, long agentHandle, long recipeId) throws InvalidTokenException
    {
        serviceTokenManager.validateToken(token);

        // Do this request synchronously
        serverRecipeService.terminateRecipe(agentHandle, recipeId);
    }

    //---( Resource API )---

    public List<ResourceConfiguration> discoverResources(String token)
    {
        ResourceDiscoverer discoverer = resourceLocatorExtensionManager.createResourceDiscoverer();
        return discoverer.discover();
    }

    public void garbageCollect()
    {
        Runtime.getRuntime().gc();
    }

    public List<FileInfo> getFileInfos(String token, AgentRecipeDetails recipeDetails, String relativePath)
    {
        serviceTokenManager.validateToken(token);

        ServerRecipePaths recipePaths = new ServerRecipePaths(recipeDetails, configurationManager.getUserPaths().getData());

        File path = new File(recipePaths.getBaseDir(), relativePath);
        File[] listing = path.listFiles();
        if (listing != null)
        {
            return CollectionUtils.map(listing,  new ToFileInfoMapping());
        }

        return new LinkedList<FileInfo>();
    }

    public FileInfo getFileInfo(String token, AgentRecipeDetails recipeDetails, String relativePath)
    {
        serviceTokenManager.validateToken(token);

        ServerRecipePaths recipePaths = new ServerRecipePaths(recipeDetails, configurationManager.getUserPaths().getData());
        File base = recipePaths.getBaseDir();
        return new FileInfo(new File(base, relativePath));
    }

    public ServiceTokenManager getServiceTokenManager()
    {
        return serviceTokenManager;
    }

    //---( Required resources. )---

    public void setThreadPool(SlaveThreadPool threadPool)
    {
        this.threadPool = threadPool;
    }

    public void setConfigurationManager(SlaveConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setStartupManager(StartupManager startupManager)
    {
        this.startupManager = startupManager;
    }

    public void setServerMessagesHandler(ServerMessagesHandler serverMessagesHandler)
    {
        this.serverMessagesHandler = serverMessagesHandler;
    }

    public void setServiceTokenManager(ServiceTokenManager serviceTokenManager)
    {
        this.serviceTokenManager = serviceTokenManager;
    }

    public void setMasterProxyFactory(MasterProxyFactory masterProxyFactory)
    {
        this.masterProxyFactory = masterProxyFactory;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setServerRecipeService(ServerRecipeService serverRecipeService)
    {
        this.serverRecipeService = serverRecipeService;
    }

    public void setSynchronisationTaskRunnerService(SynchronisationTaskRunnerService synchronisationTaskRunnerService)
    {
        this.synchronisationTaskRunnerService = synchronisationTaskRunnerService;
    }

    public void setForwardingEventListener(ForwardingEventListener forwardingEventListener)
    {
        this.forwardingEventListener = forwardingEventListener;
    }

    public void setPluginManager(PluginManager pluginManager)
    {
        this.pluginManager = pluginManager;
    }

    public void setPluginSynchroniser(PluginSynchroniser pluginSynchroniser)
    {
        this.pluginSynchroniser = pluginSynchroniser;
    }

    public void setResourceLocatorExtensionManager(ResourceLocatorExtensionManager resourceLocatorExtensionManager)
    {
        this.resourceLocatorExtensionManager = resourceLocatorExtensionManager;
    }
}
