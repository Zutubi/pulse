package com.zutubi.pulse.events.system;

/**
 * An event raised just after the system has started.  Used to kick off the
 * third stage of startup: all subsytems that require the system to be ready
 * before starting themselves should handle this event.
 */
public class SystemStartedEvent extends SystemEvent
{
    public SystemStartedEvent(Object source)
    {
        super(source);
    }

    public String toString()
    {
        return "System Started Event";
    }
}
