package com.zutubi.pulse.master.license.events;

import com.zutubi.events.Event;
import com.zutubi.pulse.master.license.License;

/**
 * The base event type for license related events.
 */
public abstract class LicenseEvent extends Event
{
    public LicenseEvent(License source)
    {
        super(source);
    }

    /**
     * Get the license associated with this event.
     *
     * @return the license instance associated with this event.
     */
    public License getLicense()
    {
        return (License) getSource();
    }
}
