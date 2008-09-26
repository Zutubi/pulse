package com.zutubi.pulse.scheduling;

import com.zutubi.pulse.events.Event;

/**
 * The EventTrigger is triggered by the occurance of an event within the system.
 * Which event will trigger the event trigger is defined by the triggerEvents property.
 */
public class EventTrigger extends Trigger
{
    static final String TYPE = "event";

    // Here we specify Event.class directly, making the cast safe
    @SuppressWarnings("unchecked")
    private static final Class<? extends Event>[] DEFAULT_TRIGGER_EVENTS = (Class<? extends Event>[])new Class[]{Event.class};

    private Class<? extends Event>[] triggers = DEFAULT_TRIGGER_EVENTS;
    private Class<? extends EventTriggerFilter> filterClass = null;

    /**
     * Default no argument constructor required by hibernate.
     */
    public EventTrigger()
    {

    }

    public EventTrigger(Class<? extends Event> trigger)
    {
        this(trigger, null);
    }

    public EventTrigger(Class<? extends Event> trigger, String name)
    {
        this(trigger, name, DEFAULT_GROUP);
    }

    public EventTrigger(Class<? extends Event> trigger, String name, Class<? extends EventTriggerFilter> filterClass)
    {
        this(trigger, name, DEFAULT_GROUP);
        this.filterClass = filterClass;
    }

    // This method checks the trigger class type.
    @SuppressWarnings("unchecked")
    public EventTrigger(Class<? extends Event> trigger, String name, String group)
    {
        super(name, group);
        triggers = (Class<? extends Event>[]) new Class[]{trigger};
    }

    // This method checks the trigger class type.
    @SuppressWarnings("unchecked")
    public EventTrigger(Class<? extends Event> trigger, String name, String group, Class<? extends EventTriggerFilter> filterClass)
    {
        super(name, group);
        triggers = (Class<? extends Event>[]) new Class[]{trigger};
        this.filterClass = filterClass;
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
    public Class<? extends Event>[] getTriggerEvents()
    {
        return triggers;
    }

    @SuppressWarnings({"UnusedDeclaration"}) // Used by hibernate
    private Class<? extends Event> getTriggerEvent()
    {
        return getTriggerEvents()[0];
    }

    // This method checks the trigger class type, and is used by hibernate
    @SuppressWarnings({"unchecked", "UnusedDeclaration"})
    private void setTriggerEvent(Class<? extends Event> event)
    {
        triggers = (Class<? extends Event>[]) new Class[]{ event };
    }

    public Class<? extends EventTriggerFilter> getFilterClass()
    {
        return filterClass;
    }

    public void setFilterClass(Class<? extends EventTriggerFilter> filterClass)
    {
        this.filterClass = filterClass;
    }

    public boolean canEdit()
    {
        return true;
    }
}
