package com.zutubi.pulse.master;

import com.zutubi.pulse.master.events.build.BuildRequestEvent;

/**
 * The single build scheduler handler is the simplest implementation
 * of the ScheduleHandler, and simply passes the build request on
 * to the FatController.  If the build request does not already have
 * a build id, then one is assigned.
 */
public class SingleBuildScheduleHandler extends BaseScheduleHandler
{
    private FatController scheduler;

    public void handle(BuildRequestEvent request)
    {
        if (request.getBuildId() == 0)
        {
            request.setBuildId(getMetaBuildId());
        }
        scheduler.enqueueBuildRequest(request);
    }

    public void setFatController(FatController scheduler)
    {
        this.scheduler = scheduler;
    }

    public boolean isMultiBuild()
    {
        return false;
    }
}
