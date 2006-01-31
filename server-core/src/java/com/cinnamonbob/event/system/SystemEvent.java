package com.cinnamonbob.event.system;

import com.cinnamonbob.events.Event;

/**
 * Parent for all system events.  These are events that indicate a change in
 * the server state.
 */
public abstract class SystemEvent extends Event
{
    public SystemEvent(Object source)
    {
        super(source);
    }
}
