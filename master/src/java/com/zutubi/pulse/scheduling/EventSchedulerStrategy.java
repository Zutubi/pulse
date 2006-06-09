package com.zutubi.pulse.scheduling;

import com.zutubi.pulse.core.ObjectFactory;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.util.logging.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * <class-comment/>
 */
public class EventSchedulerStrategy implements SchedulerStrategy
{
    private static final Logger LOG = Logger.getLogger(EventSchedulerStrategy.class);

    private EventManager eventManager;

    private TriggerHandler triggerHandler;

    private Map<Long, EventListener> activeListenerMap = new HashMap<Long, EventListener>();

    private Map<Long, EventListener> pausedListenerMap = new HashMap<Long, EventListener>();
    private ObjectFactory objectFactory;

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
                    boolean accept = true;
                    Class<? extends EventTriggerFilter> filterClass = eventTrigger.getFilterClass();

                    if(filterClass != null)
                    {
                        try
                        {
                            EventTriggerFilter filter = objectFactory.buildBean(filterClass);
                            accept = filter.accept(eventTrigger, evt);
                        }
                        catch (Exception e)
                        {
                            LOG.severe("Unable to construct event filter of type '" + filterClass.getName() + "': " + e.getMessage(), e);
                        }
                    }

                    if(accept)
                    {
                        triggerHandler.fire(trigger);
                    }
                }
                catch (SchedulingException e)
                {
                    LOG.severe(e);
                }
            }
        };
        
        activeListenerMap.put(eventTrigger.getId(), eventListener);
        eventManager.register(eventListener);
        trigger.setState(TriggerState.SCHEDULED);
    }

    public void unschedule(Trigger trigger) throws SchedulingException
    {
        if (activeListenerMap.containsKey(trigger.getId()))
        {
            EventListener listener = activeListenerMap.remove(trigger.getId());
            eventManager.unregister(listener);
            trigger.setState(TriggerState.NONE);
        }
        else if (pausedListenerMap.containsKey(trigger.getId()))
        {
            pausedListenerMap.remove(trigger.getId());
            trigger.setState(TriggerState.NONE);
        }
    }

    public void pause(Trigger trigger) throws SchedulingException
    {
        if (activeListenerMap.containsKey(trigger.getId()))
        {
            EventListener listener = activeListenerMap.remove(trigger.getId());
            eventManager.unregister(listener);
            pausedListenerMap.put(trigger.getId(), listener);
            trigger.setState(TriggerState.PAUSED);
        }
    }

    public void resume(Trigger trigger) throws SchedulingException
    {
        if (pausedListenerMap.containsKey(trigger.getId()))
        {
            EventListener listener = pausedListenerMap.remove(trigger.getId());
            eventManager.register(listener);
            activeListenerMap.put(trigger.getId(), listener);
            trigger.setState(TriggerState.SCHEDULED);
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

    /**
     * Required resource.
     *
     * @param eventManager
     */
    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    /**
     * Required resource.
     *
     * @param triggerHandler
     */
    public void setTriggerHandler(TriggerHandler triggerHandler)
    {
        this.triggerHandler = triggerHandler;
    }

    /**
     * Required resource.
     * 
     * @param objectFactory
     */
    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
