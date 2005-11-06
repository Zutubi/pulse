package com.cinnamonbob.schedule.triggers;

import com.cinnamonbob.core.event.Event;
import com.cinnamonbob.core.event.EventListener;
import com.cinnamonbob.core.event.EventManager;
import com.cinnamonbob.schedule.SchedulingException;

/**
 * The event trigger is triggered by the occurance of an Event.
 */
public abstract class EventTrigger extends Trigger implements EventListener
{
    private EventManager eventManager;

    private EventTriggerEventListener proxyListener;

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void handleEvent(Event evt)
    {
        trigger();
    }

    public void trigger()
    {
        super.trigger();
    }

    public void rehydrate() throws SchedulingException
    {
        if (isActive())
        {
            internalActivate();
        }
    }

    public void internalActivate() throws SchedulingException
    {
        eventManager.register(getProxyListener());
    }

    public void internalComplete() throws SchedulingException
    {
        eventManager.unregister(getProxyListener());
    }

    public void internalPause() throws SchedulingException
    {
        eventManager.unregister(getProxyListener());
    }

    public void internalResume() throws SchedulingException
    {
        eventManager.register(getProxyListener());
    }

    private EventListener getProxyListener()
    {
        if (proxyListener == null)
        {
            proxyListener = new EventTriggerEventListener(this);
        }
        return proxyListener;
    }
}

class EventTriggerEventListener implements EventListener
{
    private final EventTrigger trigger;

    public EventTriggerEventListener(EventTrigger trigger)
    {
        this.trigger = trigger;
    }

    public Class[] getHandledEvents()
    {
        return trigger.getHandledEvents();
    }

    public void handleEvent(Event evt)
    {

        trigger.handleEvent(evt);
    }

    // Need to override equals and hashcode so that we can correctly register and
    // unregister different instances of the event listener for a specific trigger
    // instance.

    public boolean equals(Object other)
    {
        return trigger.equals(other);
    }

    public int hashcode()
    {
        return trigger.hashCode();
    }
}