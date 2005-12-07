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

    public void schedule(final Trigger trigger, final Task task) throws SchedulingException
    {
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
                try
                {
                    trigger(trigger, task, context);
                }
                catch (SchedulingException e)
                {
                    e.printStackTrace();
                }
            }
        };
        triggerListenerMap.put(eventTrigger, eventListener);
        eventManager.register(eventListener);
        trigger.setState(TriggerState.ACTIVE);
    }

    public void unschedule(Trigger trigger) throws SchedulingException
    {
        if (triggerListenerMap.containsKey(trigger))
        {
            EventListener listener = triggerListenerMap.remove(trigger);
            eventManager.unregister(listener);
            trigger.setState(TriggerState.NONE);
        }
        else if (pausedTriggerListenerMap.containsKey(trigger))
        {
            pausedTriggerListenerMap.remove(trigger);
            trigger.setState(TriggerState.NONE);
        }
    }

    public void pause(Trigger trigger) throws SchedulingException
    {
        if (triggerListenerMap.containsKey(trigger))
        {
            EventListener listener = triggerListenerMap.remove(trigger);
            eventManager.unregister(listener);
            pausedTriggerListenerMap.put(trigger, listener);
            trigger.setState(TriggerState.PAUSED);
        }
    }

    public void resume(Trigger trigger) throws SchedulingException
    {
        if (pausedTriggerListenerMap.containsKey(trigger))
        {
            EventListener listener = pausedTriggerListenerMap.remove(trigger);
            eventManager.register(listener);
            triggerListenerMap.put(trigger, listener);
            trigger.setState(TriggerState.ACTIVE);
        }
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }
}
