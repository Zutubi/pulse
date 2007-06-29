package com.zutubi.prototype.config.events;

import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.pulse.core.config.Configuration;

/**
 */
public class PostInsertEvent extends ConfigurationEvent
{
    private boolean cascaded;

    public PostInsertEvent(ConfigurationTemplateManager source, Configuration newInstance, boolean cascaded)
    {
        super(source, newInstance);
        this.cascaded = cascaded;
    }

    public boolean isCascaded()
    {
        return cascaded;
    }

    public String toString()
    {
        return "Post Insert Event: " + getInstance().getConfigurationPath();
    }
}
