package com.zutubi.pulse.master.agent;

import com.zutubi.events.Event;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.master.bootstrap.DefaultSetupManager;
import com.zutubi.pulse.master.events.AgentAddedEvent;
import com.zutubi.pulse.master.events.AgentChangedEvent;
import com.zutubi.pulse.master.events.AgentPingRequestedEvent;
import com.zutubi.pulse.master.events.AgentRemovedEvent;
import com.zutubi.pulse.master.license.LicenseManager;
import com.zutubi.pulse.master.license.authorisation.AddAgentAuthorisation;
import com.zutubi.pulse.master.model.AgentState;
import com.zutubi.pulse.master.model.ProjectManager;
import static com.zutubi.pulse.master.model.UserManager.ALL_USERS_GROUP_NAME;
import static com.zutubi.pulse.master.model.UserManager.ANONYMOUS_USERS_GROUP_NAME;
import com.zutubi.pulse.master.model.persistence.AgentStateDao;
import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.AGENTS_SCOPE;
import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.GROUPS_SCOPE;
import com.zutubi.pulse.master.tove.config.agent.AgentAclConfiguration;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.master.tove.config.group.GroupConfiguration;
import com.zutubi.pulse.servercore.services.SlaveService;
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
public class DefaultAgentManager implements AgentManager, ExternalStateManager<AgentConfiguration>, com.zutubi.events.EventListener
{
    private static final Logger LOG = Logger.getLogger(DefaultAgentManager.class);

    private static final int DEFAULT_AGENT_PORT = 8090;

    private ReentrantLock lock = new ReentrantLock();
    private Map<Long, Agent> agents;

    private ObjectFactory objectFactory;
    private AgentStatusManager agentStatusManager;
    private ConfigurationProvider configurationProvider;
    private ConfigurationTemplateManager configurationTemplateManager;
    private TypeRegistry typeRegistry;
    private EventManager eventManager;
    private SlaveProxyFactory slaveProxyFactory;
    private ThreadFactory threadFactory;
    private AgentStateDao agentStateDao;
    private ProjectManager projectManager;
    private HostManager hostManager;

    private LicenseManager licenseManager;

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
        hostManager.init(this);

        refreshAgents();

        // register the canAddAgent license authorisation.
        AddAgentAuthorisation addAgentAuthorisation = new AddAgentAuthorisation();
        addAgentAuthorisation.setAgentManager(this);
        licenseManager.addAuthorisation(addAgentAuthorisation);

        // ensure that we create the default master agent.
        ensureDefaultAgentsDefined();

        hostManager.pingHosts();
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
        agentStateDao.save(state);
        return state.getId();
    }

    public void rollbackState(long id)
    {
        deleteState(id);
    }

    public AgentState getState(long id)
    {
        return agentStateDao.findById(id);
    }

    public void deleteState(AgentState state)
    {
        deleteState(state.getId());
    }

    private void deleteState(long id)
    {
        AgentState agentState = agentStateDao.findById(id);
        if (agentState != null)
        {
            projectManager.removeReferencesToAgent(id);
            agentStateDao.delete(agentState);
        }
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
                AgentState agentState = getState(agentConfig.getAgentStateId());
                Host host = changeExisting ? hostManager.agentChanged(agentConfig) : hostManager.agentAdded(agentConfig);
                if (agentState.getEnableState() == AgentState.EnableState.DISABLING)
                {
                    agentState.setEnableState(AgentState.EnableState.DISABLED);
                    agentStateDao.save(agentState);
                }

                DefaultAgent agent = new DefaultAgent(agentConfig, agentState, createAgentService(agentConfig), host);
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
                    hostManager.pingHost(host);
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

    public int getAgentCount()
    {
        return agents.size();
    }

    public void setEnableState(Agent agent, AgentState.EnableState state)
    {
        AgentState agentState = agentStateDao.findById(agent.getId());
        agentState.setEnableState(state);
        agentStateDao.save(agentState);
        agent.setAgentState(agentState);
    }

    public void handleEvent(Event evt)
    {
        if (evt instanceof AgentPingRequestedEvent)
        {
            AgentConfiguration agentConfig = ((AgentPingRequestedEvent) evt).getAgent().getConfig();
            hostManager.pingHost(hostManager.getHostForAgent(agentConfig));
        }
        else if (evt instanceof ConfigurationEventSystemStartedEvent)
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
        return agentStatusManager.getAgentsByStatusPredicate(new Predicate<AgentStatus>()
        {
            public boolean satisfied(AgentStatus status)
            {
                return status.isOnline();
            }
        });
    }

    public List<Agent> getAvailableAgents()
    {
        return agentStatusManager.getAgentsByStatusPredicate(new Predicate<AgentStatus>()
        {
            public boolean satisfied(AgentStatus status)
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
        hostManager.pingHost(hostManager.getHostForAgent(agent));
    }

    public void agentAdded(AgentConfiguration agentConfig)
    {
        AgentState agentState = agentStateDao.findById(agentConfig.getAgentStateId());
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
        AgentState agentState = agentStateDao.findById(agentConfig.getAgentStateId());
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
            hostManager.agentDeleted(agentConfig);
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

    public Agent getAgent(String name)
    {
        try
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

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setAgentStateDao(AgentStateDao agentStateDao)
    {
        this.agentStateDao = agentStateDao;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setHostManager(HostManager hostManager)
    {
        this.hostManager = hostManager;
    }
}
