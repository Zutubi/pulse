package com.zutubi.pulse.master.events;

import com.zutubi.pulse.master.agent.Host;

/**
 * Event raised with a host upgrade completes, indicating if it succeeded.
 */
public class HostUpgradeCompleteEvent extends HostEvent
{
    private boolean successful;

    public HostUpgradeCompleteEvent(Object source, Host host, boolean successful)
    {
        super(source, host);
        this.successful = successful;
    }

    public boolean isSuccessful()
    {
        return successful;
    }

    @Override
    public String toString()
    {
        return "Host Upgrade Complete Event: " + getHost().getLocation() + ": " + successful;
    }
}
