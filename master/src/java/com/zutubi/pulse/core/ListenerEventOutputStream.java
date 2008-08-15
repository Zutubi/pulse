package com.zutubi.pulse.core;

import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.events.build.BuildOutputEvent;

/**
 *
 *
 */
public class ListenerEventOutputStream extends EventOutputStream
{
    private EventListener listener;

    public ListenerEventOutputStream(EventListener listener, boolean autoflush)
    {
        super(autoflush);
        this.listener = listener;
    }

    protected void sendEvent(byte[] sendBuffer)
    {
        BuildOutputEvent event = new BuildOutputEvent(this, sendBuffer);
        listener.handleEvent(event);
        offset = 0;
    }
}
