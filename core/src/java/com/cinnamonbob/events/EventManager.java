package com.cinnamonbob.events;

import com.cinnamonbob.events.Event;
import com.cinnamonbob.events.EventListener;

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
