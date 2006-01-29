package com.cinnamonbob.events.build;

import com.cinnamonbob.core.event.Event;

/**
 * Raised to request that all running builds be forcefully terminated.
 */
public class BuildTerminationRequestEvent extends Event
{
    public BuildTerminationRequestEvent(Object source)
    {
        super(source);
    }
}
