package com.zutubi.tove.config.events;

import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.config.ConfigurationTemplateManager;

/**
 * Raised when an instance is being inserted.  Note that the insert may later
 * fail and the transaction be rolled back.  Thus this event is most useful
 * when the handler makes other changes that should only be committed if the
 * insert goes ahead.  To only react when the transaction is certain to
 * commit handle {@link PostInsertEvent}.
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
