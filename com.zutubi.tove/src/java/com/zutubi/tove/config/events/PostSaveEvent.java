package com.zutubi.tove.config.events;

import com.zutubi.tove.config.Configuration;
import com.zutubi.tove.config.ConfigurationTemplateManager;

/**
 * Raised when an instance has been changed and the transaction is being
 * committed.  Note that you should not interact with the configuration
 * system while handling this event.  If you need to interact with the
 * configuration system, consider handling {@link SaveEvent}.
 */
public class PostSaveEvent extends ConfigurationEvent
{
    public PostSaveEvent(ConfigurationTemplateManager source, Configuration newInstance)
    {
        super(source, newInstance);
    }

    public boolean isPost()
    {
        return true;
    }

    public String toString()
    {
        return "Post Save Event: " + getInstance().getConfigurationPath();
    }
}
