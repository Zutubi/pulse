package com.zutubi.pulse.core.events;

import com.zutubi.pulse.events.Event;

/**
 * This event is generated when the systems data directory is changed. This event will be generated
 * when the data directory is specified via the Web UI.
 *  
 */
public class DataDirectoryChangedEvent extends Event
{
    public DataDirectoryChangedEvent(Object source)
    {
        super(source);
    }
}
