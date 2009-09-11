package com.zutubi.pulse.master.events;

import com.zutubi.pulse.master.agent.Host;
import com.zutubi.pulse.servercore.services.HostStatus;

/**
 * Event raised when a host ping completes.
 */
public class HostPingEvent extends HostEvent
{
    private HostStatus hostStatus;

    public HostPingEvent(Object source, Host host, HostStatus hostStatus)
    {
        super(source, host);
        this.hostStatus = hostStatus;
    }

    public HostStatus getHostStatus()
    {
        return hostStatus;
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder("Host Ping Event");
        if (getHost() != null)
        {
            builder.append(": ").append(getHost().getLocation()).append(", ").append(hostStatus);
        }
        return builder.toString();
    }
}
