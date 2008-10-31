package com.zutubi.pulse.master.events.build;

import com.zutubi.events.Event;

/**
 * Raised to request that all running builds be forcefully terminated.
 */
public class BuildTerminationRequestEvent extends Event
{
    /**
     * ID of the build to terminate, or -1 to terminate all builds.
     */
    private long buildId;
    
    private boolean timeout;

    public BuildTerminationRequestEvent(Object source, long buildId, boolean timeout)
    {
        super(source);
        this.buildId = buildId;
        this.timeout = timeout;
    }

    public boolean isTimeout()
    {
        return timeout;
    }

    public long getBuildId()
    {
        return buildId;
    }

    public String toString()
    {
        return String.format("Build Termination Request Event[buildId: %s]", buildId);
    }
}
