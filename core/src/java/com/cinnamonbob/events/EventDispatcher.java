package com.cinnamonbob.events;

import com.cinnamonbob.events.Event;

import java.util.List;

/**
 * <class-comment/>
 */
public interface EventDispatcher
{
    void dispatch(Event evt, List<EventListener> listeners);
}
