package com.zutubi.pulse.events;

/**
 * <class-comment/>
 */
public abstract class AllEventListener implements EventListener
{
    public Class[] getHandledEvents()
    {
        return new Class[]{Event.class};
    }
}
