package com.zutubi.pulse.master;

import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.pulse.master.events.build.BuildCompletedEvent;
import com.zutubi.pulse.master.events.build.MetaBuildCompletedEvent;
import com.zutubi.events.EventListener;
import com.zutubi.events.Event;
import com.zutubi.events.EventManager;

/**
 * The single build scheduler handler is the simplest implementation
 * of the ScheduleHandler, and simply passes the build request on
 * to the FatController.  If the build request does not already have
 * a build id, then one is assigned.
 */
public class SingleMetaBuildHandler extends BaseMetaBuildHandler
{
    private FatController fatController;
    private EventListener eventListener;
    private EventManager eventManager;

    @Override
    public void init()
    {
        super.init();

        eventListener = new EventListener()
        {
            public void handleEvent(Event event)
            {
                BuildCompletedEvent evt = (BuildCompletedEvent) event;
                if (evt.getBuildResult().getMetaBuildId() == getMetaBuildId())
                {
                    buildFinished(evt);
                }
            }

            public Class[] getHandledEvents()
            {
                return new Class[]{BuildCompletedEvent.class};
            }
        };

        eventManager.register(eventListener);
    }

    private void buildFinished(BuildCompletedEvent evt)
    {
        eventManager.unregister(eventListener);
        eventManager.publish(new MetaBuildCompletedEvent(this, evt.getBuildResult(), evt.getContext()));
    }

    public void handle(BuildRequestEvent request)
    {
        if (request.getMetaBuildId() == 0)
        {
            request.setMetaBuildId(getMetaBuildId());
        }
        fatController.enqueueBuildRequest(request);
    }

    public void setFatController(FatController fatController)
    {
        this.fatController = fatController;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }
}
