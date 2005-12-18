package com.cinnamonbob;

import com.cinnamonbob.bootstrap.ComponentContext;
import com.cinnamonbob.core.event.Event;
import com.cinnamonbob.core.event.EventManager;
import com.cinnamonbob.services.MasterService;

/**
 */
public class MasterServiceImpl implements MasterService
{
    EventManager eventManager;

    public void handleEvent(Event event)
    {
        // TODO: fix wiring of services from hessian
        ComponentContext.autowire(this);
        eventManager.publish(event);
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }
}
