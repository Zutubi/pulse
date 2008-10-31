package com.zutubi.pulse.servercore.events.system;

import com.zutubi.events.EventListener;
import com.zutubi.events.Event;

/**
 * A utility listener to simplify the registration of an event listener
 * to listen for the system started event.
 */
public abstract class SystemStartedListener implements EventListener
{
    public Class[] getHandledEvents()
    {
        return new Class[]{SystemStartedEvent.class};
    }

    public void handleEvent(Event event)
    {
        systemStarted();
    }

    public abstract void systemStarted();
}
