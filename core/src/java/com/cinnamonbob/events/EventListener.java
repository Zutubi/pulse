package com.cinnamonbob.events;

import com.cinnamonbob.events.Event;

/**
 * <class-comment/>
 */
public interface EventListener extends java.util.EventListener
{
    void handleEvent(Event evt);

    Class[] getHandledEvents();
}
