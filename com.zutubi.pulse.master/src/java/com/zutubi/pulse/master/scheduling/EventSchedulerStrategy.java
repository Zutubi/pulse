/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.scheduling;

import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

    public List<String> canHandle()
    {
        return Arrays.asList(EventTrigger.TYPE);
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
        unregister(trigger);
    }

    public void pause(Trigger trigger) throws SchedulingException
    {
        unregister(trigger);
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
    }

    private void unregister(Trigger trigger)
    {
        if (activeListenerMap.containsKey(trigger.getId()))
        {
            EventListener listener = activeListenerMap.remove(trigger.getId());
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
                TaskExecutionContext context = new TaskExecutionContext();
                context.setTrigger(trigger);

                Class<? extends EventTriggerFilter> filterClass = eventTrigger.getFilterClass();

                if (filterClass != null)
                {
                    try
                    {
                        EventTriggerFilter filter = objectFactory.buildBean(filterClass);
                        accept = filter.accept(eventTrigger, evt, context);
                    }
                    catch (Exception e)
                    {
                        LOG.severe("Unable to construct event filter of type '" + filterClass.getName() + "': " + e.getMessage(), e);
                        accept = false;
                    }
                }

                if (accept)
                {
                    triggerHandler.fire(trigger, context);
                }
            }
            catch (SchedulingException e)
            {
                LOG.severe(e);
            }
        }
    }
}
