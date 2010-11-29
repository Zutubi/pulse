package com.zutubi.pulse.master.license.events;

import com.zutubi.events.Event;
import com.zutubi.events.EventListener;

/**
 * A utility class for listeners interested in license events.
 */
public abstract class LicenseEventListener implements EventListener
{
    public void handleEvent(Event event)
    {
        if (event instanceof LicenseExpiredEvent)
        {
            licenseExpired();
        }
        else if (event instanceof LicenseUpdateEvent)
        {
            licenseUpdated();
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{LicenseEvent.class};
    }

    /**
     * This method is called if a license expired event is handled.
     */
    public void licenseExpired()
    {

    }

    /**
     * This method is called if a license updated event is handled.
     */
    public void licenseUpdated()
    {
        
    }

}
