package com.cinnamonbob.events.build;

import com.cinnamonbob.events.Event;

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
