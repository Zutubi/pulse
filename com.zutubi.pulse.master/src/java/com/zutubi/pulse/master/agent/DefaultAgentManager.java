/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.agent;

import com.google.common.base.Predicate;
import com.zutubi.events.AsynchronousDelegatingListener;
import com.zutubi.events.Event;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.master.agent.statistics.AgentStatistics;
import com.zutubi.pulse.master.agent.statistics.AgentStatisticsManager;
import com.zutubi.pulse.master.bootstrap.DefaultSetupManager;
import com.zutubi.pulse.master.events.*;
import com.zutubi.pulse.master.model.AgentState;
import com.zutubi.pulse.master.model.AgentSynchronisationMessage;
import com.zutubi.pulse.master.model.persistence.AgentStateDao;
import com.zutubi.pulse.master.model.persistence.AgentSynchronisationMessageDao;
import com.zutubi.pulse.master.tove.config.agent.AgentAclConfiguration;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.master.tove.config.group.GroupConfiguration;
import com.zutubi.pulse.servercore.agent.SynchronisationMessage;
import com.zutubi.tove.config.*;
import com.zutubi.tove.events.ConfigurationEventSystemStartedEvent;
import com.zutubi.tove.events.ConfigurationSystemStartedEvent;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.util.NullaryFunction;
import com.zutubi.util.Sort;
import com.zutubi.util.UnaryProcedure;
import com.zutubi.util.adt.Pair;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.find;
import static com.zutubi.pulse.master.model.UserManager.ALL_USERS_GROUP_NAME;
import static com.zutubi.pulse.master.model.UserManager.ANONYMOUS_USERS_GROUP_NAME;
import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.AGENTS_SCOPE;
import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.GROUPS_SCOPE;
import static com.zutubi.tove.security.AccessManager.ACTION_VIEW;

/**
 */
public class DefaultAgentManager implements AgentManager, ExternalStateManager<AgentConfiguration>, com.zutubi.events.EventListener
{
    private static final Logger LOG = Logger.getLogger(DefaultAgentManager.class);

    private static final int DEFAULT_AGENT_PORT = 8090;

    private ReentrantLock lock = new ReentrantLock();
    private Map<Long, Agent> agents;
    private AgentStatisticsManager agentStatisticsManager;

    private ObjectFactory objectFactory;
    private AgentStatusManager agentStatusManager;
    private ConfigurationProvider configurationProvider;
    private ConfigurationTemplateManager configurationTemplateManager;
    private TypeRegistry typeRegistry;
    private EventManager eventManager;
    private SlaveProxyFactory slaveProxyFactory;
    private ThreadFactory threadFactory;
    private AgentStateDao agentStateDao;
    private AgentSynchronisationMessageDao agentSynchronisationMessageDao;
    private AgentSynchronisationService agentSynchronisationService;
    private HostManager hostManager;

    private void handleConfigurationEventSystemStarted(ConfigurationEventSystemStartedEvent event)
    {
        agentSynchronisationService.init(this);

        // Create prior to any AgentAddedEvents being fired.
        configurationProvider = event.getConfigurationProvider();
        agentStatusManager = new AgentStatusManager(this, Executors.newSingleThreadExecutor(new ThreadFactory()
        {
            public Thread newThread(Runnable r)
            {
                Thread t = threadFactory.newThread(r);
                t.setName("Agent Status Manager Event Pump");
                return t;
            }
        }), eventManager, configurationProvider);

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

        startStatisticsManager();

        refreshAgents();

        // ensure that we create the default master agent.
        ensureDefaultAgentsDefined();
    }

    private synchronized void startStatisticsManager()
    {
        agentStatisticsManager = objectFactory.buildBean(AgentStatisticsManager.class);
        agentStatisticsManager.init();
        String listenerName = agentStatisticsManager.getClass().getSimpleName() + " status change listener";
        eventManager.register(new AsynchronousDelegatingListener(agentStatisticsManager, listenerName, threadFactory));
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
                MutableRecord globalTemplate = agentType.unstantiate(globalAgent, PathUtils.getPath(AGENTS_SCOPE, GLOBAL_AGENT_NAME));
                configurationTemplateManager.markAsTemplate(globalTemplate);
                configurationTemplateManager.insertRecord(AGENTS_SCOPE, globalTemplate);

                // reload the template so that we have the handle.
                Record persistedGlobalTemplate = configurationTemplateManager.getRecord(PathUtils.getPath(AGENTS_SCOPE, GLOBAL_AGENT_NAME));

                AgentConfiguration masterAgent = new AgentConfiguration();
                masterAgent.setName(MASTER_AGENT_NAME);
                masterAgent.setRemote(false);

                MutableRecord masterAgentRecord = agentType.unstantiate(masterAgent, PathUtils.getPath(AGENTS_SCOPE, MASTER_AGENT_NAME));
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
            agentSynchronisationMessageDao.deleteByAgentState(agentState);
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
            return objectFactory.buildBean(SlaveAgentService.class, slaveProxyFactory.createProxy(agentConfig), agentConfig);
        }
        else
        {
            return objectFactory.buildBean(MasterAgentService.class, agentConfig);
        }
    }

    public int getAgentCount()
    {
        return agents.size();
    }

    public void withAvailableAgents(final UnaryProcedure<List<Agent>> fn)
    {
        agentStatusManager.withAgentsLock(new NullaryFunction<Object>()
        {
            public Object process()
            {
                List<Agent> availableAgents = agentStatusManager.getAgentsByStatusPredicate(new Predicate<AgentStatus>()
                {
                    public boolean apply(AgentStatus status)
                    {
                        return status.isAvailable();
                    }
                });

                fn.run(availableAgents);
                return null;
            }
        });
    }

    public AgentStatistics getAgentStatistics(Agent agent)
    {
        return new AgentStatistics(agentStatisticsManager.getStatisticsForAgent(agent.getId()));
    }

    public synchronized void updateStatistics()
    {
        if (agentStatisticsManager != null)
        {
            agentStatisticsManager.update();
        }
    }

    public void enqueueSynchronisationMessages(final Agent agent, final String taskType, final List<Pair<Properties, String>> propertiesDescriptionPairs)
    {
        final AgentState agentState = agentStateDao.findById(agent.getId());
        if (agentState != null)
        {
            // Note that it is not tragic if a few duplicates slip in.  The duplicate detection is
            // to stop the list of messages from growing out of hand for agents that are never
            // online to synchronise.  We could also consider setting a time or count limit.
            final List<AgentSynchronisationMessage> messages = agentSynchronisationMessageDao.queryMessages(agentState, AgentSynchronisationMessage.Status.QUEUED, taskType);
            final List<AgentSynchronisationMessage> newMessages = new ArrayList<AgentSynchronisationMessage>(propertiesDescriptionPairs.size());
            for (final Pair<Properties, String> pair: propertiesDescriptionPairs)
            {
                if (!matchingMessageExists(messages, pair.first, pair.second))
                {
                    final SynchronisationMessage message = new SynchronisationMessage(taskType, pair.first);
                    newMessages.add(new AgentSynchronisationMessage(agentState, message, pair.second));
                }
            }
            
            agentStatusManager.withAgentsLock(new NullaryFunction<Object>()
            {
                public Object process()
                {
                    for (AgentSynchronisationMessage newMessage : newMessages)
                    {
                        agentSynchronisationMessageDao.save(newMessage);
                    }
                    
                    agentSynchronisationMessageDao.flush();
                    eventManager.publish(new AgentSynchronisationMessagesEnqueuedEvent(this, agent));
                    return null;
                }
            });
        }
    }

    private boolean matchingMessageExists(final List<AgentSynchronisationMessage> existing, final Properties properties, final String description)
    {
        return any(existing, new Predicate<AgentSynchronisationMessage>()
        {
            public boolean apply(AgentSynchronisationMessage existingMessage)
            {
                return existingMessage.getDescription().equals(description) &&
                        existingMessage.getMessage().getArguments().equals(properties);
            }
        });
    }

    public void dequeueSynchronisationMessages(List<AgentSynchronisationMessage> messages)
    {
        for (AgentSynchronisationMessage message: messages)
        {
            agentSynchronisationMessageDao.delete(message);
        }
        agentSynchronisationMessageDao.flush();
    }

    public void saveSynchronisationMessages(List<AgentSynchronisationMessage> messages)
    {
        for (AgentSynchronisationMessage message: messages)
        {
            agentSynchronisationMessageDao.save(message);
        }

        agentSynchronisationMessageDao.flush();
    }

    public List<AgentSynchronisationMessage> getSynchronisationMessages(long agentId)
    {
        AgentState agentState = agentStateDao.findById(agentId);
        if (agentState != null)
        {
            return agentSynchronisationMessageDao.findByAgentState(agentState);
        }
        else
        {
            return Collections.emptyList();
        }
    }

    public List<AgentSynchronisationMessage> getProcessingSynchronisationMessages()
    {
        return agentSynchronisationMessageDao.findByStatus(AgentSynchronisationMessage.Status.PROCESSING);
    }

    public AgentSynchronisationMessage getSynchronisationMessage(long messageId)
    {
        return agentSynchronisationMessageDao.findById(messageId);
    }

    public boolean completeSynchronisation(final long agentId, final boolean successful)
    {
        return agentStatusManager.withAgentsLock(new NullaryFunction<Boolean>()
        {
            public Boolean process()
            {
                Agent agent = getAgentById(agentId);
                if (agent != null)
                {
                    List<AgentSynchronisationMessage> messages = getSynchronisationMessages(agentId);
                    if (agent.getStatus() == AgentStatus.SYNCHRONISING)
                    {
                        // When a cycle is unsuccessful (unable to sent messages to the
                        // agent), we declare it complete and let the status manager
                        // move the agent offline.  When we get a successful ping, a
                        // new cycle will start.
                        if (!successful || !any(messages, AgentSynchronisationService.INCOMPLETE_MESSAGES_PREDICATE))
                        {
                            eventManager.publish(new AgentSynchronisationCompleteEvent(this, getAgentById(agentId), successful));
                            return true;
                        }
                    }
                    
                    return !any(messages, AgentSynchronisationService.PENDING_MESSAGES_PREDICATE);
                }
                else
                {
                    // We don't want a new cycle to start in this case.
                    return true;
                }
            }
        });
    }

    public void updateAgentState(Agent agent, UnaryProcedure<AgentState> updateFn)
    {
        AgentState agentState = agentStateDao.findById(agent.getId());
        updateFn.run(agentState);
        agentStateDao.save(agentState);
        agent.setAgentState(agentState);
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
            public boolean apply(AgentStatus status)
            {
                return status.isOnline();
            }
        });
    }

    public Agent getAgentByHandle(long handle)
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

    public Agent getAgentById(final long agentId)
    {
        return find(agents.values(), new Predicate<Agent>()
        {
            public boolean apply(Agent agent)
            {
                return agent.getId() == agentId;
            }
        }, null);
    }

    public Agent getAgent(AgentConfiguration agent)
    {
        return getAgentByHandle(agent.getHandle());
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

    public void setHostManager(HostManager hostManager)
    {
        this.hostManager = hostManager;
    }

    public void setAgentSynchronisationMessageDao(AgentSynchronisationMessageDao agentSynchronisationMessageDao)
    {
        this.agentSynchronisationMessageDao = agentSynchronisationMessageDao;
    }

    public void setAgentSynchronisationService(AgentSynchronisationService agentSynchronisationService)
    {
        this.agentSynchronisationService = agentSynchronisationService;
    }

    void setAgentStatusManager(AgentStatusManager agentStatusManager)
    {
        this.agentStatusManager = agentStatusManager;
    }
}
