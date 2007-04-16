package com.zutubi.prototype.config;

import com.zutubi.pulse.events.EventManager;
import com.zutubi.prototype.config.events.*;

/**
 * An adaptation between the tightly-coupled ConfigurationListener interface
 * and the standard Pulse event system.  This class listens for configuration
 * changes and publishes corresponding events.
 */
public class EventPublishingConfigurationListener implements ConfigurationListener
{
    private EventManager eventManager;

    public EventPublishingConfigurationListener(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void preInsert(String path)
    {
        eventManager.publish(new PreInsertEvent(this, path));
    }

    public void postInsert(String path, String insertedPath, Object newInstance)
    {
        eventManager.publish(new PostInsertEvent(this, path, insertedPath, newInstance));
    }

    public void preSave(String path, Object oldInstance)
    {
        eventManager.publish(new PreSaveEvent(this, path, oldInstance));
    }

    public void postSave(String path, Object oldInstance, String newPath, Object newInstance)
    {
        eventManager.publish(new PostSaveEvent(this, path, oldInstance, newPath, newInstance));
    }

    public void preDelete(String path, Object instance)
    {
        eventManager.publish(new PreDeleteEvent(this, path, instance));
    }

    public void postDelete(String path, Object oldInstance)
    {
        eventManager.publish(new PostDeleteEvent(this, path, oldInstance));
    }
}
