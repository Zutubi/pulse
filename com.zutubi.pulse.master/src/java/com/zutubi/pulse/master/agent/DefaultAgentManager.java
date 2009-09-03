package com.zutubi.pulse.master.agent;

import com.zutubi.events.Event;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.Stoppable;
import com.zutubi.pulse.master.bootstrap.DefaultSetupManager;
import com.zutubi.pulse.master.events.*;
import com.zutubi.pulse.master.license.LicenseManager;
import com.zutubi.pulse.master.license.authorisation.AddAgentAuthorisation;
import com.zutubi.pulse.master.model.AgentState;
import com.zutubi.pulse.master.model.AgentStateManager;
import static com.zutubi.pulse.master.model.UserManager.ALL_USERS_GROUP_NAME;
import static com.zutubi.pulse.master.model.UserManager.ANONYMOUS_USERS_GROUP_NAME;
import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.AGENTS_SCOPE;
import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.GROUPS_SCOPE;
import com.zutubi.pulse.master.tove.config.agent.AgentAclConfiguration;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.master.tove.config.group.GroupConfiguration;
import com.zutubi.pulse.servercore.agent.Status;
import com.zutubi.pulse.servercore.services.SlaveService;
import com.zutubi.pulse.servercore.services.UpgradeStatus;
import com.zutubi.tove.config.*;
import com.zutubi.tove.events.ConfigurationEventSystemStartedEvent;
import com.zutubi.tove.events.ConfigurationSystemStartedEvent;
import static com.zutubi.tove.security.AccessManager.ACTION_VIEW;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.util.Predicate;
import com.zutubi.util.Sort;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.ReentrantLock;

/**
 */
public class DefaultAgentManager implements AgentManager, ExternalStateManager<AgentConfiguration>, com.zutubi.events.EventListener, Stoppable
{
    private static final Logger LOG = Logger.getLogger(DefaultAgentManager.class);

    private static final int DEFAULT_AGENT_PORT = 8090;

    private ReentrantLock lock = new ReentrantLock();
    private Map<Long, Agent> agents;

    private ObjectFactory objectFactory;
    private AgentStatusManager agentStatusManager;
    private AgentStateManager agentStateManager;
    private ConfigurationProvider configurationProvider;
    private ConfigurationTemplateManager configurationTemplateManager;
    private TypeRegistry typeRegistry;
    private EventManager eventManager;
    private SlaveProxyFactory slaveProxyFactory;
    private ThreadFactory threadFactory;
    private AgentPingService agentPingService;

    private LicenseManager licenseManager;

    private Map<Long, AgentUpdater> updaters = new TreeMap<Long, AgentUpdater>();
    private ReentrantLock updatersLock = new ReentrantLock();

    private void handleConfigurationEventSystemStarted(ConfigurationEventSystemStartedEvent event)
    {
        // Create prior to any AgentAddedEvents being fired.
        agentStatusManager = new AgentStatusManager(this, Executors.newSingleThreadExecutor(new ThreadFactory()
        {
            public Thread newThread(Runnable r)
            {
                Thread t = threadFactory.newThread(r);
                t.setName("Agent Status Manager Event Pump");
                return t;
            }
        }), eventManager);

        configurationProvider = event.getConfigurationProvider();
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
    }

    public void handleConfigurationSystemStarted()
    {
        refreshAgents();

        // register the canAddAgent license authorisation.
        AddAgentAuthorisation addAgentAuthorisation = new AddAgentAuthorisation();
        addAgentAuthorisation.setAgentManager(this);
        licenseManager.addAuthorisation(addAgentAuthorisation);

        // ensure that we create the default master agent.
        ensureDefaultAgentsDefined();

        pingAgents();
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
                GroupConfiguration group = configurationProvider.get(PathUtils.getPath(GROUPS_SCOPE, ALL_USERS_GROUP_NAME), GroupConfiguration.class);
                globalAgent.addPermission(new AgentAclConfiguration(group, ACTION_VIEW));

                // Anonymous users can view all agents by default (but only
                // when anonymous access is explicitly enabled).
                group = configurationProvider.get(PathUtils.getPath(GROUPS_SCOPE, ANONYMOUS_USERS_GROUP_NAME), GroupConfiguration.class);
                globalAgent.addPermission(new AgentAclConfiguration(group, ACTION_VIEW));

                CompositeType agentType = typeRegistry.getType(AgentConfiguration.class);
                MutableRecord globalTemplate = agentType.unstantiate(globalAgent);
                configurationTemplateManager.markAsTemplate(globalTemplate);
                configurationTemplateManager.insertRecord(AGENTS_SCOPE, globalTemplate);

                // reload the template so that we have the handle.
                Record persistedGlobalTemplate = configurationTemplateManager.getRecord(PathUtils.getPath(AGENTS_SCOPE, GLOBAL_AGENT_NAME));

                AgentConfiguration masterAgent = new AgentConfiguration();
                masterAgent.setName(MASTER_AGENT_NAME);
                masterAgent.setRemote(false);

                MutableRecord masterAgentRecord = agentType.unstantiate(masterAgent);
                configurationTemplateManager.setParentTemplate(masterAgentRecord, persistedGlobalTemplate.getHandle());
                configurationTemplateManager.insertRecord(AGENTS_SCOPE, masterAgentRecord);
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

    public Object getState(long id)
    {
        return agentStateManager.getAgentState(id);
    }

    private void refreshAgents()
    {
        lock.lock();
        try
        {
            agents = new TreeMap<Long, Agent>();
            for (AgentConfiguration agentConfig : configurationProvider.getAll(AgentConfiguration.class))
            {
                addAgent(agentConfig, false, false);
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    private void addAgent(AgentConfiguration agentConfig, boolean ping, boolean changeExisting)
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
                else if (agentState.getEnableState() == AgentState.EnableState.DISABLING)
                {
                    agentState.setEnableState(AgentState.EnableState.DISABLED);
                    agentStateManager.save(agentState);
                }

                DefaultAgent agent = new DefaultAgent(agentConfig, agentState, agentService);
                agents.put(agentConfig.getHandle(), agent);

                if (changeExisting)
                {
                    eventManager.publish(new AgentChangedEvent(this, agent));
                }
                else
                {
                    eventManager.publish(new AgentAddedEvent(this, agent));
                }

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
    }

    private AgentService createAgentService(AgentConfiguration agentConfig)
    {
        if (agentConfig.isRemote())
        {
            return objectFactory.buildBean(SlaveAgentService.class, new Class[]{SlaveService.class, AgentConfiguration.class}, new Object[]{slaveProxyFactory.createProxy(agentConfig), agentConfig});
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
        for (Agent agent : copyOfAgents)
        {
            pingAgent(agent);
        }
    }

    public int getAgentCount()
    {
        return agents.size();
    }

    public void setEnableState(Agent agent, AgentState.EnableState state)
    {
        AgentState agentState = agentStateManager.getAgentState(agent.getId());
        agentState.setEnableState(state);
        agentStateManager.save(agentState);
        agent.setAgentState(agentState);
    }
    
    private void pingAgent(final Agent agent)
    {
        if (agent.isEnabled())
        {
            agentPingService.requestPing(agent, agent.getService());
        }
    }

    public void stop(boolean force)
    {
        if (force)
        {
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
    }

    private void handleAgentUpgradeRequired(AgentUpgradeRequiredEvent event)
    {
        Agent agent = event.getAgent();
        AgentState agentState = agentStateManager.getAgentState(agent.getId());
        agentState.setEnableState(AgentState.EnableState.UPGRADING);
        agentStateManager.save(agentState);
        agent.setAgentState(agentState);

        AgentUpdater updater = objectFactory.buildBean(AgentUpdater.class, new Class[]{Agent.class}, new Object[]{agent});
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

    private void handleAgentUpgradeComplete(AgentUpgradeCompleteEvent suce)
    {
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
            suce.getAgent().updateStatus(Status.OFFLINE);
            agentState.setEnableState(AgentState.EnableState.ENABLED);
            agentStateManager.save(agentState);
            agent.setAgentState(agentState);
            pingAgent(suce.getAgent());
        }
        else
        {
            agentState.setEnableState(AgentState.EnableState.FAILED_UPGRADE);
            agent.setAgentState(agentState);
            agentStateManager.save(agentState);
        }
    }

    public void handleEvent(Event evt)
    {
        if(evt instanceof AgentPingRequestedEvent)
        {
            pingAgent(((AgentPingRequestedEvent) evt).getAgent());
        }
        else if(evt instanceof AgentUpgradeRequiredEvent)
        {
            handleAgentUpgradeRequired((AgentUpgradeRequiredEvent) evt);
        }
        else if(evt instanceof AgentUpgradeCompleteEvent)
        {
            handleAgentUpgradeComplete((AgentUpgradeCompleteEvent) evt);
        }
        else if(evt instanceof ConfigurationEventSystemStartedEvent)
        {
            handleConfigurationEventSystemStarted((ConfigurationEventSystemStartedEvent) evt);
        }
        else
        {
            handleConfigurationSystemStarted();
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[] {
                AgentPingRequestedEvent.class,
                AgentUpgradeRequiredEvent.class,
                AgentUpgradeCompleteEvent.class,
                ConfigurationEventSystemStartedEvent.class,
                ConfigurationSystemStartedEvent.class
        };
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
        return agentStatusManager.getAgentsByStatusPredicate(new Predicate<Status>()
        {
            public boolean satisfied(Status status)
            {
                return status.isOnline();
            }
        });
    }

    public List<Agent> getAvailableAgents()
    {
        return agentStatusManager.getAgentsByStatusPredicate(new Predicate<Status>()
        {
            public boolean satisfied(Status status)
            {
                return status.isOnline() && !status.isBusy();
            }
        });
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

    public Agent getAgent(AgentConfiguration agent)
    {
        return getAgent(agent.getHandle());
    }

    public void pingAgent(AgentConfiguration agent)
    {
        pingAgent(getAgent(agent));
    }

    public void agentAdded(AgentConfiguration agentConfig)
    {
        AgentState agentState = agentStateManager.getAgentState(agentConfig.getAgentStateId());
        if (agentState != null)
        {
            lock.lock();
            try
            {
                addAgent(agentConfig, true, false);

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
                removeAgent(agentConfig.getHandle(), true);
                addAgent(agentConfig, true, true);
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
            removeAgent(agentConfig.getHandle(), false);
            licenseManager.refreshAuthorisations();
        }
        finally
        {
            lock.unlock();
        }
    }

    private void removeAgent(long handle, boolean changeExisting)
    {
        Agent agent = agents.remove(handle);
        if (agent != null && !changeExisting)
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

    public void setSlaveProxyFactory(SlaveProxyFactory slaveProxyFactory)
    {
        this.slaveProxyFactory = slaveProxyFactory;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
        eventManager.register(this);
    }

    public void setLicenseManager(LicenseManager licenseManager)
    {
        this.licenseManager = licenseManager;
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

    public void setThreadFactory(ThreadFactory threadFactory)
    {
        this.threadFactory = threadFactory;
    }

    public void setAgentPingService(AgentPingService agentPingService)
    {
        this.agentPingService = agentPingService;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
