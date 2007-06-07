package com.zutubi.prototype.config.events;

import com.zutubi.prototype.config.ConfigurationTemplateManager;

/**
 */
public class PreDeleteEvent extends ConfigurationEvent
{
    private Object instance;

    public PreDeleteEvent(ConfigurationTemplateManager source, String path, Object instance)
    {
        super(source, path);
        this.instance = instance;
    }

    public Object getInstance()
    {
        return instance;
    }

    public String toString()
    {
        return "Pre Delete Event: " + getPath();
    }
}
