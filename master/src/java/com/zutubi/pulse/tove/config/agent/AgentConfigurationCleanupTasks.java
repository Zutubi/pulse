package com.zutubi.pulse.tove.config.agent;

import com.zutubi.pulse.model.AgentStateManager;
import com.zutubi.tove.config.cleanup.RecordCleanupTask;

import java.util.Arrays;
import java.util.List;

/**
 * Adds a custom cleanup task for agent configuration that deletes the
 * agent state.
 */
public class AgentConfigurationCleanupTasks
{
    private AgentStateManager agentStateManager;

    public List<RecordCleanupTask> getTasks(AgentConfiguration instance)
    {
        return Arrays.<RecordCleanupTask>asList(new AgentStateCleanupTask(instance, agentStateManager));
    }

    public void setAgentStateManager(AgentStateManager agentStateManager)
    {
        this.agentStateManager = agentStateManager;
    }
}
