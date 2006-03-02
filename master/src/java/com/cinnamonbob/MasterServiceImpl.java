package com.cinnamonbob;

import com.cinnamonbob.bootstrap.ComponentContext;
import com.cinnamonbob.events.Event;
import com.cinnamonbob.events.EventManager;
import com.cinnamonbob.services.MasterService;

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
