package com.zutubi.events;

/**
 * Extend this listener if you are interested in being notified about all generated events.
 */
public abstract class AllEventListener implements EventListener
{
    public final Class[] getHandledEvents()
    {
        return new Class[]{ Event.class };
    }
}
