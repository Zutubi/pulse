package com.zutubi.pulse.master.agent;

import com.zutubi.pulse.servercore.agent.PingStatus;
import com.zutubi.pulse.servercore.services.SlaveStatus;
import com.zutubi.util.logging.Logger;

import java.net.ConnectException;
import java.util.concurrent.Callable;

/**
 * A callable task that pings an agent and returns its status.
 */
class AgentPing implements Callable<SlaveStatus>
{
    private static final Logger LOG = Logger.getLogger(AgentPing.class);

    private Agent agent;
    private AgentService service;
    private int masterBuildNumber;
    private String masterLocation;

    public AgentPing(Agent agent, AgentService service, int masterBuildNumber, String masterLocation)
    {
        this.agent = agent;
        this.service = service;
        this.masterBuildNumber = masterBuildNumber;
        this.masterLocation = masterLocation;
    }

    public SlaveStatus call()
    {
        SlaveStatus status;

        try
        {
            int build = service.ping();
            if (build == masterBuildNumber)
            {
                status = service.getStatus(masterLocation);
            }
            else
            {
                status = new SlaveStatus(PingStatus.VERSION_MISMATCH);
            }
        }
        catch (Exception e)
        {
            LOG.debug(e);

            Throwable cause = e.getCause();
            // the most common cause of the exception is the Connect Exception.
            if (cause instanceof ConnectException)
            {
                status = new SlaveStatus(PingStatus.OFFLINE, cause.getMessage());
            }
            else
            {
                LOG.warning("Exception pinging agent '" + agent.getConfig().getName() + "': " + e.getMessage());
                status = new SlaveStatus(PingStatus.OFFLINE, "Exception: '" + e.getClass().getName() + "'. Reason: " + e.getMessage());
            }
        }

        return status;
    }
}
