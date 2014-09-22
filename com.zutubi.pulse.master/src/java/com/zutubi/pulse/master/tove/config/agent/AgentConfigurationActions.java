package com.zutubi.pulse.master.tove.config.agent;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.agent.Host;
import com.zutubi.pulse.master.events.AgentDisableRequestedEvent;
import com.zutubi.pulse.master.events.AgentEnableRequestedEvent;
import com.zutubi.pulse.master.events.HostUpgradeRequestedEvent;
import com.zutubi.pulse.master.model.HostState;
import com.zutubi.pulse.servercore.agent.DeleteDirectoryTask;
import com.zutubi.pulse.servercore.agent.SynchronisationMessage;
import com.zutubi.pulse.servercore.agent.SynchronisationTaskFactory;
import com.zutubi.tove.annotations.Permission;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.util.adt.Pair;

import java.util.*;

import static com.zutubi.pulse.core.engine.api.BuildProperties.PROPERTY_AGENT;
import static com.zutubi.pulse.core.engine.api.BuildProperties.PROPERTY_AGENT_HANDLE;
import static com.zutubi.util.CollectionUtils.asPair;
import static com.zutubi.util.io.FileSystemUtils.encodeFilenameComponent;

/**
 *
 *
 */
public class AgentConfigurationActions
{
    public static final String ACTION_DISABLE = "disable";
    public static final String ACTION_DISABLE_NOW = "disableNow";
    public static final String ACTION_ENABLE = "enable";
    public static final String ACTION_CLEAN = "clean";
    public static final String ACTION_PING = "ping";
    public static final String ACTION_RETRY_UPGRADE = "retryUpgrade";

    private AgentManager agentManager;
    private EventManager eventManager;
    private SynchronisationTaskFactory synchronisationTaskFactory;

    public List<String> getActions(AgentConfiguration config)
    {
        List<String> actions = new LinkedList<String>();

        Agent agent = agentManager.getAgent(config);
        Host host = agent.getHost();
        if (host.isUpgrading())
        {
            if (host.getPersistentUpgradeState() == HostState.PersistentUpgradeState.FAILED_UPGRADE)
            {
                actions.add(ACTION_RETRY_UPGRADE);
            }
        }
        else
        {
            if (agent.isEnabled())
            {
                if (agent.isDisabling())
                {
                    actions.add(ACTION_ENABLE);
                    actions.add(ACTION_DISABLE_NOW);
                }
                else
                {
                    actions.add(ACTION_DISABLE);
                    actions.add(ACTION_CLEAN);
                }

                actions.add(ACTION_PING);
            }
            else if (agent.isDisabled())
            {
                actions.add(ACTION_ENABLE);
            }
        }

        return actions;
    }

    @Permission(ACTION_DISABLE)
    public void doDisable(AgentConfiguration config)
    {
        Agent agent = agentManager.getAgent(config);
        if (agent != null)
        {
            eventManager.publish(new AgentDisableRequestedEvent(this, agent));
        }
    }

    @Permission(ACTION_DISABLE)
    public void doDisableNow(AgentConfiguration config)
    {
        doDisable(config);
    }

    @Permission(ACTION_DISABLE)
    public void doEnable(AgentConfiguration config)
    {
        Agent agent = agentManager.getAgent(config);
        if (agent != null)
        {
            eventManager.publish(new AgentEnableRequestedEvent(this, agent));
        }
    }

    @Permission(ACTION_PING)
    public void doPing(AgentConfiguration config)
    {
        agentManager.pingAgent(config);
    }

    @Permission(AccessManager.ACTION_ADMINISTER)
    public void doClean(AgentConfiguration config)
    {
        Agent agent = agentManager.getAgent(config);
        if (agent != null)
        {
            final String deleteTaskType = SynchronisationTaskFactory.getTaskType(DeleteDirectoryTask.class);
            List<Pair<Properties, String>> propertiesDescriptionPairs = new LinkedList<Pair<Properties, String>>();

            Map<String, String> variables = new HashMap<String, String>();
            variables.put(PROPERTY_AGENT, encodeFilenameComponent(config.getName()));
            variables.put(PROPERTY_AGENT_HANDLE, Long.toString(config.getHandle()));

            for (String directory: Arrays.asList("recipes", "work"))
            {
                DeleteDirectoryTask deleteTask = new DeleteDirectoryTask(config.getDataDirectory(), "$(agent.data.dir)/" + directory, variables);
                SynchronisationMessage message = synchronisationTaskFactory.toMessage(deleteTask);
                propertiesDescriptionPairs.add(asPair(message.getArguments(), "clean up " + directory + " directory"));
            }

            agentManager.enqueueSynchronisationMessages(agent, deleteTaskType, propertiesDescriptionPairs);
        }
    }

    @Permission(AccessManager.ACTION_ADMINISTER)
    public void doRetryUpgrade(AgentConfiguration config)
    {
        Agent agent = agentManager.getAgent(config);
        if (agent != null)
        {
            eventManager.publish(new HostUpgradeRequestedEvent(this, agent.getHost()));
        }
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setSynchronisationTaskFactory(SynchronisationTaskFactory synchronisationTaskFactory)
    {
        this.synchronisationTaskFactory = synchronisationTaskFactory;
    }
}
