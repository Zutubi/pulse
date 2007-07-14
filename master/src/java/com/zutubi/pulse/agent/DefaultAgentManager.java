package com.zutubi.pulse.agent;

import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.prototype.config.ConfigurationRegistry;
import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.prototype.config.TypeListener;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.pulse.*;
import com.zutubi.pulse.bootstrap.DefaultSetupManager;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.core.Stoppable;
import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.events.*;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.license.LicenseManager;
import com.zutubi.pulse.license.authorisation.AddAgentAuthorisation;
import com.zutubi.pulse.model.AgentState;
import com.zutubi.pulse.model.AgentStateManager;
import com.zutubi.pulse.model.ResourceManager;
import com.zutubi.pulse.prototype.config.admin.GeneralAdminConfiguration;
import com.zutubi.pulse.prototype.config.agent.AgentConfiguration;
import com.zutubi.pulse.services.ServiceTokenManager;
import com.zutubi.pulse.services.SlaveStatus;
import com.zutubi.pulse.services.UpgradeStatus;
import com.zutubi.util.Sort;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;

import java.net.ConnectException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 */
public class DefaultAgentManager implements AgentManager, EventListener, Stoppable
{
    private static final Logger LOG = Logger.getLogger(DefaultAgentManager.class);

    // ping timeout in seconds.
    private static final int PING_TIMEOUT = Integer.getInteger("pulse.agent.ping.timeout", 45);
    private static final String GLOBAL_AGENT_NAME = "global agent template";
    private static final String MASTER_AGENT_NAME = "master agent";
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
    private ObjectFactory objectFactory;

    private LicenseManager licenseManager;
    private ExecutorService pingService = Executors.newCachedThreadPool();

    private Map<Long, AgentUpdater> updaters = new TreeMap<Long, AgentUpdater>();
    private ReentrantLock updatersLock = new ReentrantLock();

    public void init()
    {
        TypeListener<AgentConfiguration> listener = new TypeListener<AgentConfiguration>(AgentConfiguration.class)
        {
            public void postInsert(AgentConfiguration instance)
            {
                AgentState state = new AgentState();
                agentStateManager.save(state);
                instance.setAgentStateId(state.getId());
                agentAdded(instance);
            }

            public void preDelete(AgentConfiguration instance)
            {
                agentDeleted(instance);
            }

            public void postSave(AgentConfiguration instance)
            {
                agentChanged(instance);
            }
        };
        listener.register(configurationProvider);

        refreshAgents();

        // register the canAddAgent license authorisation.
        AddAgentAuthorisation addAgentAuthorisation = new AddAgentAuthorisation();
        addAgentAuthorisation.setAgentManager(this);
        licenseManager.addAuthorisation(addAgentAuthorisation);

        // ensure that we create the default master agent.
        if (DefaultSetupManager.initialInstallation)
        {
            CompositeType agentType = typeRegistry.getType(AgentConfiguration.class);
            MutableRecord globalTemplate = agentType.createNewRecord(false);
            globalTemplate.put("name", GLOBAL_AGENT_NAME);
            globalTemplate.put("port", Integer.toString(DEFAULT_AGENT_PORT));
            globalTemplate.put("remote", "true");

            configurationTemplateManager.markAsTemplate(globalTemplate);
            configurationTemplateManager.insertRecord(ConfigurationRegistry.AGENTS_SCOPE, globalTemplate);

            // reload the template so that we have the handle.
            Record persistedGlobalTemplate = configurationTemplateManager.getRecord(PathUtils.getPath(ConfigurationRegistry.AGENTS_SCOPE, GLOBAL_AGENT_NAME));

            MutableRecord masterAgent = agentType.createNewRecord(false);
            masterAgent.put("name", MASTER_AGENT_NAME);
            masterAgent.put("remote", "false");

            configurationTemplateManager.setParentTemplate(masterAgent, persistedGlobalTemplate.getHandle());
            configurationTemplateManager.insertRecord(ConfigurationRegistry.AGENTS_SCOPE, masterAgent);
        }
    }

    private void refreshAgents()
    {
        lock.lock();
        try
        {
            agents = new TreeMap<Long, Agent>();
            for (AgentConfiguration agentConfig: configurationProvider.getAll(AgentConfiguration.class))
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
        try
        {
            AgentService agentService = createAgentService(agentConfig);
            AgentState agentState = agentStateManager.getAgentState(agentConfig.getAgentStateId());
            if(agentState.getEnableState() == AgentState.EnableState.UPGRADING)
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
                pingAgent(agent);
            }
        }
        catch (Exception e)
        {
            LOG.severe("Unable to initialise agent '" + agentConfig.getName() + "': " + e.getMessage());
        }
    }

    private AgentService createAgentService(AgentConfiguration agentConfig) throws Exception
    {
        if(agentConfig.isRemote())
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
            return objectFactory.buildBean(MasterAgentService.class, new Class[]{AgentConfiguration.class}, new Object[]{agentConfig});
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
        for (Agent agent: copyOfAgents)
        {
            pingAgent(agent);
        }
    }

    public int getAgentCount()
    {
        return agents.size();
    }

    public void enableAgent(long handle)
    {
        setAgentState(handle, AgentState.EnableState.ENABLED);
    }

    public void disableAgent(long handle)
    {
        setAgentState(handle, AgentState.EnableState.DISABLED);
    }

    public void setAgentState(long handle, AgentState.EnableState state)
    {
        Agent agent = agents.get(handle);
        if (agent != null)
        {
            AgentState agentState = agent.getState();
            agentState.setEnableState(state);
            agentStateManager.save(agentState);
            agentChanged(agent.getConfig());
        }
    }

    private void pingAgent(Agent agent)
    {
        if (agent.isEnabled())
        {
            Status oldStatus = agent.getStatus();

            FutureTask<SlaveStatus> future = new FutureTask<SlaveStatus>(new Pinger(agent));
            pingService.execute(future);

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
            catch(Exception e)
            {
                String message = "Unexpected error pinging agent '" + agent.getConfig().getName() + "': " + e.getMessage();
                LOG.warning(message);
                LOG.debug(e);
                status = new SlaveStatus(Status.OFFLINE, message);
            }

            agent.updateStatus(status);

            // If the slave has just come online, run resource
            // discovery
            if(!oldStatus.isOnline() && status.getStatus().isOnline())
            {
                try
                {
                    List<Resource> resources = agent.getService().discoverResources();
                    resourceManager.addDiscoveredResources(agent.getConfig().getHandle(), resources);
                }
                catch (Exception e)
                {
                    LOG.warning("Unable to discover resource for agent '" + agent.getConfig().getName() + "': " + e.getMessage(), e);
                }
            }

            if(oldStatus != agent.getStatus())
            {
                eventManager.publish(new AgentStatusEvent(this, oldStatus, agent));

                if(status.getStatus() == Status.VERSION_MISMATCH)
                {
                    // Try to update this agent.
                    updateAgent(agent);
                }
            }
        }
    }

    private void updateAgent(Agent agent)
    {
        AgentState agentState = agent.getState();
        agentState.setEnableState(AgentState.EnableState.UPGRADING);
        agentStateManager.save(agentState);

        String masterUrl = "http://" + MasterAgentService.constructMasterLocation(configurationProvider.get(GeneralAdminConfiguration.class), configurationManager.getSystemConfig());
        AgentUpdater updater = new AgentUpdater(agent, serviceTokenManager.getToken(), masterUrl, eventManager, configurationManager.getSystemPaths());
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
        if(force)
        {
            pingService.shutdownNow();
            updatersLock.lock();
            try
            {
                for(AgentUpdater updater: updaters.values())
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
            pingService.shutdown();
        }
    }

    public void handleEvent(Event evt)
    {
        AgentUpgradeCompleteEvent suce = (AgentUpgradeCompleteEvent) evt;
        AgentState agentState = suce.getAgent().getState();

        updatersLock.lock();
        try
        {
            updaters.remove(agentState.getId());
        }
        finally
        {
            updatersLock.unlock();
        }

        if(suce.isSuccessful())
        {
            suce.getAgent().setStatus(Status.OFFLINE);
            agentState.setEnableState(AgentState.EnableState.ENABLED);
            agentStateManager.save(agentState);
            pingAgent(suce.getAgent());
        }
        else
        {
            agentState.setEnableState(AgentState.EnableState.FAILED_UPGRADE);
            agentStateManager.save(agentState);
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[] { AgentUpgradeCompleteEvent.class };
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
            for(Agent a: agents.values())
            {
                if(a.isOnline())
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

    public void pingAgent(long handle)
    {
        pingAgent(agents.get(handle));
    }

    public void agentAdded(AgentConfiguration agentConfig)
    {
        AgentState agentState = agentStateManager.getAgentState(agentConfig.getAgentStateId());
        if(agentState != null)
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
        if(agentState != null)
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
        eventManager.publish(new AgentRemovedEvent(this, agent));
    }

    public void upgradeStatus(UpgradeStatus upgradeStatus)
    {
        updatersLock.lock();
        try
        {
            AgentUpdater updater = updaters.get(upgradeStatus.getHandle());
            if(updater != null)
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
            for(Agent s: agents.values())
            {
                if(s.getConfig().getName().equals(name))
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

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
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
                if(build == masterBuildNumber)
                {
                    status = agent.getService().getStatus("http://" + MasterAgentService.constructMasterLocation(configurationProvider.get(GeneralAdminConfiguration.class), configurationManager.getSystemConfig()));
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
