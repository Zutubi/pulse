package com.zutubi.prototype.config.events;

import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.pulse.core.config.Configuration;

/**
 */
public class SaveEvent extends ConfigurationEvent
{
    public SaveEvent(ConfigurationTemplateManager source, Configuration newInstance)
    {
        super(source, newInstance);
    }

    public boolean isPost()
    {
        return false;
    }

    public String toString()
    {
        return "Save Event: " + getInstance().getConfigurationPath();
    }
}
