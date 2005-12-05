package com.cinnamonbob.scheduling;

import com.cinnamonbob.core.event.EventManager;
import com.cinnamonbob.core.event.EventListener;
import com.cinnamonbob.core.event.Event;

import java.util.Map;
import java.util.HashMap;

/**
 * <class-comment/>
 */
public class EventSchedulerImpl extends BaseSchedulerImpl
{
    private EventManager eventManager;
    private Map<Trigger, EventListener> triggerListenerMap = new HashMap<Trigger, EventListener>();
    private Map<Trigger, EventListener> pausedTriggerListenerMap = new HashMap<Trigger, EventListener>();

    public void schedule(final Trigger trigger, final Task task)
    {
        super.schedule(trigger, task);

        final EventTrigger eventTrigger = (EventTrigger)trigger;

        EventListener eventListener = new EventListener()
        {
            public Class[] getHandledEvents()
            {
                return eventTrigger.getTriggerEvents();
            }

            public void handleEvent(Event evt)
            {
                TaskExecutionContext context = new TaskExecutionContext();
                context.put("event", evt);
                trigger(trigger, task, context);
            }
        };
        triggerListenerMap.put(eventTrigger, eventListener);
        eventManager.register(eventListener);
    }

    public void unschedule(Trigger trigger)
    {
        super.unschedule(trigger);
        if (triggerListenerMap.containsKey(trigger))
        {
            EventListener listener = triggerListenerMap.remove(trigger);
            eventManager.unregister(listener);
        }
        else if (pausedTriggerListenerMap.containsKey(trigger))
        {
            pausedTriggerListenerMap.remove(trigger);
        }
    }

    public void pause(Trigger trigger)
    {
        super.pause(trigger);
        if (triggerListenerMap.containsKey(trigger))
        {
            EventListener listener = triggerListenerMap.remove(trigger);
            eventManager.unregister(listener);
            pausedTriggerListenerMap.put(trigger, listener);
        }
    }

    public void resume(Trigger trigger)
    {
        super.resume(trigger);
        if (pausedTriggerListenerMap.containsKey(trigger))
        {
            EventListener listener = pausedTriggerListenerMap.remove(trigger);
            eventManager.register(listener);
        }
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }
}
