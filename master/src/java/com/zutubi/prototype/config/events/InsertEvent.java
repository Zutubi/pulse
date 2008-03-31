package com.zutubi.prototype.config.events;

import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.pulse.core.config.Configuration;

/**
 */
public class InsertEvent extends CascadableEvent
{
    public InsertEvent(ConfigurationTemplateManager source, Configuration newInstance, boolean cascaded)
    {
        super(source, newInstance, cascaded);
    }

    public boolean isPost()
    {
        return false;
    }

    public String toString()
    {
        return "Insert Event: " + getInstance().getConfigurationPath();
    }
}
