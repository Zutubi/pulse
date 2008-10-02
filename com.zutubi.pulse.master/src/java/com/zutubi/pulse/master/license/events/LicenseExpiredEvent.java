package com.zutubi.pulse.master.license.events;

import com.zutubi.pulse.master.license.License;

/**
 * A license event indicating that a license has expired.
 *
 */
public class LicenseExpiredEvent extends LicenseEvent
{
    public LicenseExpiredEvent(License source)
    {
        super(source);
    }

    public String toString()
    {
        return "License Expired Event";
    }    
}
