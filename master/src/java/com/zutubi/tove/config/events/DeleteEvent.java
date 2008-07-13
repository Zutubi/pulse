package com.zutubi.tove.config.events;

import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.tove.config.ConfigurationTemplateManager;

/**
 * Raised when an instance is being deleted.  Note that the delete may later
 * fail and the transaction be rolled back.  Thus this event is most useful
 * when the handler makes other changes that should only be committed if the
 * delete goes ahead.  To only react when the transaction is certain to
 * commit handle {@link PostDeleteEvent}.
 */
public class DeleteEvent extends CascadableEvent
{
    public DeleteEvent(ConfigurationTemplateManager source, Configuration oldInstance, boolean cascaded)
    {
        super(source, oldInstance, cascaded);
    }

    public boolean isPost()
    {
        return false;
    }

    public String toString()
    {
        return "Delete Event: " + getInstance().getConfigurationPath();
    }
}
