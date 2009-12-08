package com.zutubi.pulse.master.agent.statistics;

import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.scheduling.Task;
import com.zutubi.pulse.master.scheduling.TaskExecutionContext;

/**
 * Scheduling task that pokes the agent manager to make it update agent
 * statistics.
 */
public class UpdateStatisticsTask implements Task
{
    private AgentManager agentManager;

    public void execute(TaskExecutionContext context)
    {
        agentManager.updateStatistics();
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }
}
