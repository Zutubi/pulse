/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.license;

import com.zutubi.pulse.events.Event;

/**
 * The base event type for license related events.
 */
public class LicenseEvent extends Event<License>
{
    public LicenseEvent(License source)
    {
        super(source);
    }

    public License getLicense()
    {
        return getSource();
    }
}
