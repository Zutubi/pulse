package com.cinnamonbob.events;

/**
 * <class-comment/>
 */
public interface EventListener extends java.util.EventListener
{
    void handleEvent(Event evt);

    Class[] getHandledEvents();
}
