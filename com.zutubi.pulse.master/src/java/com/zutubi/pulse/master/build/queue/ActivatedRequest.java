package com.zutubi.pulse.master.build.queue;

import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.pulse.master.build.control.BuildController;

/**
 * Holds an activated build request.
 *
 * A build request is activated when it is considered ready to build.
 * The build then receives a build controller and awaits dispatch to
 * the first available agent for building.
 *
 * A build remains activated from the moment is leaves the queue to the
 * time the scheduler receives a build completed event.
 */
public class ActivatedRequest extends RequestHolder
{
    private BuildController controller;

    protected ActivatedRequest(BuildRequestEvent request)
    {
        super(request);
    }

    public void setController(BuildController controller)
    {
        this.controller = controller;
    }

    public BuildController getController()
    {
        return controller;
    }
}
