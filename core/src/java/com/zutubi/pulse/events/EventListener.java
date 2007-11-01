package com.zutubi.pulse.events;

/**
 * <class-comment/>
 */
public interface EventListener extends java.util.EventListener
{
    void handleEvent(Event event);

    Class[] getHandledEvents();
}
