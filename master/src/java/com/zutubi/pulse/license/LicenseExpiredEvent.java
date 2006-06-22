package com.zutubi.pulse.license;

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
}
