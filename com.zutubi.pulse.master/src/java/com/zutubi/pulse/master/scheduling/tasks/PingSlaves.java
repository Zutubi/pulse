package com.zutubi.pulse.master.scheduling.tasks;

import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.scheduling.Task;
import com.zutubi.pulse.master.scheduling.TaskExecutionContext;
import com.zutubi.util.logging.Logger;

/**
 * <class-comment/>
 */
public class PingSlaves implements Task
{
    private static final Logger LOG = Logger.getLogger(PingSlaves.class);

    private AgentManager agentManager;

    public void execute(TaskExecutionContext context)
    {
        LOG.info("pinging slaves.");
        agentManager.pingAgents();
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }
}