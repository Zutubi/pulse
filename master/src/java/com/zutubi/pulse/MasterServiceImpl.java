/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse;

import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.services.MasterService;

/**
 */
public class MasterServiceImpl implements MasterService
{
    EventManager eventManager;

    public void handleEvent(Event event)
    {
        eventManager.publish(event);
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }
}
