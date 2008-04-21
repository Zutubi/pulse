package com.zutubi.pulse.agent;

import com.zutubi.prototype.config.*;
import com.zutubi.prototype.security.AccessManager;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.pulse.*;
import com.zutubi.pulse.bootstrap.DefaultSetupManager;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.bootstrap.StartupManager;
import com.zutubi.pulse.core.Stoppable;
import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.events.*;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.license.LicenseManager;
import com.zutubi.pulse.license.authorisation.AddAgentAuthorisation;
import com.zutubi.pulse.logging.ServerMessagesHandler;
import com.zutubi.pulse.model.AgentState;
import com.zutubi.pulse.model.AgentStateManager;
import com.zutubi.pulse.model.ResourceManager;
import com.zutubi.pulse.model.UserManager;
import com.zutubi.pulse.prototype.config.admin.GlobalConfiguration;
import com.zutubi.pulse.prototype.config.agent.AgentAclConfiguration;
import com.zutubi.pulse.prototype.config.agent.AgentConfiguration;
import com.zutubi.pulse.prototype.config.group.AbstractGroupConfiguration;
import com.zutubi.pulse.services.ServiceTokenManager;
import com.zutubi.pulse.services.SlaveStatus;
import com.zutubi.pulse.services.UpgradeStatus;
import com.zutubi.util.Sort;
import com.zutubi.util.logging.Logger;

import java.net.ConnectException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 */
public class DefaultAgentManager implements AgentManager, ExternalStateManager<AgentConfiguration>, EventListener, Stoppable
{
    private static final Logger LOG = Logger.getLogger(DefaultAgentManager.class);

    // ping timeout in seconds.
    private static final int PING_TIMEOUT = Integer.getInteger("pulse.agent.ping.timeout", 45);
    private static final int DEFAULT_AGENT_PORT = 8090;

    private final int masterBuildNumber = Version.getVersion().getBuildNumberAsInt();

    private ReentrantLock lock = new ReentrantLock();
    private Map<Long, Agent> agents;

    private AgentStateManager agentStateManager;
    private MasterConfigurationManager configurationManager;
    private ConfigurationProvider configurationProvider;
    private ConfigurationTemplateManager configurationTemplateManager;
    private TypeRegistry typeRegistry;
    private ResourceManager resourceManager;
    private EventManager eventManager;
    private SlaveProxyFactory slaveProxyFactory;
    private ServiceTokenManager serviceTokenManager;
    private MasterRecipeProcessor masterRecipeProcessor;
    private ServerMessagesHandler serverMessagesHandler;
    private StartupManager startupManager;
    private ThreadFactory threadFactory;

    private LicenseManager licenseManager;
    /**
     * This pool is used to wait for the result of an agent ping, which can
     * take up to the ping timeout to execute and so should not be done
     * inline as they can hold up the calling thread.
     */
    private ExecutorService pingPool;
    /**
     * This pool is used to actually make the network calls to ping an agent.
     * It is separate from the above pool as the network calls can take even
     * longer to timeout, so these threads may dangle for a while on the
     * network calls.
     */
    private ExecutorService pingerThreadPool;

    private Map<Long, AgentUpdater> updaters = new TreeMap<Long, AgentUpdater>();
    private ReentrantLock updatersLock = new ReentrantLock();

    public void init()
    {
        pingPool = Executors.newCachedThreadPool(threadFactory);
        pingerThreadPool = Executors.newCachedThreadPool(threadFactory);

        TypeListener<AgentConfiguration> listener = new TypeAdapter<AgentConfiguration>(AgentConfiguration.class)
        {
            public void postInsert(AgentConfiguration instance)
            {
                agentAdded(instance);
            }

            public void postDelete(AgentConfiguration instance)
            {
                agentDeleted(instance);
            }

            public void postSave(AgentConfiguration instance, boolean nested)
            {
                agentChanged(instance);
            }
        };
        listener.register(configurationProvider, true);

        refreshAgents();

        // register the canAddAgent license authorisation.
        AddAgentAuthorisation addAgentAuthorisation = new AddAgentAuthorisation();
        addAgentAuthorisation.setAgentManager(this);
        licenseManager.addAuthorisation(addAgentAuthorisation);

        // ensure that we create the default master agent.
        ensureDefaultAgentsDefined();
    }

    private void ensureDefaultAgentsDefined()
    {
        if (DefaultSetupManager.initialInstallation)
        {
            try
            {
                AgentConfiguration globalAgent = new AgentConfiguration();
                globalAgent.setName(GLOBAL_AGENT_NAME);
                globalAgent.setRemote(true);
                globalAgent.setPort(DEFAULT_AGENT_PORT);
                globalAgent.setPermanent(true);
                
                // All users can view all agents by default.
                AbstractGroupConfiguration group = configurationProvider.get(PathUtils.getPath(ConfigurationRegistry.GROUPS_SCOPE, UserManager.ALL_USERS_GROUP_NAME), AbstractGroupConfiguration.class);
                globalAgent.addPermission(new AgentAclConfiguration(group, AccessManager.ACTION_VIEW));

                // Anonymous users can view all agents by default (but only
                // when anonymous access is explicitly enabled).
                group = configurationProvider.get(PathUtils.getPath(ConfigurationRegistry.GROUPS_SCOPE, UserManager.ANONYMOUS_USERS_GROUP_NAME), AbstractGroupConfiguration.class);
                globalAgent.addPermission(new AgentAclConfiguration(group, AccessManager.ACTION_VIEW));

                CompositeType agentType = typeRegistry.getType(AgentConfiguration.class);
                MutableRecord globalTemplate = agentType.unstantiate(globalAgent);
                configurationTemplateManager.markAsTemplate(globalTemplate);
                configurationTemplateManager.insertRecord(ConfigurationRegistry.AGENTS_SCOPE, globalTemplate);

                // reload the template so that we have the handle.
                Record persistedGlobalTemplate = configurationTemplateManager.getRecord(PathUtils.getPath(ConfigurationRegistry.AGENTS_SCOPE, GLOBAL_AGENT_NAME));

                AgentConfiguration masterAgent = new AgentConfiguration();
                masterAgent.setName(MASTER_AGENT_NAME);
                masterAgent.setRemote(false);

                MutableRecord masterAgentRecord = agentType.unstantiate(masterAgent);
                configurationTemplateManager.setParentTemplate(masterAgentRecord, persistedGlobalTemplate.getHandle());
                configurationTemplateManager.insertRecord(ConfigurationRegistry.AGENTS_SCOPE, masterAgentRecord);
            }
            catch (TypeException e)
            {
                LOG.severe("Unable to create default agents: " + e.getMessage(), e);
            }
        }
    }

    public long createState(AgentConfiguration instance)
    {
        AgentState state = new AgentState();
        agentStateManager.save(state);
        return state.getId();
    }

    public void rollbackState(long id)
    {
        agentStateManager.delete(id);
    }

    private void refreshAgents()
    {
        lock.lock();
        try
        {
            agents = new TreeMap<Long, Agent>();
            for (AgentConfiguration agentConfig : configurationProvider.getAll(AgentConfiguration.class))
            {
                addAgent(agentConfig, false);
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    private void addAgent(AgentConfiguration agentConfig, boolean ping)
    {
        if (configurationTemplateManager.isDeeplyValid(agentConfig.getConfigurationPath()))
        {
            try
            {
                AgentService agentService = createAgentService(agentConfig);
                AgentState agentState = agentStateManager.getAgentState(agentConfig.getAgentStateId());
                if (agentState.getEnableState() == AgentState.EnableState.UPGRADING)
                {
                    // Something went wrong: lost contact with slave (or master
                    // died) during upgrade.
                    agentState.setEnableState(AgentState.EnableState.FAILED_UPGRADE);
                    agentStateManager.save(agentState);
                }

                DefaultAgent agent = new DefaultAgent(agentConfig, agentState, agentService);
                agents.put(agentConfig.getHandle(), agent);

                if (ping)
                {
                    pingAgent(agent, false);
                }
            }
            catch (Exception e)
            {
                LOG.severe("Unable to initialise agent '" + agentConfig.getName() + "': " + e.getMessage());
            }
        }
    }

    private AgentService createAgentService(AgentConfiguration agentConfig) throws Exception
    {
        if (agentConfig.isRemote())
        {
            // Object factory fails us here as some of these deps are in the
            // same context.
            SlaveAgentService agentService = new SlaveAgentService(slaveProxyFactory.createProxy(agentConfig), agentConfig);
            agentService.setConfigurationManager(configurationManager);
            agentService.setConfigurationProvider(configurationProvider);
            agentService.setResourceManager(resourceManager);
            agentService.setServiceTokenManager(serviceTokenManager);
            return agentService;
        }
        else
        {
            // We wire this up ourselves as it uses resources that are
            // defined in the same context as this agent manager (and
            // thus are not in the context atm :|).
            MasterAgentService agentService = new MasterAgentService(agentConfig);
            agentService.setConfigurationManager(configurationManager);
            agentService.setConfigurationProvider(configurationProvider);
            agentService.setMasterRecipeProcessor(masterRecipeProcessor);
            agentService.setResourceManager(resourceManager);
            agentService.setServerMessagesHandler(serverMessagesHandler);
            agentService.setStartupManager(startupManager);
            return agentService;
        }
    }

    public void pingAgents()
    {
        Collection<Agent> copyOfAgents = null;

        lock.lock();
        try
        {
            copyOfAgents = new ArrayList<Agent>(agents.values());
        }
        finally
        {
            lock.unlock();
        }

        // pinging the slave agents may take a while, so lets do this outside the lock.
        for (Agent agent : copyOfAgents)
        {
            pingAgent(agent, true);
        }
    }

    public int getAgentCount()
    {
        return agents.size();
    }

    public void setAgentState(AgentConfiguration agentConfig, AgentState.EnableState state)
    {
        Agent agent = agents.get(agentConfig.getHandle());
        if (agent != null)
        {
            AgentState agentState = agentStateManager.getAgentState(agent.getId());
            agentState.setEnableState(state);
            agentStateManager.save(agentState);
            agentChanged(agent.getConfig());
        }
    }

    private void pingAgent(final Agent agent, boolean inline)
    {
        if (agent.isEnabled())
        {
            Runnable pingRunner = new Runnable()
            {
                public void run()
                {
                    Status oldStatus = agent.getStatus();

                    FutureTask<SlaveStatus> future = new FutureTask<SlaveStatus>(new Pinger(agent));
                    pingerThreadPool.execute(future);

                    SlaveStatus status;

                    try
                    {
                        status = future.get(PING_TIMEOUT, TimeUnit.SECONDS);
                    }
                    catch (TimeoutException e)
                    {
                        LOG.warning("Timed out pinging agent '" + agent.getConfig().getName() + "'", e);
                        status = new SlaveStatus(Status.OFFLINE, "Agent ping timed out");
                    }
                    catch (Exception e)
                    {
                        String message = "Unexpected error pinging agent '" + agent.getConfig().getName() + "': " + e.getMessage();
                        LOG.warning(message);
                        LOG.debug(e);
                        status = new SlaveStatus(Status.OFFLINE, message);
                    }

                    agent.updateStatus(status);

                    // If the slave has just come online, run resource
                    // discovery
                    if (status.getStatus().isOnline())
                    {
                        if (oldStatus.isOnline() && status.isFirst())
                        {
                            // The agent bounced between pings.  Act like we saw it by
                            // sending the offline event and setting old status to
                            // offline.
                            agent.setStatus(Status.OFFLINE);
                            eventManager.publish(new AgentStatusEvent(this, oldStatus, agent));
                            oldStatus = Status.OFFLINE;
                            agent.updateStatus(status);
                        }

                        if (!oldStatus.isOnline())
                        {
                            if (status.getRecipeId() != 0)
                            {
                                // The agent appears to have just come online but is
                                // building.  Snap it out of that.
                                try
                                {
                                    agent.getService().terminateRecipe(status.getRecipeId());
                                }
                                catch (Exception e)
                                {
                                    LOG.severe("Unable to terminate unwanted recipe on agent '" + agent.getConfig().getName() + "': " + e.getMessage(), e);
                                }
                            }

                            try
                            {
                                List<Resource> resources = agent.getService().discoverResources();
                                resourceManager.addDiscoveredResources(agent.getConfig().getConfigurationPath(), resources);
                            }
                            catch (Exception e)
                            {
                                LOG.warning("Unable to discover resource for agent '" + agent.getConfig().getName() + "': " + e.getMessage(), e);
                            }
                        }
                    }

                    if (oldStatus != agent.getStatus())
                    {
                        eventManager.publish(new AgentStatusEvent(this, oldStatus, agent));

                        if (status.getStatus() == Status.VERSION_MISMATCH)
                        {
                            // Try to update this agent.
                            updateAgent(agent);
                        }
                    }
                }
            };

            if(inline)
            {
                pingRunner.run();
            }
            else
            {
                pingPool.execute(pingRunner);
            }
        }
    }

    private void updateAgent(Agent agent)
    {
        AgentState agentState = agentStateManager.getAgentState(agent.getId());
        agentState.setEnableState(AgentState.EnableState.UPGRADING);
        agentStateManager.save(agentState);
        agent.setAgentState(agentState);

        String masterUrl = "http://" + MasterAgentService.constructMasterLocation(configurationProvider.get(GlobalConfiguration.class), configurationManager.getSystemConfig());
        AgentUpdater updater = new AgentUpdater(agent, masterUrl, eventManager, configurationManager.getSystemPaths(), threadFactory);
        updatersLock.lock();

        try
        {
            updaters.put(agent.getConfig().getHandle(), updater);
            updater.start();
        }
        finally
        {
            updatersLock.unlock();
        }
    }

    public void stop(boolean force)
    {
        if (force)
        {
            pingerThreadPool.shutdownNow();
            updatersLock.lock();
            try
            {
                for (AgentUpdater updater : updaters.values())
                {
                    updater.stop(force);
                }
            }
            finally
            {
                updatersLock.unlock();
            }
        }
        else
        {
            pingerThreadPool.shutdown();
        }
    }

    public void handleEvent(Event evt)
    {
        AgentUpgradeCompleteEvent suce = (AgentUpgradeCompleteEvent) evt;
        Agent agent = suce.getAgent();
        AgentState agentState = agentStateManager.getAgentState(agent.getId());

        updatersLock.lock();
        try
        {
            updaters.remove(agentState.getId());
        }
        finally
        {
            updatersLock.unlock();
        }

        if (suce.isSuccessful())
        {
            suce.getAgent().setStatus(Status.OFFLINE);
            agentState.setEnableState(AgentState.EnableState.ENABLED);
            agentStateManager.save(agentState);
            agent.setAgentState(agentState);
            pingAgent(suce.getAgent(), false);
        }
        else
        {
            agentState.setEnableState(AgentState.EnableState.FAILED_UPGRADE);
            agent.setAgentState(agentState);
            agentStateManager.save(agentState);
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{AgentUpgradeCompleteEvent.class};
    }

    public List<Agent> getAllAgents()
    {
        lock.lock();
        try
        {
            List<Agent> result = new LinkedList<Agent>(agents.values());
            final Comparator<String> c = new Sort.StringComparator();
            Collections.sort(result, new Comparator<Agent>()
            {
                public int compare(Agent o1, Agent o2)
                {
                    return c.compare(o1.getConfig().getName(), o2.getConfig().getName());
                }
            });
            return result;
        }
        finally
        {
            lock.unlock();
        }
    }

    public List<Agent> getOnlineAgents()
    {
        lock.lock();
        try
        {
            List<Agent> online = new LinkedList<Agent>();
            for (Agent a : agents.values())
            {
                if (a.isOnline())
                {
                    online.add(a);
                }
            }

            return online;
        }
        finally
        {
            lock.unlock();
        }
    }

    public Agent getAgent(long handle)
    {
        lock.lock();
        try
        {
            return agents.get(handle);
        }
        finally
        {
            lock.unlock();
        }
    }

    public void pingAgent(AgentConfiguration agent, boolean inline)
    {
        pingAgent(agents.get(agent.getHandle()), inline);
    }

    public void agentAdded(AgentConfiguration agentConfig)
    {
        AgentState agentState = agentStateManager.getAgentState(agentConfig.getAgentStateId());
        if (agentState != null)
        {
            lock.lock();
            try
            {
                addAgent(agentConfig, true);

                licenseManager.refreshAuthorisations();
            }
            finally
            {
                lock.unlock();
            }
        }
    }

    public void agentChanged(AgentConfiguration agentConfig)
    {
        AgentState agentState = agentStateManager.getAgentState(agentConfig.getAgentStateId());
        if (agentState != null)
        {
            lock.lock();
            try
            {
                removeAgent(agentConfig.getHandle());
                addAgent(agentConfig, true);
            }
            finally
            {
                lock.unlock();
            }
        }
    }

    public void agentDeleted(AgentConfiguration agentConfig)
    {
        lock.lock();
        try
        {
            removeAgent(agentConfig.getHandle());
            licenseManager.refreshAuthorisations();
        }
        finally
        {
            lock.unlock();
        }
    }

    private void removeAgent(long handle)
    {
        Agent agent = agents.remove(handle);
        if (agent != null)
        {
            eventManager.publish(new AgentRemovedEvent(this, agent));
        }
    }

    public void upgradeStatus(UpgradeStatus upgradeStatus)
    {
        updatersLock.lock();
        try
        {
            AgentUpdater updater = updaters.get(upgradeStatus.getHandle());
            if (updater != null)
            {
                updater.upgradeStatus(upgradeStatus);
            }
            else
            {
                LOG.warning("Received upgrade status for agent that is not upgrading [" + upgradeStatus.getHandle() + "]");
            }
        }
        finally
        {
            updatersLock.unlock();
        }
    }

    public Agent getAgent(String name)
    {
        try // synchronize access to the agents map.
        {
            lock.lock();
            for (Agent s : agents.values())
            {
                if (s.getConfig().getName().equals(name))
                {
                    return s;
                }
            }
        }
        finally
        {
            lock.unlock();
        }

        return null;
    }

    public void setAgentStateManager(AgentStateManager agentStateManager)
    {
        this.agentStateManager = agentStateManager;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setResourceManager(ResourceManager resourceManager)
    {
        this.resourceManager = resourceManager;
    }

    public void setSlaveProxyFactory(SlaveProxyFactory slaveProxyFactory)
    {
        this.slaveProxyFactory = slaveProxyFactory;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
        eventManager.register(this);
    }

    public void setServiceTokenManager(ServiceTokenManager serviceTokenManager)
    {
        this.serviceTokenManager = serviceTokenManager;
    }

    public void setLicenseManager(LicenseManager licenseManager)
    {
        this.licenseManager = licenseManager;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }

    public void setMasterRecipeProcessor(MasterRecipeProcessor masterRecipeProcessor)
    {
        this.masterRecipeProcessor = masterRecipeProcessor;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }

    public void setConfigurationStateManager(ConfigurationStateManager configurationStateManager)
    {
        configurationStateManager.register(AgentConfiguration.class, this);
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setServerMessagesHandler(ServerMessagesHandler serverMessagesHandler)
    {
        this.serverMessagesHandler = serverMessagesHandler;
    }

    public void setStartupManager(StartupManager startupManager)
    {
        this.startupManager = startupManager;
    }

    public void setThreadFactory(ThreadFactory threadFactory)
    {
        this.threadFactory = threadFactory;
    }

    private class Pinger implements Callable<SlaveStatus>
    {
        private Agent agent;

        public Pinger(Agent agent)
        {
            this.agent = agent;
        }

        public SlaveStatus call() throws Exception
        {
            SlaveStatus status;

            try
            {
                int build = agent.getService().ping();
                if (build == masterBuildNumber)
                {
                    status = agent.getService().getStatus("http://" + MasterAgentService.constructMasterLocation(configurationProvider.get(GlobalConfiguration.class), configurationManager.getSystemConfig()));
                }
                else
                {
                    status = new SlaveStatus(Status.VERSION_MISMATCH);
                }
            }
            catch (Exception e)
            {
                Throwable cause = e.getCause();
                // the most common cause of the exception is the Connect Exception.
                if (cause instanceof ConnectException)
                {
                    status = new SlaveStatus(Status.OFFLINE, cause.getMessage());
                }
                else
                {
                    LOG.warning("Exception pinging agent '" + agent.getConfig().getName() + "': " + e.getMessage());
                    LOG.debug(e);
                    status = new SlaveStatus(Status.OFFLINE, "Exception: '" + e.getClass().getName() + "'. Reason: " + e.getMessage());
                }
            }

            status.setPingTime(System.currentTimeMillis());
            return status;
        }
    }
}
