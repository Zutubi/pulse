package com.cinnamonbob.event;

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
