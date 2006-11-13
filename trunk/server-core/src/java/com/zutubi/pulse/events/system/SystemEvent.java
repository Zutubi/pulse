package com.zutubi.pulse.events.system;

import com.zutubi.pulse.events.Event;

/**
 * Parent for all system events.  These are events that indicate a change in
 * the server state.
 */
public abstract class SystemEvent extends Event<Object>
{
    public SystemEvent(Object source)
    {
        super(source);
    }

    public String toString()
    {
        return "System Event";
    }
}
