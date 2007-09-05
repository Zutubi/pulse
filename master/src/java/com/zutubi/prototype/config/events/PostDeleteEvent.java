package com.zutubi.prototype.config.events;

import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.pulse.core.config.Configuration;

/**
 */
public class PostDeleteEvent extends ConfigurationEvent
{
    private boolean cascaded;

    public PostDeleteEvent(ConfigurationTemplateManager source, Configuration oldInstance, boolean cascaded)
    {
        super(source, oldInstance);
        this.cascaded = cascaded;
    }

    public boolean isCascaded()
    {
        return cascaded;
    }

    public String toString()
    {
        return "Post Delete Event: " + getInstance().getConfigurationPath();
    }
}
