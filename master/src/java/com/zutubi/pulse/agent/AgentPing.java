package com.zutubi.pulse.agent;

import com.zutubi.pulse.services.SlaveStatus;
import com.zutubi.pulse.services.SlaveService;
import com.zutubi.pulse.util.logging.Logger;
import com.zutubi.pulse.util.Pair;

import java.util.concurrent.Callable;
import java.net.ConnectException;

/**
 * A callable task that pings an agent and returns its status.
 */
class AgentPing implements Callable<SlaveStatus>
{
    private static final Logger LOG = Logger.getLogger(AgentPing.class);

    private Agent agent;
    private SlaveService service;
    private int masterBuildNumber;
    private String masterLocation;
    private String token;

    public AgentPing(Agent agent, SlaveService service, int masterBuildNumber, String masterLocation, String token)
    {
        this.agent = agent;
        this.service = service;
        this.masterBuildNumber = masterBuildNumber;
        this.masterLocation = masterLocation;
        this.token = token;
    }

    public SlaveStatus call()
    {
        SlaveStatus status;

        try
        {
            int build = service.ping();
            if (build == masterBuildNumber)
            {
                status = service.getStatus(token, masterLocation);
            }
            else
            {
                status = new SlaveStatus(Status.VERSION_MISMATCH);
            }
        }
        catch (Exception e)
        {
            LOG.debug(e);

            Throwable cause = e.getCause();
            // the most common cause of the exception is the Connect Exception.
            if (cause instanceof ConnectException)
            {
                status = new SlaveStatus(Status.OFFLINE, cause.getMessage());
            }
            else
            {
                LOG.warning("Exception pinging agent '" + agent.getName() + "': " + e.getMessage());
                status = new SlaveStatus(Status.OFFLINE, "Exception: '" + e.getClass().getName() + "'. Reason: " + e.getMessage());
            }
        }

        status.setPingTime(System.currentTimeMillis());
        return status;
    }
}
