package com.zutubi.events;

/**
 * The base interface implemented by event managers.  Event managers support registering listeners
 * and publishing events to those listeners.
 */
public interface EventManager
{
    /**
     * Register an event listener so that it is notified on the occurrence of specific events.
     *
     * @param eventListener listener to register
     */
    void register(EventListener eventListener);

    /**
     * Unregister an event listener so that it is no longer notified of events.
     *
     * @param eventListener listener to unregister
     */
    void unregister(EventListener eventListener);

    /**
     * Publish an event, notifying the appropriate event listeners.  Equivalent to publish(event,
     * PublishFlag.IMMEDIATE).
     *
     * @see PublishFlag
     * @see #publish(Event,PublishFlag)
     *
     * @param event the event being published
     */
    void publish(Event event);

    /**
     * Publish an event, notifying the appropriate event listeners.

     * @param event the event to publish
     * @param how   how the event should be published, see the enumeration types for details
     */
    void publish(Event event, PublishFlag how);
}
