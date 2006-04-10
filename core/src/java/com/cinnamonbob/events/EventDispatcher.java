package com.zutubi.pulse.events;

import com.zutubi.pulse.events.Event;

import java.util.List;

/**
 * <class-comment/>
 */
public interface EventDispatcher
{
    void dispatch(Event evt, List<EventListener> listeners);
}
