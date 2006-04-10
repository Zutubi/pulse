package com.zutubi.pulse.events.build;

import com.zutubi.pulse.events.Event;

/**
 */
public class BuildTimeoutEvent extends Event
{
    long buildId;

    public BuildTimeoutEvent(Object source, long id)
    {
        super(source);
        this.buildId = id;
    }

    public long getBuildId()
    {
        return buildId;
    }
}
