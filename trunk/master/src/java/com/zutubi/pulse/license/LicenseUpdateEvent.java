package com.zutubi.pulse.license;

/**
 * An event indicating that the system license has been changed.
 *
 * @author Daniel Ostermeier
 */
public class LicenseUpdateEvent extends LicenseEvent
{
    public LicenseUpdateEvent(License source)
    {
        super(source);
    }

    public String toString()
    {
        return "License Update Event";
    }
}
