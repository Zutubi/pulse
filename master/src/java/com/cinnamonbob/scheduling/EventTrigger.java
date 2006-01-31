package com.cinnamonbob.scheduling;

import com.cinnamonbob.events.Event;

/**
 * The EventTrigger is triggered by the occurance of an event within the system.
 * Which event will trigger the event trigger is defined by the triggerEvents property.
 *
 */
public class EventTrigger extends Trigger
{
    private static final Class<Event>[] DEFAULT_TRIGGER_EVENTS = new Class[]{Event.class};

    private Class<Event>[] triggers = DEFAULT_TRIGGER_EVENTS;

    static final String TYPE = "event";

    /**
     * Default no argument constructor required by hibernate. 
     */
    public EventTrigger()
    {

    }

    public EventTrigger(Class trigger)
    {
        this(trigger, null);
    }

    public EventTrigger(Class trigger, String name)
    {
        this(trigger, name, DEFAULT_GROUP);
    }

    public EventTrigger(Class trigger, String name, String group)
    {
        super(name, group);
        triggers = new Class[]{trigger};
    }

    public String getType()
    {
        return TYPE;
    }

    /**
     * Get the array of Event classes that will trigger this event trigger.
     *
     * @return the array of event classes.
     */
    public Class<Event>[] getTriggerEvents()
    {
        return triggers;
    }

    private Class getTriggerEvent()
    {
        return getTriggerEvents()[0];
    }

    private void setTriggerEvent(Class event)
    {
        getTriggerEvents()[0] = event;
    }
}
