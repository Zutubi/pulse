/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.events.build;

import com.zutubi.pulse.events.Event;

/**
 * Raised to request that all running builds be forcefully terminated.
 */
public class BuildTerminationRequestEvent extends Event
{
    boolean timeout;

    public BuildTerminationRequestEvent(Object source, boolean timeout)
    {
        super(source);
        this.timeout = timeout;
    }

    public boolean isTimeout()
    {
        return timeout;
    }
}
