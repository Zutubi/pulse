package com.zutubi.pulse.events.build;

import com.zutubi.pulse.events.Event;

/**
 * Raised to request that all running builds be forcefully terminated.
 */
public class BuildTerminationRequestEvent extends Event
{
    /**
     * ID of the build to terminate, or -1 to terminate all builds.
     */
    private long id;
    private boolean timeout;

    public BuildTerminationRequestEvent(Object source, long id, boolean timeout)
    {
        super(source);
        this.id = id;
        this.timeout = timeout;
    }

    public boolean isTimeout()
    {
        return timeout;
    }

    public long getId()
    {
        return id;
    }

    public String toString()
    {
        return "Build Termination Request Event: " + id;
    }
}
