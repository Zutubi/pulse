package com.zutubi.pulse.master.agent;

import com.zutubi.pulse.servercore.agent.PingStatus;
import com.zutubi.pulse.servercore.services.HostStatus;
import com.zutubi.util.logging.Logger;

import java.net.ConnectException;
import java.util.concurrent.Callable;

/**
 * A callable task that pings a host and returns its status.
 */
class HostPing implements Callable<HostStatus>
{
    private static final Logger LOG = Logger.getLogger(HostPing.class);

    private Host host;
    private HostService service;
    private int masterBuildNumber;
    private String masterLocation;

    public HostPing(Host host, HostService service, int masterBuildNumber, String masterLocation)
    {
        this.host = host;
        this.service = service;
        this.masterBuildNumber = masterBuildNumber;
        this.masterLocation = masterLocation;
    }

    public HostStatus call()
    {
        HostStatus status;

        try
        {
            int build = service.ping();
            if (build == masterBuildNumber)
            {
                status = service.getStatus(masterLocation);
            }
            else
            {
                status = new HostStatus(PingStatus.VERSION_MISMATCH);
            }
        }
        catch (Exception e)
        {
            LOG.debug(e);

            Throwable cause = e.getCause();
            // the most common cause of the exception is the Connect Exception.
            if (cause instanceof ConnectException)
            {
                status = new HostStatus(PingStatus.OFFLINE, cause.getMessage());
            }
            else
            {
                LOG.warning("Exception pinging host '" + host.getLocation() + "': " + e.getMessage());
                status = new HostStatus(PingStatus.OFFLINE, "Exception: '" + e.getClass().getName() + "'. Reason: " + e.getMessage());
            }
        }

        return status;
    }
}
