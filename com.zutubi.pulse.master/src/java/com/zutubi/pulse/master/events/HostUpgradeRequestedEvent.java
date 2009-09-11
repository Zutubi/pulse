package com.zutubi.pulse.master.events;

import com.zutubi.pulse.master.agent.Host;

/**
 * An event raised to request that a host upgrades.
 */
public class HostUpgradeRequestedEvent extends HostEvent
{
    public HostUpgradeRequestedEvent(Object source, Host host)
    {
        super(source, host);
    }

    @Override
    public String toString()
    {
        return "Host Upgrade Requested Event: " + getHost().getLocation();
    }
}