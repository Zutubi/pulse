package com.cinnamonbob.event;

/**
 * <class-comment/>
 */
public interface EventListener extends java.util.EventListener
{
    void handleEvent(Event evt);

    Class[] getHandledEvents();
}
