package com.cinnamonbob.scheduling;

import com.cinnamonbob.core.event.Event;

/**
 * <class-comment/>
 */
public class EventTrigger extends Trigger
{
    private static final Class[] DEFAULT_TRIGGER_EVENTS = new Class[]{Event.class};

    private Class[] triggers = DEFAULT_TRIGGER_EVENTS;

    public EventTrigger()
    {

    }

    public EventTrigger(Class trigger)
    {
        this(trigger, null);
    }

    public EventTrigger(Class trigger, String name)
    {
        this(trigger, name, null);
    }

    public EventTrigger(Class trigger, String name, String group)
    {
        super(name, group);
        triggers = new Class[]{trigger};
    }

    public Class[] getTriggerEvents()
    {
        return triggers;
    }
}
