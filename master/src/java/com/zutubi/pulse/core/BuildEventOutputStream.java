package com.zutubi.pulse.core;

import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.build.CommandOutputEvent;
import com.zutubi.pulse.events.build.BuildOutputEvent;

/**
 *
 *
 */
public class BuildEventOutputStream extends EventOutputStream
{
    private EventManager eventManager;

    public BuildEventOutputStream(EventManager eventManager, boolean autoflush)
    {
        super(autoflush);
        this.eventManager = eventManager;
    }

    protected void sendEvent(byte[] sendBuffer)
    {
        BuildOutputEvent event = new BuildOutputEvent(this, sendBuffer);
        eventManager.publish(event);
        offset = 0;
    }
}
