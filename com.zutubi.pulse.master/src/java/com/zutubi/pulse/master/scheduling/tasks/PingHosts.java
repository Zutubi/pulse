package com.zutubi.pulse.master.scheduling.tasks;

import com.zutubi.pulse.master.agent.HostManager;
import com.zutubi.pulse.master.scheduling.Task;
import com.zutubi.pulse.master.scheduling.TaskExecutionContext;
import com.zutubi.util.logging.Logger;

/**
 * Scheduler task that pings all hosts.
 */
public class PingHosts implements Task
{
    private static final Logger LOG = Logger.getLogger(PingHosts.class);

    private HostManager hostManager;

    public void execute(TaskExecutionContext context)
    {
        LOG.info("Pinging hosts");
        hostManager.pingHosts();
    }

    public void setHostManager(HostManager hostManager)
    {
        this.hostManager = hostManager;
    }
}