package com.zutubi.prototype.config.events;

import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.pulse.core.config.Configuration;

/**
 */
public class PostSaveEvent extends ConfigurationEvent
{
    public PostSaveEvent(ConfigurationTemplateManager source, Configuration newInstance)
    {
        super(source, newInstance);
    }

    public String toString()
    {
        return "Post Save Event: " + getInstance().getConfigurationPath();
    }
}
