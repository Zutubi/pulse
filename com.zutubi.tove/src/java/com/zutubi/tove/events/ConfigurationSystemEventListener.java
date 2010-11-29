package com.zutubi.tove.events;

import com.zutubi.events.Event;
import com.zutubi.events.EventListener;

/**
 * A base listener implementation for configuration system events.  
 */
public abstract class ConfigurationSystemEventListener implements EventListener
{
    public Class[] getHandledEvents()
    {
        return new Class[]{ConfigurationSystemEvent.class};
    }

    public void handleEvent(Event event)
    {
        if (event instanceof ConfigurationEventSystemStartedEvent)
        {
            configurationEventSystemStarted();
        }
        else if (event instanceof ConfigurationSystemStartedEvent)
        {
            configurationSystemStarted();
        }

        configurationSystemEvent();
    }

    public void configurationEventSystemStarted()
    {

    }

    public void configurationSystemStarted()
    {
        
    }

    public void configurationSystemEvent()
    {

    }
}
