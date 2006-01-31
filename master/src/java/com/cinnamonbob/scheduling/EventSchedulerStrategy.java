package com.cinnamonbob.scheduling;

import com.cinnamonbob.events.EventManager;
import com.cinnamonbob.events.Event;
import com.cinnamonbob.events.EventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * <class-comment/>
 */
public class EventSchedulerStrategy implements SchedulerStrategy
{
    private EventManager eventManager;

    private TriggerHandler triggerHandler;

    private Map<Trigger, EventListener> activeListenerMap = new HashMap<Trigger, EventListener>();

    private Map<Trigger, EventListener> pausedListenerMap = new HashMap<Trigger, EventListener>();

    public String canHandle()
    {
        return EventTrigger.TYPE;
    }

    public void schedule(final Trigger trigger) throws SchedulingException
    {
        final EventTrigger eventTrigger = (EventTrigger) trigger;

        EventListener eventListener = new EventListener()
        {
            public Class[] getHandledEvents()
            {
                return eventTrigger.getTriggerEvents();
            }

            public void handleEvent(Event evt)
            {
                try
                {
                    triggerHandler.trigger(trigger);
                }
                catch (SchedulingException e)
                {
                    e.printStackTrace();
                }
            }
        };
        activeListenerMap.put(eventTrigger, eventListener);
        eventManager.register(eventListener);
        trigger.setState(TriggerState.ACTIVE);
    }

    public void unschedule(Trigger trigger) throws SchedulingException
    {
        if (activeListenerMap.containsKey(trigger))
        {
            EventListener listener = activeListenerMap.remove(trigger);
            eventManager.unregister(listener);
            trigger.setState(TriggerState.NONE);
        }
        else if (pausedListenerMap.containsKey(trigger))
        {
            pausedListenerMap.remove(trigger);
            trigger.setState(TriggerState.NONE);
        }
    }

    public void pause(Trigger trigger) throws SchedulingException
    {
        if (activeListenerMap.containsKey(trigger))
        {
            EventListener listener = activeListenerMap.remove(trigger);
            eventManager.unregister(listener);
            pausedListenerMap.put(trigger, listener);
            trigger.setState(TriggerState.PAUSED);
        }
    }

    public void resume(Trigger trigger) throws SchedulingException
    {
        if (pausedListenerMap.containsKey(trigger))
        {
            EventListener listener = pausedListenerMap.remove(trigger);
            eventManager.register(listener);
            activeListenerMap.put(trigger, listener);
            trigger.setState(TriggerState.ACTIVE);
        }
    }

    public void stop(boolean force)
    {
        for (EventListener listener : activeListenerMap.values())
        {
            eventManager.unregister(listener);
        }

        for (EventListener listener : pausedListenerMap.values())
        {
            eventManager.unregister(listener);
        }
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setTriggerHandler(TriggerHandler triggerHandler)
    {
        this.triggerHandler = triggerHandler;
    }
}
