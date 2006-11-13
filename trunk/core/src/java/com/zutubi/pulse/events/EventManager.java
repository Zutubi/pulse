package com.zutubi.pulse.events;

import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventListener;

/**
 *
 * 
 */
public interface EventManager
{
    void register(EventListener eventListener);

    void unregister(EventListener eventListener);

    void publish(Event evt);
}
