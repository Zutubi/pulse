package com.zutubi.tove.config.events;

import com.zutubi.tove.config.Configuration;
import com.zutubi.tove.config.ConfigurationTemplateManager;

/**
 * Raised when an instance is being changed.  Note that the save may later
 * fail and the transaction be rolled back.  Thus this event is most useful
 * when the handler makes other changes that should only be committed if the
 * save goes ahead.  To only react when the transaction is certain to commit
 * handle {@link PostSaveEvent}.
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
