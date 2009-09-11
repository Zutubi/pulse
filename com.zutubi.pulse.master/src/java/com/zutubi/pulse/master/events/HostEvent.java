package com.zutubi.pulse.master.events;

import com.zutubi.events.Event;
import com.zutubi.pulse.master.agent.Host;

/**
 * Base for all events related to hosts.
 */
public class HostEvent extends Event
{
    private Host host;

    public HostEvent(Object source, Host host)
    {
        super(source);
        this.host = host;
    }

    public Host getHost()
    {
        return host;
    }
}