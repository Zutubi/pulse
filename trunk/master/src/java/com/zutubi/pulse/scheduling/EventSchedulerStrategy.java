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

    private ObjectFactory objectFactory;

    public String canHandle()
    {
        return EventTrigger.TYPE;
    }

    public void init(Trigger trigger) throws SchedulingException
    {
        if(trigger.isActive())
        {
            register(trigger);
        }
    }

    public void schedule(final Trigger trigger) throws SchedulingException
    {
        register(trigger);
    }

    public void unschedule(Trigger trigger) throws SchedulingException
    {
        unregister(trigger, TriggerState.NONE);
    }

    public void pause(Trigger trigger) throws SchedulingException
    {
        unregister(trigger, TriggerState.PAUSED);
    }

    public void resume(Trigger trigger) throws SchedulingException
    {
        register(trigger);
    }

    public void stop(boolean force)
    {
        for (EventListener listener : activeListenerMap.values())
        {
            eventManager.unregister(listener);
        }
    }

    private void register(Trigger trigger)
    {
        final EventTrigger eventTrigger = (EventTrigger) trigger;

        EventListener eventListener = new EventTriggerListener(eventTrigger, trigger);

        activeListenerMap.put(eventTrigger.getId(), eventListener);
        eventManager.register(eventListener);
        trigger.setState(TriggerState.SCHEDULED);
    }

    private void unregister(Trigger trigger, TriggerState newState)
    {
        if (activeListenerMap.containsKey(trigger.getId()))
        {
            EventListener listener = activeListenerMap.remove(trigger.getId());
            eventManager.unregister(listener);
            trigger.setState(newState);
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

    public boolean dependsOnProject(Trigger trigger, long projectId)
    {
        Class filterClass = ((EventTrigger)trigger).getFilterClass();
        if(filterClass != null)
        {
            try
            {
                EventTriggerFilter filter = objectFactory.buildBean(filterClass);
                return filter.dependsOnProject(trigger, projectId);
            }
            catch (Exception e)
            {
                LOG.severe("Unable to construct event filter of type '" + filterClass.getName() + "': " + e.getMessage(), e);
            }
        }
        
        return false;
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

    private class EventTriggerListener implements EventListener
    {
        private final EventTrigger eventTrigger;
        private final Trigger trigger;

        public EventTriggerListener(EventTrigger eventTrigger, Trigger trigger)
        {
            this.eventTrigger = eventTrigger;
            this.trigger = trigger;
        }

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
    }
}
