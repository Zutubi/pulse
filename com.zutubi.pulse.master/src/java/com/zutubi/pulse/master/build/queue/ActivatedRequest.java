package com.zutubi.pulse.master.build.queue;

import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.pulse.master.build.control.BuildController;

/**
 * The activated request holds the details of the build requests that
 * have been passed through the build queue and been activated.
 *
 * A build request is activated when it is considered ready to build.
 * The build then receives a build controller and awaits dispatch to
 * the first available agent for building.
 *
 * A build remains activated from the moment is leaves the queue to the
 * time the scheduler received a build completed event.
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
