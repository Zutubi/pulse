package com.zutubi.tove.events;

import com.zutubi.events.Event;

public class ConfigurationSystemEvent extends Event
{
    public ConfigurationSystemEvent(Object source)
    {
        super(source);
    }

    public String toString()
    {
        return "System Event";
    }
}
